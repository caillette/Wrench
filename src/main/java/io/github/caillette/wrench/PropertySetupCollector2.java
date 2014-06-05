package io.github.caillette.wrench;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.AbstractInvocationHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public final class PropertySetupCollector2< C extends Configuration> {
  public final C template ;
  private Method lastAccessed = null ;
  private final Configuration.PropertySetup2.SetupAcceptor setupAcceptor ;

  public PropertySetupCollector2(
      final Class< C > configurationClass,
      final Configuration.PropertySetup2.SetupAcceptor setupAcceptor
  ) {
    this.setupAcceptor = setupAcceptor ;
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
            lastAccessed = method ;
            return null ;
          }
        }
    ) ;
  }

  /**
   * Don't call outside of {@link io.github.caillette.wrench.source.ObjectSource ()}.
   */
  public final < T > Configuration.PropertySetup2< C, T > on(
      @SuppressWarnings( "UnusedParameters" ) T methodCallResult
  ) {
    return new Configuration.PropertySetup2<>( lastAccessed, setupAcceptor ) ;
  }

}
