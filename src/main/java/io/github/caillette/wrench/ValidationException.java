package io.github.caillette.wrench;

/**
 * @author Laurent Caillette
 */
public class ValidationException extends ConfigurationException {

  public ValidationException( Iterable< Validator.Infrigement > causes ) {
    super( asSingleMessage( causes ) ) ;
  }

  private static String asSingleMessage( Iterable< Validator.Infrigement > causes ) {
    final StringBuilder stringBuilder = new StringBuilder() ;
    for( final Validator.Infrigement infrigement : causes ) {
      stringBuilder.append( "\n    " ) ;
      stringBuilder.append( infrigement.property.name() ) ;
      stringBuilder.append( " -> '" ) ;
      stringBuilder.append( infrigement.propertyValue ) ;
      stringBuilder.append( "' - " ) ;
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
