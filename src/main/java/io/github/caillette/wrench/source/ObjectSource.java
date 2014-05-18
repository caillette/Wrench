package io.github.caillette.wrench.source;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.AbstractInvocationHandler;
import io.github.caillette.wrench.*;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Sets default values with plain objects and static typing.
 * This is an alternative to {@link Configuration.Annotations.DefaultValue} annotation which
 * implies parsing.
 */
public abstract class ObjectSource< C extends Configuration > implements Configuration.Source.Raw< C > {

  protected final ImmutableMap< Configuration.Property< C >, Object > valuedProperties  ;

  public ObjectSource( Configuration.Factory< C > factory ) {
    final ImmutableMap.Builder< Method, Configuration.Property< C > > builder
        = ImmutableMap.builder() ;
    for( final Configuration.Property< C > property : factory.properties().values() ) {
      builder.put( property.declaringMethod(), property ) ;
    }
    final Collector collector = new Collector( factory, builder.build() ) ;
    record( collector, collector.template ) ;
    valuedProperties = collector.values() ;
  }

  protected final class Collector {
    private final C template ;
    private Configuration.Property< C > lastAccessed = null ;

    private Collector(
        final Configuration.Factory< C > factory,
        final ImmutableMap< Method, Configuration.Property< C > > properties
    ) {
      //noinspection unchecked
      template = ( C ) Proxy.newProxyInstance(
          getClass().getClassLoader(),
          new Class< ? >[] { factory.configurationClass() },
          new AbstractInvocationHandler() {
            @Override
            protected Object handleInvocation(
                final Object proxy,
                final Method method,
                final Object[] args
            ) throws Throwable {
              lastAccessed = properties.get( method ) ;
              if( lastAccessed == null ) {
                throw new DeclarationException( "Unknown method in "
                    + factory.configurationClass().getName() + ": " + method.toGenericString() ) ;
              }
              return null ;
            }
          }
      ) ;
    }

    private final Map< Configuration.Property< C >, Object > builder = new HashMap<>() ;

    ImmutableMap< Configuration.Property< C >, Object > values() {
      return ImmutableMap.copyOf( builder ) ;
    }

    public < T > ValueReceiver< T > with(
        @SuppressWarnings( "UnusedParameters" ) T methodCallResult
    ) {
      return new ValueReceiver<>() ;
    }

    public final class ValueReceiver< T > {
      public void put( T value ) {
        builder.put( lastAccessed, value ) ;
      }
    }

  }

  /**
   * <pre>
   *   collector.template().someProperty() ;
   *   collector.set( "Some default value" ) ;
   *
   *   // More verbosely:
   *   collector.template().someProperty() ;
   *   collector.set( collector.lastAccessedProperty(), "Some default value" ) ;
   * </pre>
   */
  protected abstract void record( Collector collector, C template ) ;

  @Override
  public ImmutableMap< Configuration.Property< C >, Object > map() {
    return valuedProperties ;
  }

  @Override
  public String sourceName() {
    return ConfigurationTools.getNiceName( getClass() ) ;
  }
}
