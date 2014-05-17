package io.github.caillette.wrench;

/**
 * Thrown if values read from {@link Configuration.Source}s infrige some validation rules.
 */
public class ValidationException extends ConfigurationException {

  public ValidationException( Iterable< Validator.Infrigement > causes ) {
    super( asSingleMessage( causes ) ) ;
  }

  private static String asSingleMessage( Iterable< Validator.Infrigement > causes ) {
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
