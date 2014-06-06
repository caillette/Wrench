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

  protected static String singleMessageFromInfrigements( Iterable< Validator.Infrigement > causes ) {
    final StringBuilder stringBuilder = new StringBuilder() ;
    for( final Validator.Infrigement infrigement : causes ) {
      stringBuilder.append( "\n    " ) ;
      if( infrigement.property != null ) {
        stringBuilder.append( infrigement.property.name() ) ;
        stringBuilder.append( " -> '" ) ;
        stringBuilder.append( infrigement.propertyValue ) ;
        stringBuilder.append( "' - " ) ;
      }
      stringBuilder.append( infrigement.message ) ;
      if( infrigement.source == null ) {
        stringBuilder.append( " - No source " ) ;
      } else {
        stringBuilder.append( " - Source: " ) ;
        stringBuilder.append( infrigement.source.sourceName() ) ;
      }
    }
    return stringBuilder.toString() ;
  }
}
