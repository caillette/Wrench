package io.github.caillette.wrench;

/**
 * Thrown if values read from {@link Configuration.Source}s infrige some validation rules.
 */
public class ValidationException extends ConfigurationException {

  public ValidationException( Iterable<Validation.Bad> causes ) {
    super( singleMessageFromInfrigements( causes ) ) ;
  }


}
