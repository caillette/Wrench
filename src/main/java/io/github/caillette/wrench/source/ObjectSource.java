package io.github.caillette.wrench.source;

import com.google.common.collect.ImmutableMap;
import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.ConfigurationTools;
import io.github.caillette.wrench.PropertySetupCollector;

import java.lang.reflect.Method;

/**
 * Sets default values with plain objects and static typing.
 * This is an alternative to {@link Configuration.Annotations.DefaultValue} annotation which
 * implies parsing.
 */
public abstract class ObjectSource< C extends Configuration >
    implements Configuration.Source.Raw< C >
{

  private final PropertySetupCollector< C > collector ;
  protected final C template ;

  public ObjectSource( Configuration.Factory< C > factory ) {
    final ImmutableMap.Builder< Method, Configuration.Property< C > > builder
        = ImmutableMap.builder() ;
    for( final Configuration.Property< C > property : factory.properties().values() ) {
      builder.put( property.declaringMethod(), property ) ;
    }
    collector = new PropertySetupCollector<>( factory, builder.build() ) ;
    template = collector.template ;
  }

  protected final < T > Configuration.PropertySetup.DefaultValue< C, T > on(
      final T templateCallResult
  ) {
    return collector.on( templateCallResult ) ;
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
