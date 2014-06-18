package io.github.caillette.wrench;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.github.caillette.wrench.Configuration.Inspector;
import static io.github.caillette.wrench.Configuration.Property;

class ConfigurationInspector< C extends Configuration > implements Inspector< C > {

  private final Configuration.Factory< C > factory ;

  private final ImmutableSortedMap< String, ValuedProperty > valuedProperties ;

  /**
   * Mutable object.
   */
  private final List< Property< C > > lastAccessed ;

  public ConfigurationInspector(
      final Configuration.Factory< C > factory,
      final ImmutableSortedMap< String, ValuedProperty > valuedProperties,
      final List< Property< C > > lastAccessed
  ) {
    this.factory = checkNotNull( factory ) ;
    this.valuedProperties = checkNotNull( valuedProperties ) ;
    this.lastAccessed = checkNotNull( lastAccessed ) ;
  }

  public ValuedProperty valuedProperty( final Property< C > property ) {
    return valuedProperties.get( property.name() ) ;
  }

  interface InspectorEnabled {
    ThreadLocal< Map< Inspector, List< Property > > > $$inspectors$$() ;
    ImmutableSortedMap< String, ValuedProperty > $$properties$$() ;
    Configuration.Factory $$factory$$() ;
  }

// =======
// Support
// =======

  @Override
  public ImmutableMap< String, Property< C > > properties() {
    final ImmutableMap.Builder< String, Property< C > > builder
        = ImmutableMap.builder() ;
    for( final ValuedProperty valuedProperty : valuedProperties.values() ) {
      builder.put( valuedProperty.property.name(), valuedProperty.property ) ;
    }
    return builder.build() ;
  }

  @Override
  public boolean usingDefault( final Property< C > property ) {
    return valuedSlot( property ).usingDefault ;
  }

  @Override
  public Configuration.Source sourceOf( final Property< C > property ) {
    return valuedSlot( property ).source ;
  }

  @Override
  public String stringValueOf( final Property< C > property ) {
    final ValuedProperty valuedProperty = valuedProperties.get( property.name() ) ;
    return valuedProperty == null ? null : valuedProperty.stringValue ;
  }

  @Override
  public String safeValueOf(
      final Property< C > property,
      final String replacement
  ) {
    final ValuedProperty valuedProperty = valuedSlot( property ) ;
    final String stringValue ;
    if( valuedProperty.usingDefault ) {
      stringValue = valuedProperty.property.defaultValueAsString() ;
    } else {
      stringValue = valuedProperty.stringValue ;
    }
    if( stringValue == null ) {
      return null ;
    } else {
      if( property.obfuscator() == null ) {
        return stringValue ;
      } else {
        return property.obfuscator().obfuscate( stringValue, replacement ) ;
      }
    }
  }

  @Override
  public ImmutableList< Property< C > > lastAccessed() {
    return ImmutableList.copyOf( lastAccessed ) ;
  }

  @Override
  public void clearLastAccessed() {
    lastAccessed.clear() ;
  }

  @Override
  public Configuration.Factory< C > factory() {
    return factory ;
  }

// ======
// Boring
// ======

  private ValuedProperty valuedSlot( final Property< C > property ) {
    final ValuedProperty valuedProperty = valuedProperties.get( property.name() ) ;
    if( valuedProperty == null ) {
      throw new IllegalArgumentException( "Unknown: " + property ) ;
    }
    return valuedProperty;
  }


}
