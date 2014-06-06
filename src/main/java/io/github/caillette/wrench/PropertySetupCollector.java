package io.github.caillette.wrench;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.AbstractInvocationHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public final class PropertySetupCollector< C extends Configuration> {
  public final C template ;
  private Configuration.Property< C > lastAccessed = null ;

  public PropertySetupCollector(
      final Configuration.Factory< C > factory,
      final ImmutableMap< Method, Configuration.Property< C > > properties
  ) {
    this( factory.configurationClass(), properties ) ;
  }
  public PropertySetupCollector(
      final Class< C > configurationClass,
      final ImmutableMap< Method, Configuration.Property< C > > properties
  ) {
    //noinspection unchecked,NullableProblems
    template = ( C ) Proxy.newProxyInstance(
        getClass().getClassLoader(),
        new Class< ? >[]{ configurationClass },
        new AbstractInvocationHandler() {
          @Override
          protected Object handleInvocation(
              final Object proxy,
              final Method method,
              final Object[] args
          ) throws Throwable {
            lastAccessed = properties.get( method ) ;
            if ( lastAccessed == null ) {
              throw new DeclarationException( "Unknown method in "
                  + configurationClass.getName() + ": " + method.toGenericString() ) ;
            }
            return null;
          }
        }
    ) ;
  }

  private final Map< Configuration.Property< C >, Object > builder = new HashMap<>() ;

  public final ImmutableMap< Configuration.Property< C >, Object > values() {
    return ImmutableMap.copyOf( builder ) ;
  }

  /**
   * Don't call outside of {@link io.github.caillette.wrench.source.ObjectSource ()}.
   */
  public final < T > Configuration.PropertySetup.DefaultValue< C, T > on(
      @SuppressWarnings( "UnusedParameters" ) T methodCallResult
  ) {
    return new Configuration.PropertySetup.DefaultValue<>( lastAccessed, builder ) ;
  }

}
