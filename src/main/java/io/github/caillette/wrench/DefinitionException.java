package io.github.caillette.wrench;

/**
 * @author Laurent Caillette
 */
public class DefinitionException extends RuntimeException {
  public DefinitionException( String message ) {
    super( message );
  }

  public DefinitionException( Exception e ) {
    super( e ) ;
  }
}
