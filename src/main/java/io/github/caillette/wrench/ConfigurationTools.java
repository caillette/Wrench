package io.github.caillette.wrench;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.github.caillette.wrench.Configuration.Annotations;
import static io.github.caillette.wrench.Configuration.Annotations.Convert;
import static io.github.caillette.wrench.Configuration.Factory;

/**
 * Utility methods to create {@link Configuration.Factory} and {@link Configuration.Inspector}
 * objects.
 */
public final class ConfigurationTools {

  public static < C extends Configuration > Factory< C > newFactory(
      final Class< C > configurationClass
  ) {
    return new TemplateBasedFactory< C >( configurationClass ) { } ;
  }

  @Deprecated
  public static < C extends Configuration > Factory< C > newAnnotationBasedFactory(
      Class<C> configurationClass
  ) {
    return newAnnotationBasedFactory( configurationClass, Converters.DEFAULTS ) ;
  }

  @Deprecated
  public static < C extends Configuration > Factory< C > newAnnotationBasedFactory(
      Class<C> configurationClass,
      ImmutableMap<Class<?>, Configuration.Converter> converters
  ) {
    return new ConfigurationFactory<>( configurationClass, converters ) ;
  }

  @SuppressWarnings( "unchecked" )
  public static < C extends Configuration > Configuration.Inspector< C > inspector( C configuration ) {
    return ( ( ConfigurationInspector.InspectorEnabled ) configuration ).$$inspector$$() ;
  }
  
// ===============
// Our own cooking
// ===============

  static< C extends Configuration > ImmutableSet<Configuration.Property< C > > extractProperties(
      final Class< C > configurationClass,
      final ImmutableMap< Class< ? >, Configuration.Converter > converters
  ) throws ConvertException, DefinitionException {
    checkNotNull( configurationClass ) ;
    final Set<Configuration.Property< C >> builder = new TreeSet<>() ;
    Class currentClass = configurationClass ;
    while( currentClass != null && ! currentClass.isAssignableFrom( Object.class ) ) {
      final Method[] methods = currentClass.getDeclaredMethods() ;
      for( final Method method : methods ) {
        if( method.getParameterTypes().length == 0 ) {
          final AnnotationPack pack = new AnnotationPack<>( configurationClass, method ) ;
          final String name = resolveName( method, pack ) ;
          final String defaultValueAsString = pack.defaultValue == null
              ? null : pack.defaultValue.value() ;
          final Configuration.Converter converter ;
          try {
            converter = resolveConverter( method, converters ) ;
          } catch ( ConvertException e ) {
            throw new DefinitionException( e ) ;
          }
          final Pattern obfuscatorPattern = pack.obfuscator == null
              ? null : Pattern.compile( pack.obfuscator.value() ) ;

          final Configuration.Property< C > property
              = new ConfigurationProperty<>(
                  method,
                  name,
                  converter,
                  defaultValueAsString,
                  pack.defaulNull != null,
                  obfuscatorPattern
              )
          ;

          if( ! builder.contains( property ) ) {
            builder.add( property ) ;
          }
        } else {
          throw new DefinitionException(
              "Should have no parameters: " + method.toGenericString() ) ;
        }
      }
      currentClass = currentClass.getSuperclass() ;
    }
    return ImmutableSet.copyOf( builder ) ;
  }

  private static String resolveName(
      final Method method,
      final AnnotationPack annotationPack
  ) {
    if( annotationPack.name != null ) {
      return annotationPack.name.value() ;
    }
    final String methodName = method.getName() ;
    if( annotationPack.fieldTransformName != null ) {
      try {
        return annotationPack.fieldTransformName.value().newInstance().transform( methodName ) ;
      } catch ( InstantiationException | IllegalAccessException e ) {
        throw new DefinitionException( e ) ;
      }
    }
    if( annotationPack.classTransformName != null ) {
      try {
        return annotationPack.classTransformName.value().newInstance().transform( methodName ) ;
      } catch ( InstantiationException | IllegalAccessException e ) {
        throw new DefinitionException( e ) ;
      }
    } else {
      return methodName ;
    }
  }


  /**
   * @return a non-null object.
   */
  static Configuration.Converter resolveConverter(
      final Method method,
      final ImmutableMap< Class< ? >, Configuration.Converter > converters
  ) throws ConvertException, DefinitionException {

    final Convert convertAnnotation = method.getAnnotation( Convert.class ) ;
    if( convertAnnotation == null ) {
      @SuppressWarnings( "unchecked" )
      final Class targetClass = method.getReturnType() ;

      @SuppressWarnings( "unchecked" )
      final Configuration.Converter converter = converters.get( targetClass ) ;

      if( converter == null ) {
        throw new ConvertException( "Unsupported: " + targetClass + " in " + converters ) ;
      }
      return converter ;
    } else {
      try {
        @SuppressWarnings( "unchecked" )
        final Configuration.Converter converter = convertAnnotation.value().newInstance() ;
        return converter ;
      } catch ( InstantiationException | IllegalAccessException e ) {
        throw new DefinitionException( "Could not instantiate " + convertAnnotation.value(), e ) ;
      }
    }

  }


  private static class AnnotationPack< C extends Configuration > {
    final private Annotations.Name name ;
    final private Annotations.DefaultNull defaulNull ;
    final private Annotations.TransformName fieldTransformName ;
    final private Annotations.DefaultValue defaultValue ;
    final Annotations.TransformName classTransformName ;
    final Annotations.Obfuscator obfuscator ;


    public AnnotationPack( Class< C > configurationClass, Method method ) {
      classTransformName
          = configurationClass.getAnnotation( Annotations.TransformName.class ) ;
      name
          = method.getAnnotation( Annotations.Name.class ) ;
      defaulNull
          = method.getAnnotation( Annotations.DefaultNull.class ) ;
      fieldTransformName
          = method.getAnnotation( Annotations.TransformName.class ) ;
      defaultValue
          = method.getAnnotation( Annotations.DefaultValue.class ) ;
      obfuscator
          = method.getAnnotation( Annotations.Obfuscator.class ) ;

      if( name != null && fieldTransformName != null ) {
        throw new DefinitionException(
            "Can't define at the same time "
                + Annotations.Name.class.getName() + " and "
                + Annotations.TransformName.class + " in "
                + configurationClass.getName() )
            ;
      }
      if( defaulNull != null && defaultValue != null ) {
        throw new DefinitionException(
            "Can't define at the same time "
                + Annotations.DefaultNull.class.getName() + " and "
                + Annotations.DefaultValue.class + " in "
                + configurationClass.getName() )
            ;
      }
    }

  }

  public static String getNiceName( final Class originClass ) {
    String className = originClass.getSimpleName() ;
    Class enclosingClass = originClass.getEnclosingClass() ;
    while( enclosingClass != null ) {
      className = enclosingClass.getSimpleName() + "$" + className ;
      enclosingClass = enclosingClass.getEnclosingClass() ;
    }
    return className ;
  }


  public static< C extends Configuration > ImmutableMap< Method, Configuration.Property< C > >
  remap( ImmutableMap< String, Configuration.Property< C > > propertiesByName ) {
    final ImmutableMap.Builder< Method, Configuration.Property< C > > propertiesByMethod
        = ImmutableMap.builder() ;
    for( final Configuration.Property< C > property : propertiesByName.values() ) {
      propertiesByMethod.put( property.declaringMethod(), property ) ;
    }
    return propertiesByMethod.build() ;
  }
}
