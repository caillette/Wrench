package io.github.caillette.wrench.source;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.AbstractInvocationHandler;
import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.ConfigurationTools;
import io.github.caillette.wrench.DeclarationException;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Sets default values with plain objects and static typing.
 * This is an alternative to {@link Configuration.Annotations.DefaultValue} annotation which
 * implies parsing.
 */
public abstract class ObjectSource< C extends Configuration >
    implements Configuration.Source.Raw< C >
{

  private final Collector collector ;
  protected final C template ;

  public ObjectSource( Configuration.Factory< C > factory ) {
    final ImmutableMap.Builder< Method, Configuration.Property< C > > builder
        = ImmutableMap.builder() ;
    for( final Configuration.Property< C > property : factory.properties().values() ) {
      builder.put( property.declaringMethod(), property ) ;
    }
    collector = new Collector( factory, builder.build() ) ;
    template = collector.template ;
  }

  protected final < T > Collector.ValueReceiver< T > on( final T templateCallResult ) {
    return collector.on( templateCallResult ) ;
  }

  private final class Collector {
    private final C template ;
    private Configuration.Property< C > lastAccessed = null ;

    private Collector(
        final Configuration.Factory< C > factory,
        final ImmutableMap< Method, Configuration.Property< C > > properties
    ) {
      //noinspection unchecked,NullableProblems
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

    /**
     * Don't call outside of {@link io.github.caillette.wrench.source.ObjectSource()}.
     */
    public final < T > ValueReceiver< T > on(
        @SuppressWarnings( "UnusedParameters" ) T methodCallResult
    ) {
      return new ValueReceiver<>() ;
    }

    public final class ValueReceiver< T > {
      public void defaultValue( T value ) {
        builder.put( lastAccessed, value ) ;
      }
    }
  }

  @Override
  public final ImmutableMap< Configuration.Property< C >, Object > map() {
    return collector.values() ;
  }

  @Override
  public final String sourceName() {
    return ConfigurationTools.getNiceName( getClass() ) ;
  }
}
