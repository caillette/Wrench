package io.github.caillette.wrench;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Laurent Caillette
 */
class ConfigurationSupport< C extends Configuration > implements Configuration.Support< C > {

  private final ImmutableSortedMap< String, ValuedProperty> properties ;
  private final ThreadLocal<Configuration.Property< C >> lastAccessed ;

  public ConfigurationSupport(
      final ImmutableSortedMap< String, ValuedProperty> properties,
      final ThreadLocal<Configuration.Property< C >> lastAccessed
  ) {
    this.properties = checkNotNull( properties ) ;
    this.lastAccessed = checkNotNull( lastAccessed ) ;
  }

  interface SupportEnabled {
    Configuration.Support $$support$$() ;
  }

// =======
// Support
// =======

  @Override
  public ImmutableMap< String, Configuration.Property< C >> properties() {
    final ImmutableMap.Builder< String, Configuration.Property< C >> builder
        = ImmutableMap.builder() ;
    for( final ValuedProperty valuedProperty : properties.values() ) {
      builder.put( valuedProperty.property.name(), valuedProperty.property ) ;
    }
    return builder.build() ;
  }

  @Override
  public boolean usingDefault( Configuration.Property< C > property ) {
    return valuedSlot( property ).source instanceof PropertyDefaultSource ;
  }

  @Override
  public Configuration.Source sourceOf( Configuration.Property< C > property ) {
    return valuedSlot( property ).source ;
  }

  @Override
  public String stringValueOf( Configuration.Property< C > property ) {
    return valuedSlot( property ).stringValue ;
  }

  @Override
  public Configuration.Property< C > lastAccessed() {
    return lastAccessed.get() ;
  }

  // ======
// Boring
// ======

  private ValuedProperty valuedSlot( Configuration.Property< C > property ) {
    final ValuedProperty valuedProperty = properties.get( property.name() ) ;
    if( valuedProperty == null ) {
      throw new IllegalArgumentException( "Unknown: " + property ) ;
    }
    return valuedProperty;
  }


}
