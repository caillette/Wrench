package io.github.caillette.wrench;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Need to keep package-private because we do tricks with {@link #resolvedValue} and
 * {@link #NULL_VALUE}.
 */
class ValuedProperty {

  public final Configuration.Property property ;
  public final Configuration.Source source ;
  public final String stringValue ;
  public final Object resolvedValue ;
  public final boolean usingDefault ;

  public ValuedProperty( final Configuration.Property property ) {
    this.property = checkNotNull( property ) ;
    this.source = Sources.UNDEFINED ;
    this.stringValue = "<not-set>"  ;
    this.resolvedValue = NO_VALUE ;
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
      return ValuedProperty.class.getSimpleName() + "#NULL_VALUE{}";
    }
  } ;

  public static final Object NO_VALUE = new Object() {
    @Override
    public String toString() {
      return ValuedProperty.class.getSimpleName() + "#NO_VALUE{}" ;
    }
  } ;
}
