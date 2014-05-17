package io.github.caillette.wrench;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.reflect.AbstractInvocationHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * @author Laurent Caillette
 */
class ConfigurationFactory< C extends Configuration > implements Configuration.Factory< C > {

  private final Class< C > configurationClass ;
  private final ImmutableMap< String, Configuration.Property< C >> properties ;

  public ConfigurationFactory(
      final Class< C > configurationClass,
      final ImmutableMap< Class< ? >, Configuration.Converter > converters
  ) {
    this.configurationClass = Preconditions.checkNotNull( configurationClass ) ;
    final ImmutableSet<Configuration.Property< C >> propertySet;
    try {
      propertySet = ConfigurationTools.extractProperties(
          configurationClass, converters ) ;
    } catch ( ConvertException e ) {
      throw new DefinitionException( e ) ;
    }
    final Map< String, Configuration.Property< C >> builder = new HashMap<>();
    for( final Configuration.Property< C > property : propertySet ) {
      if( builder.containsKey( property.name() ) ) {
        throw new DefinitionException(
            "Property with name '" + property.name() + "' "
            + "defined in both " + builder.get( property.name() ).declaringMethod()
            + " and " + property.declaringMethod() ) ;
      }
      builder.put( property.name(), property ) ;
    }
    properties = ImmutableMap.copyOf( builder ) ;
  }

  @Override
  public C create( Configuration.Source source1, Configuration.Source... others )
      throws ConfigurationException
  {
    final List< Configuration.Source > sources = new ArrayList<>( others.length + 2 ) ;
    sources.add(
        new PropertyDefaultSource(
            configurationClass,
            ImmutableSet.copyOf( properties.values() )
        )
    ) ;
    sources.add( source1 ) ;
    Collections.addAll( sources, others ) ;

    final Map< String, ValuedProperty> values = new HashMap<>() ;
    final List< ConfigurationException > exceptions = new ArrayList<>() ;

    for( final Configuration.Source source : sources ) {
      checkPropertyNamesAllDeclared( source, source.map().keySet(), properties, exceptions ) ;
    }

    for( final Map.Entry< String, Configuration.Property< C >> entry
        : properties.entrySet()
    ) {
      final Configuration.Property< C > propertyProperty = entry.getValue() ;
      final String propertyName = entry.getKey() ;
      for( final Configuration.Source source : sources ) {
        final Object convertedValue ;
        final String valueFromSource = source.map().get( propertyName ) ;
        if( source instanceof PropertyDefaultSource ) {
          if( propertyProperty.maybeNull() ) {
            convertedValue = null ;
          } else {
            convertedValue = propertyProperty.defaultValueAsString() == null
                ? CONVERSION_FAILED
                : propertyProperty.defaultValue()
            ;
          }
        } else {
          convertedValue
              = convertSafe( exceptions, propertyProperty, valueFromSource, source ) ;
        }
        if ( convertedValue != CONVERSION_FAILED ) {
          final ValuedProperty valuedProperty = new ValuedProperty(
              propertyProperty, source, valueFromSource, convertedValue ) ;
          values.put( propertyName, valuedProperty ) ;
        }
      }
    }

    if( exceptions.size() > 0 ) {
      throw new ConfigurationException( exceptions ) ;
    }

    final ImmutableSortedMap< String, ValuedProperty> properties
        = ImmutableSortedMap.copyOf( values ) ;

    final C configuration = createProxy( properties ) ;
    final Configuration.Annotations.ValidateWith validateWithAnnotation
        = configurationClass.getAnnotation( Configuration.Annotations.ValidateWith.class ) ;
    if( validateWithAnnotation != null ) {
      try {
        @SuppressWarnings( "unchecked" )
        final Validator validator = validateWithAnnotation.value().newInstance() ;
        @SuppressWarnings( "unchecked" )
        final ImmutableSet< Validator.Infrigement > validation
            = validator.validate( configuration ) ;
        if( ! validation.isEmpty() ) {
          throw new ValidationException( validation ) ;
        }
      } catch ( InstantiationException | IllegalAccessException e ) {
        throw new DefinitionException( e ) ;
      }

    }
    return configuration ;
  }

  @SuppressWarnings( "unchecked" )
  private C createProxy( final ImmutableSortedMap< String, ValuedProperty> properties ) {
    final ThreadLocal< Configuration.Property< C > > lastAccessedProperty = new ThreadLocal<>() ;
    final Configuration.Support support
        = new ConfigurationSupport( properties, lastAccessedProperty ) ;
    return ( C ) Proxy.newProxyInstance(
        getClass().getClassLoader(),
        new Class[]{ configurationClass, ConfigurationSupport.SupportEnabled.class },
        new AbstractInvocationHandler() {
          @SuppressWarnings( "NullableProblems" )
          @Override
          protected Object handleInvocation(
              final Object proxy,
              final Method method,
              final Object[] args
          ) throws Throwable {
            if( method.getDeclaringClass().equals( ConfigurationSupport.SupportEnabled.class ) ) {
              if( "$$support$$".equals( method.getName() ) ) {
                return support ;
              } else {
                throw new UnsupportedOperationException( "Unsupported: "
                    + method.getDeclaringClass() + "#" + method.getName() ) ;
              }
            }

            final ValuedProperty valuedProperty = properties.get( method.getName() ) ;
            lastAccessedProperty.set( valuedProperty.property ) ;
            return valuedProperty.resolvedValue ;
          }

          @Override
          public String toString() {
            return getNiceName( configurationClass ) + "{"
                + ConfigurationFactory.toString( properties.values() )
                + "}"
                ;
          }
        }
    );
  }

  private static boolean checkPropertyNamesAllDeclared(
      final Configuration.Source source,
      final ImmutableSet< String > propertyNames,
      final ImmutableMap< String, ? extends Configuration.Property > properties,
      final List< ConfigurationException > exceptions
  ) {
    boolean good = true ;
    for( final String propertyName : propertyNames ) {
      if( ! properties.containsKey( propertyName ) ) {
        exceptions.add( new DeclarationException(
            "Unknown property name '" + propertyName + "' from " + source.sourceName() ) ) ;
        good = false ;
      }
    }
    return good ;
  }

  private static final Object CONVERSION_FAILED = new Object() {
    @Override
    public String toString() {
      return ConfigurationFactory.class.getSimpleName() + "{(magic)CONVERSION_FAILED}" ;
    }
  } ;

  private static Object convertSafe(
      final List< ConfigurationException > exceptions,
      final Configuration.Property propertyProperty,
      final String valueFromSource,
      final Configuration.Source source
  ) {
    try {
      if( valueFromSource != null ) {
        return propertyProperty.converter().convert(
            propertyProperty.type(), valueFromSource ) ;
      }
    } catch ( final ConvertException e ) {
      exceptions.add( new ConvertException(
          "From " + propertyProperty.converter().toString() + ": "
          + e.getMessage()
          + " for property '" + propertyProperty.name()
          + "' — in " + source.sourceName() ) ) ;
    }
    return CONVERSION_FAILED ;
  }

  private static String getNiceName( final Class originClass ) {
    String className = originClass.getSimpleName() ;
    Class enclosingClass = originClass.getEnclosingClass() ;
    while( enclosingClass != null ) {
      className = enclosingClass.getSimpleName() + "$" + className ;
      enclosingClass = enclosingClass.getEnclosingClass() ;
    }
    return className ;
  }

  private static String toString(
      final ImmutableCollection<ValuedProperty> valuedProperties
  ) {
    final StringBuilder stringBuilder = new StringBuilder() ;
    boolean first = true ;
    for( final ValuedProperty valuedProperty : valuedProperties ) {
      if( first ) {
        first = false ;
      } else {
        stringBuilder.append( "; " ) ;
      }
      stringBuilder.append( valuedProperty.property.name() ) ;
      stringBuilder.append( '=' ) ;
      stringBuilder.append( valuedProperty.stringValue ) ;
    }
    return stringBuilder.toString() ;
  }


  @Override
  public Class< C > configurationClass() {
    return configurationClass ;
  }

  @Override
  public ImmutableMap< String, Configuration.Property< C >> properties() {
    return properties;
  }
}
