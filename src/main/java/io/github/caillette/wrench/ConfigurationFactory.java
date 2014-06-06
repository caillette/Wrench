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

import static io.github.caillette.wrench.Configuration.Source;

class ConfigurationFactory< C extends Configuration > implements Configuration.Factory< C > {

  private final Class< C > configurationClass ;
  private final ImmutableMap< String, Configuration.Property< C > > properties ;

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
    final Map< String, Configuration.Property< C > > builder = new HashMap<>();
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
  public C create( Source source1, Source... others )
      throws ConfigurationException
  {
    final List< Source > sources = new ArrayList<>( others.length + 2 ) ;
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

    for( final Source source : sources ) {
      if( source instanceof Source.Stringified ) {
        final Source.Stringified stringified = ( Source.Stringified ) source ;
        checkPropertyNamesAllDeclared(
            stringified, stringified.map().keySet(), properties, exceptions ) ;
      } else if( source instanceof Source.Raw ) {
        final Source.Raw raw = ( Source.Raw< C > ) source ;
        checkPropertyNamesAllDeclared(
            raw,
            raw.map().keySet(),
            ImmutableSet.copyOf( properties.values() ),
            exceptions
        ) ;
      } else {
        throw new IllegalArgumentException( "Unsupported: " + source ) ;
      }
    }

    for( final Map.Entry< String, Configuration.Property< C > > entry : properties.entrySet() ) {
      final Configuration.Property< C > property = entry.getValue() ;
      for( final Source source : sources ) {
        if( source instanceof Source.Stringified ) {
          feedValues( values, exceptions, property, source ) ;
        } else if( source instanceof Source.Raw ) {
          final Source.Raw rawSource = ( Source.Raw ) source ;
          final Object value = rawSource.map().get( property ) ;
          if( value == null ) {
            if( rawSource.map().containsKey( property ) ) {
              if( property.maybeNull() ) {
                values.put( property.name(), new ValuedProperty( property, source, null ) ) ;
              } else {
                throw new UnsupportedOperationException( "TODO: accumulate" ) ;
              }
            }
          } else {
            if( property.type().isAssignableFrom( value.getClass() ) ) {
              values.put( property.name(), new ValuedProperty( property, source, value ) ) ;
            } else {
              throw new UnsupportedOperationException( "TODO: accumulate" ) ;
            }
          }
        }
      }
    }

    if( exceptions.size() > 0 ) {
      throw new ConfigurationException( exceptions ) ;
    }

    final ImmutableSortedMap< String, ValuedProperty > valuedProperties
        = ImmutableSortedMap.copyOf( values ) ;


    final C configuration = createProxy( valuedProperties ) ;

    verifyNoUndefinedProperty( configuration, properties, valuedProperties ) ;

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

  private static< C extends Configuration > void feedValues(
      final Map< String, ValuedProperty > values,
      final List< ConfigurationException > exceptions,
      final Configuration.Property< C > property,
      final Source source
  ) {
    final Object convertedValue ;
    final String valueFromSource = ( ( Source.Stringified ) source ).map().get( property.name() ) ;
    if( source instanceof PropertyDefaultSource ) {
      if( property.maybeNull() ) {
        convertedValue = null ;
      } else {
        convertedValue = property.defaultValueAsString() == null
            ? CONVERSION_FAILED
            : property.defaultValue()
        ;
      }
    } else {
      convertedValue
          = convertSafe( exceptions, property, valueFromSource, source ) ;
    }
    if ( convertedValue != CONVERSION_FAILED ) {
      final ValuedProperty valuedProperty = new ValuedProperty(
          property, source, valueFromSource, convertedValue ) ;
      values.put( property.name(), valuedProperty ) ;
    }
  }

  private static < C extends Configuration > void verifyNoUndefinedProperty(
      final C configuration,
      final ImmutableMap< String, Configuration.Property< C > > properties,
      final ImmutableSortedMap< String, ValuedProperty > valuedProperties
  ) throws ValidationException {
    final Validator.Accumulator< C > accumulator = new Validator.Accumulator<>( configuration ) ;
    for( final Configuration.Property< C > property : properties.values() ) {
      final ValuedProperty valuedProperty = valuedProperties.get( property.name() ) ;
      if( valuedProperty == null ) {
        accumulator.addInfrigementForNullity( property, "No value set" ) ;
      }
    }
    accumulator.throwExceptionIfHasInfrigements() ;
  }

  @SuppressWarnings( "unchecked" )
  private C createProxy( final ImmutableSortedMap< String, ValuedProperty> properties ) {
    final ThreadLocal< Configuration.Property< C > > lastAccessedProperty = new ThreadLocal<>() ;
    final Configuration.Inspector inspector
        = new ConfigurationInspector( properties, lastAccessedProperty ) ;
    final ImmutableMap.Builder< Method, ValuedProperty > builder = ImmutableMap.builder() ;
    for( final ValuedProperty valuedProperty : properties.values() ) {
      builder.put( valuedProperty.property.declaringMethod(), valuedProperty ) ;
    }
    final ImmutableMap< Method, ValuedProperty > valuedPropertiesByMethod = builder.build() ;
    return ( C ) Proxy.newProxyInstance(
        getClass().getClassLoader(),
        new Class[]{ configurationClass, ConfigurationInspector.InspectorEnabled.class },
        new AbstractInvocationHandler() {
          @SuppressWarnings( "NullableProblems" )
          @Override
          protected Object handleInvocation(
              final Object proxy,
              final Method method,
              final Object[] args
          ) throws Throwable {
            if( method.getDeclaringClass().equals( ConfigurationInspector.InspectorEnabled.class ) ) {
              if( "$$inspector$$".equals( method.getName() ) ) {
                return inspector;
              } else {
                throw new UnsupportedOperationException( "Unsupported: "
                    + method.getDeclaringClass() + "#" + method.getName() ) ;
              }
            }

            final ValuedProperty valuedProperty = valuedPropertiesByMethod.get( method ) ;
            lastAccessedProperty.set( valuedProperty.property ) ;
            return valuedProperty.resolvedValue ;
          }

          @Override
          public String toString() {
            return ConfigurationTools.getNiceName( configurationClass ) + "{"
                + ConfigurationFactory.toString( valuedPropertiesByMethod.values() )
                + "}"
                ;
          }
        }
    );
  }

  private static boolean checkPropertyNamesAllDeclared(
      final Source source,
      final ImmutableSet< String > actualPropertyNames,
      final ImmutableMap< String, ? extends Configuration.Property > declaredProperties,
      final List< ConfigurationException > exceptions
  ) {
    boolean good = true ;
    for( final String actualPropertyName : actualPropertyNames ) {
      if( ! declaredProperties.containsKey( actualPropertyName ) ) {
        exceptions.add( new DeclarationException(
            "Unknown property name '" + actualPropertyName + "' from " + source.sourceName() ) ) ;
        good = false ;
      }
    }
    return good ;
  }

  private static< C extends Configuration > boolean checkPropertyNamesAllDeclared(
      final Source.Raw source,
      final ImmutableSet<Configuration.Property< C > > actualProperties,
      final ImmutableSet< Configuration.Property< C > > declaredProperties,
      final List< ConfigurationException > exceptions
  ) {
    boolean good = true ;
    for( final Configuration.Property actualProperty : actualProperties ) {
      if( ! declaredProperties.contains( actualProperty ) ) {
        exceptions.add( new DeclarationException(
            "Unknown property '" + actualProperty + "' "
                + "from " + source.sourceName() ) ) ;
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
      final Configuration.Property property,
      final String valueFromSource,
      final Source source
  ) {
    try {
      if( valueFromSource != null ) {
        return property.converter().convert( property.declaringMethod(), valueFromSource ) ;
      }
    } catch ( final Exception e ) {
      exceptions.add( ConvertException.toConvertException( e, property, source ) ) ;
    }
    return CONVERSION_FAILED ;
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
  public ImmutableMap< String, Configuration.Property< C > > properties() {
    return properties;
  }
}
