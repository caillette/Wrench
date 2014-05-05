package io.github.caillette.wrench;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.github.caillette.wrench.Configuration.Factory;
import static io.github.caillette.wrench.Configuration.Support;

/**
 *
 * @author Laurent Caillette
 */
public final class ConfigurationTools {

  public static < C extends Configuration > Factory< C > newFactory(
      Class< C > configurationClass
  ) {
    return newFactory( configurationClass, Converters.DEFAULTS ) ;
  }

  public static < C extends Configuration > Factory< C > newFactory(
      Class< C > configurationClass,
      ImmutableMap< Class< ? >, Configuration.Converter > converters
  ) {
    return new ConfigurationFactory<>( configurationClass, converters ) ;
  }

  @SuppressWarnings( "unchecked" )
  public static < C extends Configuration > Support< C > support( C configuration ) {
    return ( ( ConfigurationSupport.SupportEnabled ) configuration ).$$support$$() ;
  }
  
// ===============
// Our own cooking
// ===============

  static< C extends Configuration > ImmutableSet<Configuration.Property< C >> extractProperties(
      final Class<C> configurationClass,
      final ImmutableMap<Class<?>, Configuration.Converter> converters
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
            converter = resolveConverter( method.getReturnType(), converters ) ;
          } catch ( ConvertException e ) {
            throw new DefinitionException( e ) ;
          }
          final Configuration.Property< C > property
              = new ConfigurationProperty<>(
                  method,
                  name,
                  converter,
                  defaultValueAsString,
                  pack.defaulNull != null
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
  static< T > Configuration.Converter< T > resolveConverter(
      final Class< T > targetClass,
      final ImmutableMap< Class< ? >, Configuration.Converter > converters
  ) throws ConvertException {

    @SuppressWarnings( "unchecked" )
    final Configuration.Converter< T > converter = converters.get( targetClass ) ;

    if( converter == null ) {
      throw new ConvertException( "Unsupported: " + targetClass + " in " + converters ) ;
    }
    return converter ;
  }


  private static class AnnotationPack< C extends Configuration > {
    final private Configuration.Annotations.Name name;
    final private Configuration.Annotations.DefaultNull defaulNull ;
    final private Configuration.Annotations.TransformName fieldTransformName ;
    final private Configuration.Annotations.DefaultValue defaultValue ;
    final Configuration.Annotations.TransformName classTransformName;


    public AnnotationPack( Class< C > configurationClass, Method method ) {
      classTransformName
          = configurationClass.getAnnotation( Configuration.Annotations.TransformName.class ) ;
      name
          = method.getAnnotation( Configuration.Annotations.Name.class ) ;
      defaulNull
          = method.getAnnotation( Configuration.Annotations.DefaultNull.class ) ;
      fieldTransformName
          = method.getAnnotation( Configuration.Annotations.TransformName.class ) ;
      defaultValue
          = method.getAnnotation( Configuration.Annotations.DefaultValue.class ) ;

      if( name != null && fieldTransformName != null ) {
        throw new DefinitionException(
            "Can't define at the same time "
                + Configuration.Annotations.Name.class.getName() + " and "
                + Configuration.Annotations.TransformName.class + " in "
                + configurationClass.getName() )
            ;
      }
      if( defaulNull != null && defaultValue != null ) {
        throw new DefinitionException(
            "Can't define at the same time "
                + Configuration.Annotations.DefaultNull.class.getName() + " and "
                + Configuration.Annotations.DefaultValue.class + " in "
                + configurationClass.getName() )
            ;
      }
    }

  }
}
