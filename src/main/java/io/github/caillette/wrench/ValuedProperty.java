package io.github.caillette.wrench;

import static com.google.common.base.Preconditions.checkNotNull;

class ValuedProperty {

  public final Configuration.Property property ;
  public final Configuration.Source source ;
  public final String stringValue ;
  public final Object resolvedValue ;
  public final boolean usingDefault ;

  @Deprecated
  public ValuedProperty(
      final Configuration.Property property,
      final Configuration.Source source,
      final String stringValue,
      final Object resolvedValue
  ) {
    this.property = checkNotNull( property ) ;
    this.source = checkNotNull( source ) ;
    this.stringValue = stringValue ;
    this.resolvedValue = resolvedValue ;
    this.usingDefault = false ;
  }

  @Deprecated
  public ValuedProperty(
      final Configuration.Property property,
      final Configuration.Source source,
      final Object resolvedValue
  ) {
    this.property = checkNotNull( property ) ;
    this.source = checkNotNull( source ) ;
    this.stringValue = resolvedValue == null ? "<not-set>" : resolvedValue.toString() ;
    this.resolvedValue = resolvedValue ;
    this.usingDefault = false ;
  }

  public ValuedProperty(
      final Configuration.Property property,
      final Configuration.Source source,
      final Object resolvedValue,
      final boolean usingDefault
  ) {
    this.property = checkNotNull( property ) ;
    this.source = checkNotNull( source ) ;
    this.stringValue = resolvedValue == null ? "<not-set>" : resolvedValue.toString() ;
    this.resolvedValue = resolvedValue ;
    this.usingDefault = usingDefault ;
  }

  public static final Object NULL_VALUE = new Object() {
    @Override
    public String toString() {
      return ValuedProperty.class.getSimpleName() + "#NULL_VALUE{}" ;
    }
  } ;
}
