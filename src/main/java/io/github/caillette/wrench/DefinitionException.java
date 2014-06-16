package io.github.caillette.wrench;

/**
 * Thrown if there is an inconsistency in the interface defining the {@link Configuration}
 * (this includes the annotations).
 */
public class DefinitionException extends RuntimeException {
  public DefinitionException( final String message ) {
    super( message );
  }

}
