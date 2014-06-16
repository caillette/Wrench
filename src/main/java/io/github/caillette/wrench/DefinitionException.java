package io.github.caillette.wrench;

/**
 * Thrown if there is an inconsistency when creating a {@link Configuration.Factory}.
 */
public class DefinitionException extends RuntimeException {
  public DefinitionException( final String message ) {
    super( message );
  }

}
