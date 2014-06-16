package io.github.caillette.wrench;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Used by {@link io.github.caillette.wrench.TemplateBasedFactory#tweak(Configuration)}.
 */
public final class TweakedValue {

  public final Object resolvedValue ;
  public final String stringValue ;

  public TweakedValue( Object resolvedValue, String stringValue ) {
    this.stringValue = checkNotNull( stringValue ) ;
    this.resolvedValue = resolvedValue ;
  }
}
