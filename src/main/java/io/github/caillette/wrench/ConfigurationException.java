package io.github.caillette.wrench;

/**
 * Base class for exceptions thrown when something goes bad.
 */
public class ConfigurationException extends Exception {

  public ConfigurationException( String message ) {
    super( message ) ;
  }

  public ConfigurationException( Iterable< ConfigurationException > causes ) {
    super( "Exception list: " + singleMessageFromExceptions( causes ) ) ;
  }

  private static String singleMessageFromExceptions( Iterable< ConfigurationException > causes ) {
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

  protected static String singleMessageFromInfrigements( Iterable<Validator.Bad> causes ) {
    final StringBuilder stringBuilder = new StringBuilder() ;
    for( final Validator.Bad bad : causes ) {
      stringBuilder.append( "\n    " ) ;
      if( bad.property != null ) {
        stringBuilder.append( "[ " ) ;
        stringBuilder.append( bad.property.name() ) ;
        stringBuilder.append( " -> " ) ;
        stringBuilder.append( bad.propertyValue ) ;
        stringBuilder.append( " ] " ) ;
      }
      stringBuilder.append( bad.message ) ;
      if( bad.source == null ) {
        stringBuilder.append( " - No source " ) ;
      } else {
        stringBuilder.append( " - Source: " ) ;
        stringBuilder.append( bad.source.sourceName() ) ;
      }
    }
    return stringBuilder.toString() ;
  }
}
