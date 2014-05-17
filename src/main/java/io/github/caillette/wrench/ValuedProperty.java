package io.github.caillette.wrench;

import static com.google.common.base.Preconditions.checkNotNull;

class ValuedProperty {

  public final Configuration.Property property ;
  public final Configuration.Source source ;
  public final String stringValue ;
  public final Object resolvedValue ;

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
  }
}
