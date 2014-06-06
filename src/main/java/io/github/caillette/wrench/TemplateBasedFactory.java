package io.github.caillette.wrench;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.reflect.AbstractInvocationHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author Laurent Caillette
 */
public abstract class TemplateBasedFactory< C extends Configuration >
    implements Configuration.Factory< C >
{
  private final Class< C > configurationClass ;
  private ConstructionKit< C > constructionKit = new ConstructionKit<>() ;
  protected final C template ;
  private final ImmutableMap< String, Configuration.Property< C > > propertySet ;

  protected TemplateBasedFactory( final Class< C > configurationClass ) throws DefinitionException {
    this.configurationClass = checkNotNull( configurationClass ) ;

    Class< ? > currentClass = configurationClass ;
    while( currentClass != null && ! currentClass.isAssignableFrom( Object.class ) ) {
      final Method[] methods = currentClass.getDeclaredMethods() ;
      for( final Method method : methods ) {
        if( method.getParameterTypes().length == 0 ) {
          constructionKit.features.put(
              method, new HashMap< Configuration.PropertySetup2.Feature, Object >() ) ;
        } else {
          throw new DefinitionException(
              "Should have no parameters: " + method.toGenericString() ) ;
        }
      }
      currentClass = currentClass.getSuperclass() ;
    }
    constructionKit.collector = new PropertySetupCollector2<>(
        configurationClass,
        new Configuration.PropertySetup2.SetupAcceptor() {
          @Override
          public void accept(
              final Method method,
              final Configuration.PropertySetup2.Feature feature,
              final Object object
          ) {
            acceptFeature( method, feature, object ) ;
          }
        }
    ) ;
    template = constructionKit.collector.template ;
    initialize() ;
    propertySet = buildPropertyMap(
        constructionKit.features,
        constructionKit.transientConverters,
        constructionKit.transientNameTransformer
    ) ;
    constructionKit = null ;
  }

  private static class ConstructionKit< C extends Configuration > {
    private final Map< Method, Map<Configuration.PropertySetup2.Feature, Object > > features
        = new HashMap<>() ;
    public PropertySetupCollector2< C > collector ;
    public ImmutableMap< Class< ? >, Configuration.Converter > transientConverters
        = Converters.DEFAULTS ;
    public Configuration.NameTransformer transientNameTransformer = NameTransformers.IDENTITY ;
  }

  private void acceptFeature(
      final Method method,
      final Configuration.PropertySetup2.Feature feature,
      final Object object
  ) {
    final Map< Configuration.PropertySetup2.Feature, Object > propertyFeaturesMap
        = constructionKit.features.get( method ) ;
    if( propertyFeaturesMap == null ) {
      throw new IllegalArgumentException( "Unknown: " + method ) ;
    }
    propertyFeaturesMap.put( feature, object ) ;
  }


  /**
   * Only call from {@link #initialize()}.
   *
   * @param converters a non-null object.
   */
  protected final void setConverters(
      final ImmutableMap< Class< ? >, Configuration.Converter > converters
  ) {
    checkInitializing() ;
    constructionKit.transientConverters = checkNotNull( converters ) ;
  }


  /**
   * Only call from {@link #initialize()}.
   *
   * @param nameTransformer a non-null object.
   */
  protected final void setGlobalNameTransformer(
      final Configuration.NameTransformer nameTransformer
  ) {
    checkInitializing() ;
    constructionKit.transientNameTransformer = checkNotNull( nameTransformer ) ;
  }

  protected void initialize() { }

  protected final < T > Configuration.PropertySetup2< C, T > on(
      final T templateCallResult
  ) {
    checkInitializing() ;
    return constructionKit.collector.on( templateCallResult ) ;
  }

  private void checkInitializing() {
    checkState( constructionKit != null, "Don't call this method outside of #intialize()" ) ;
  }

// ==============================
// Building the map of properties
// ==============================

  private static< C extends Configuration > ImmutableMap< String, Configuration.Property< C > >
  buildPropertyMap(
      final Map< Method, Map<Configuration.PropertySetup2.Feature, Object > > allFeatures,
      final ImmutableMap< Class< ? >, Configuration.Converter > defaultConverters,
      final Configuration.NameTransformer globalNameTransformer
  ) throws DefinitionException {

    final Map< String, Configuration.Property< C > > propertyBuilder = new HashMap<>() ;

    for( final Map.Entry< Method, Map< Configuration.PropertySetup2.Feature, Object > > entry
        : allFeatures.entrySet()
    ) {
      final Method method = entry.getKey() ;
      final Map< Configuration.PropertySetup2.Feature, Object > features = entry.getValue() ;

      final Configuration.Converter converter
          = resolveConverter( method, features, defaultConverters ) ;
      final Configuration.NameTransformer specificNameTransformer
          = resolveNameTransformer( features ) ;
      final String explicitName = resolveExplicitName( features ) ;
      final String propertyName = resolvePropertyName(
          method, explicitName, specificNameTransformer, globalNameTransformer ) ;
      checkUniqueName( method, propertyBuilder, propertyName ) ;
      final Object defaultValue = resolveDefaultValue( features ) ;
      final String defaultValueAsString
          = resolveDefaultValueAsString( features, defaultValue ) ;
      final boolean maybeNull = resolveMayBeNull( features ) ;
      final Pattern obfuscatorPattern = resolveObfuscatorPattern( features ) ;
      final String documentation = resolveDocumentation( features ) ;
      final ConfigurationProperty< C > configurationProperty = new ConfigurationProperty<>(
          method,
          propertyName,
          defaultValue,
          defaultValueAsString,
          converter,
          maybeNull,
          obfuscatorPattern,
          documentation
      ) ;
      propertyBuilder.put( propertyName, configurationProperty ) ;
    }
    return ImmutableMap.copyOf( propertyBuilder ) ;
  }

  private static < C extends Configuration > void checkUniqueName(
      final Method originatingMethod,
      final Map< String, Configuration.Property< C > > propertyBuilder,
      final String propertyName
  ) {
    for( final Configuration.Property< C > property : propertyBuilder.values() ) {
      if( propertyName.equals( property.name() ) ) {
        throw new DefinitionException(
              "Duplicate name: property '" + propertyName
            + " in #" + originatingMethod.getName() + "()"
            + " collides with the one defined in #" + property.declaringMethod() + "()"
        ) ;
      }
    }
  }

  private static Object resolveDefaultValue(
      final Map< Configuration.PropertySetup2.Feature, Object > features
  ) {
    return features.get(
        Configuration.PropertySetup2.Feature.DEFAULT_VALUE );
  }

  private static String resolvePropertyName(
      final Method method,
      final String explicitName,
      final Configuration.NameTransformer specificNameTransformer,
      final Configuration.NameTransformer globalNameTransformer
  ) {
    if( explicitName == null ) {
      if( specificNameTransformer == null ) {
        if( globalNameTransformer == null ) {
          return method.getName() ;
        } else {
          return globalNameTransformer.transform( method.getName() ) ;
        }
      } else {
        return specificNameTransformer.transform( method.getName() ) ;
      }
    } else {
      return explicitName ;
    }
  }

  private static String resolveExplicitName(
      final Map<Configuration.PropertySetup2.Feature, Object> features
  ) {
    return ( String ) features.get(
        Configuration.PropertySetup2.Feature.NAME ) ;
  }

  private static Configuration.NameTransformer resolveNameTransformer(
      final Map< Configuration.PropertySetup2.Feature, Object > features
  ) {
    return ( Configuration.NameTransformer ) features.get(
        Configuration.PropertySetup2.Feature.NAME_TRANSFORMER );
  }

  private static Configuration.Converter resolveConverter(
      final Method method,
      final Map< Configuration.PropertySetup2.Feature, Object > features,
      final ImmutableMap< Class< ? >, Configuration.Converter > defaultConverters
  ) {
    final Configuration.Converter converter ;
    final Configuration.Converter explicitConverter = ( Configuration.Converter )
        features.get( Configuration.PropertySetup2.Feature.CONVERTER ) ;
    if( explicitConverter == null ) {
      final Configuration.Converter defaultConverter
          = defaultConverters.get( method.getReturnType() ) ;
      if( defaultConverter == null ) {
        throw new DefinitionException(
            "No " + ConfigurationTools.getNiceName( Configuration.Converter.class )
                + " declared for " + method.getReturnType().getName() ) ;
      } else {
        converter = defaultConverter ;
      }
    } else {
      converter = explicitConverter ;
    }
    return converter ;
  }

  private static boolean resolveMayBeNull(
      final Map< Configuration.PropertySetup2.Feature, Object > features
  ) {
    boolean maybeNull;
    final Boolean explicitMayBeNull = ( Boolean ) features.get(
        Configuration.PropertySetup2.Feature.MAYBE_NULL ) ;
    maybeNull = explicitMayBeNull != null && explicitMayBeNull ;
    return maybeNull ;
  }

  private static Pattern resolveObfuscatorPattern(
      final Map< Configuration.PropertySetup2.Feature, Object > features
  ) {
    Pattern obfuscatorPattern;
    final Pattern explicitPattern =  ( Pattern ) features.get(
        Configuration.PropertySetup2.Feature.OBFUSCATOR ) ;
    obfuscatorPattern = explicitPattern ;
    return obfuscatorPattern;
  }

  private static String resolveDocumentation(
      final Map< Configuration.PropertySetup2.Feature, Object > features
  ) {
    String documentation = ( String ) features.get(
        Configuration.PropertySetup2.Feature.DOCUMENTATION ) ;
    return documentation ;
  }

  private static String resolveDefaultValueAsString( Map<Configuration.PropertySetup2.Feature, Object> features, Object defaultValue ) {
    final String defaultValueAsString ;
    final String explicitValueAsString = ( String ) features.get(
        Configuration.PropertySetup2.Feature.DEFAULT_VALUE_AS_STRING ) ;
    if( explicitValueAsString == null && defaultValue != null ) {
      defaultValueAsString = defaultValue.toString() ;
    } else {
      defaultValueAsString = null ;
    }
    return defaultValueAsString ;
  }

// ======================
// public Factory methods
// ======================

  @Override
  public final C create( Configuration.Source source1, Configuration.Source... others )
      throws ConfigurationException
  {
    final List< Configuration.Source > sources = new ArrayList<>( others.length + 2 ) ;
    sources.add( new PropertyDefaultSource2< C >( ImmutableSet.copyOf( propertySet.values() ) ) ) ;
    sources.add( source1 ) ;
    Collections.addAll( sources, others ) ;

    final Map< String, ValuedProperty> values = new HashMap<>() ;
    final List< ConfigurationException > exceptions = new ArrayList<>() ;

    for( final Configuration.Source source : sources ) {
      if( source instanceof Configuration.Source.Stringified ) {
        final Configuration.Source.Stringified stringified
            = ( Configuration.Source.Stringified ) source ;
        checkPropertyNamesAllDeclared(
            stringified, stringified.map().keySet(), propertySet, exceptions ) ;
      } else if( source instanceof Configuration.Source.Raw ) {
        final Configuration.Source.Raw raw = ( Configuration.Source.Raw< C > ) source ;
        checkPropertyNamesAllDeclared(
            raw,
            raw.map().keySet(),
            ImmutableSet.copyOf( propertySet.values() ),
            exceptions
        ) ;
      } else {
        throw new IllegalArgumentException( "Unsupported: " + source ) ;
      }
    }

    for( final Map.Entry< String, Configuration.Property< C > > entry : propertySet.entrySet() ) {
      final Configuration.Property< C > property = entry.getValue() ;
      for( final Configuration.Source source : sources ) {
        if( source instanceof Configuration.Source.Stringified ) {
          feedValues( values, exceptions, property, source ) ;
        } else if( source instanceof Configuration.Source.Raw ) {
          final Configuration.Source.Raw rawSource = ( Configuration.Source.Raw ) source ;
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
            } else if( value == ValuedProperty.NULL_VALUE ) {
              values.put( property.name(), new ValuedProperty( property, source, null ) ) ;
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

    verifyNoUndefinedProperty( configuration, propertySet, valuedProperties ) ;

    final ImmutableSet< Validator.Infrigement< C > > validation = validate( configuration ) ;
    if( ! validation.isEmpty() ) {
      throw new ValidationException( ( Iterable< Validator.Infrigement > ) ( Object ) validation ) ;
    }

    return configuration ;
  }

  @Override
  public final Class< C > configurationClass() {
    return configurationClass ;
  }

  @Override
  public final ImmutableMap< String, Configuration.Property< C > > properties() {
    final ImmutableMap.Builder< String, Configuration.Property< C > > builder
        = ImmutableMap.builder() ;
    for( final Configuration.Property< C > property : propertySet.values() ) {
      builder.put( property.name(), property ) ;
    }
    return builder.build() ;
  }

  protected ImmutableSet< Validator.Infrigement< C > > validate( final C configuration ) {
    return ImmutableSet.of() ;
  }

// ============
// More methods
// ============

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
        new Class[]{ configurationClass, ConfigurationInspector.SupportEnabled.class },
        new AbstractInvocationHandler() {
          @SuppressWarnings( "NullableProblems" )
          @Override
          protected Object handleInvocation(
              final Object proxy,
              final Method method,
              final Object[] args
          ) throws Throwable {
            if ( method.getDeclaringClass().equals( ConfigurationInspector.SupportEnabled.class ) ) {
              if ( "$$support$$".equals( method.getName() ) ) {
                return inspector;
              } else {
                throw new UnsupportedOperationException( "Unsupported: "
                    + method.getDeclaringClass() + "#" + method.getName() );
              }
            }

            final ValuedProperty valuedProperty = valuedPropertiesByMethod.get( method );
            lastAccessedProperty.set( valuedProperty.property );
            return valuedProperty.resolvedValue;
          }

          @Override
          public String toString() {
            return ConfigurationTools.getNiceName( configurationClass ) + "{"
                + TemplateBasedFactory.toString( valuedPropertiesByMethod.values() )
                + "}"
                ;
          }
        }
    );
  }

  private static boolean checkPropertyNamesAllDeclared(
      final Configuration.Source source,
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
      final Configuration.Source.Raw source,
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


  private static String toString(
      final ImmutableCollection< ValuedProperty > valuedProperties
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

  private static< C extends Configuration > void feedValues(
      final Map< String, ValuedProperty > values,
      final List< ConfigurationException > exceptions,
      final Configuration.Property< C > property,
      final Configuration.Source source
  ) {
    final Object convertedValue ;
    final String valueFromSource = ( ( Configuration.Source.Stringified ) source ).map().get( property.name() ) ;
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

  private static Object convertSafe(
      final List< ConfigurationException > exceptions,
      final Configuration.Property property,
      final String valueFromSource,
      final Configuration.Source source
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

  private static final Object CONVERSION_FAILED = new Object() {
    @Override
    public String toString() {
      return ConfigurationFactory.class.getSimpleName() + "{(magic)CONVERSION_FAILED}" ;
    }
  } ;

}
