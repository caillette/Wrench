package io.github.caillette.wrench;

/**
 * Thrown if there is something wrong in a {@link Configuration.Source} regarding the
 * definition enforced by a {@link Configuration.Factory}.
 *
 */
public class DeclarationException extends ConfigurationException {

  public DeclarationException( final String message ) {
    super( message ) ;
  }

  public DeclarationException( final String message, final Throwable cause ) {
    super( message, cause ) ;
  }

  public DeclarationException( final Iterable< ConfigurationException > causes ) {
    super( causes ) ;
  }

  public static DeclarationException throwWith( Iterable<Validation.Bad> causes )
      throws DeclarationException
  {
    throw new DeclarationException( singleMessageFromInfrigements( causes ) ) ;
  }

}
