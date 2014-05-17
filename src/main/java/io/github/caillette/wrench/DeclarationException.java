package io.github.caillette.wrench;

/**
 * Thrown if there is something wrong in a {@link Configuration.Source} regarding the
 * definition enforced by a {@link Configuration.Factory}.
 *
 */
public class DeclarationException extends ConfigurationException {
  public DeclarationException( String message ) {
    super( message ) ;
  }
}
