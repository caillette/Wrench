package io.github.caillette.wrench;

/**
 * Base class for exceptions thrown when something goes bad.
 */
public class ConfigurationException extends Exception {

  public ConfigurationException( String message ) {
    super( message ) ;
  }

  public ConfigurationException( Iterable< ConfigurationException > causes ) {
    super( "Exception list: " + asSingleMessage( causes ) ) ;
  }

  private static String asSingleMessage( Iterable< ConfigurationException > causes ) {
    final StringBuilder stringBuilder = new StringBuilder() ;
    for( final ConfigurationException configurationException : causes ) {
      stringBuilder.append( "\n    " ) ;
      stringBuilder.append( configurationException.getClass().getSimpleName() ) ;
      stringBuilder.append( " - " ) ;
      stringBuilder.append( configurationException.getMessage() ) ;
    }
    return stringBuilder.toString() ;
  }

  public ConfigurationException( String message, Throwable cause ) {
    super( message, cause );
  }

  public ConfigurationException( Throwable cause ) {
    super( cause );
  }
}
