package io.github.caillette.wrench;

import static io.github.caillette.wrench.Configuration.Factory;

/**
 * Utility methods to create {@link Configuration.Factory} and {@link Configuration.Inspector}
 * objects.
 */
public final class ConfigurationTools {

  public static < C extends Configuration > Factory< C > newFactory(
      final Class< C > configurationClass
  ) {
    return new TemplateBasedFactory< C >( configurationClass ) { } ;
  }

  @SuppressWarnings( "unchecked" )
  public static < C extends Configuration > Configuration.Inspector< C > inspector(
      final C configuration
  ) {
    return ( ( ConfigurationInspector.InspectorEnabled ) configuration ).$$inspector$$() ;
  }
  
// ===============
// Our own cooking
// ===============


  public static String getNiceName( final Class originClass ) {
    String className = originClass.getSimpleName() ;
    Class enclosingClass = originClass.getEnclosingClass() ;
    while( enclosingClass != null ) {
      className = enclosingClass.getSimpleName() + "$" + className ;
      enclosingClass = enclosingClass.getEnclosingClass() ;
    }
    return className ;
  }


}
