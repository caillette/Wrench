package io.github.caillette.wrench;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Used by {@link io.github.caillette.wrench.TemplateBasedFactory#tweak(Configuration)}.
 */
public final class TweakedValue {

  public final Object resolvedValue ;
  public final String stringValue ;

  public TweakedValue( final Object resolvedValue ) {
    this( resolvedValue, "" + resolvedValue ) ;
  }

  public TweakedValue( final Object resolvedValue, final String stringValue ) {
    this.resolvedValue = resolvedValue ;
    this.stringValue = checkNotNull( stringValue ) ;
  }
}
