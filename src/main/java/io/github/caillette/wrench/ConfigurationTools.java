package io.github.caillette.wrench;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static io.github.caillette.wrench.Configuration.*;

/**
 * Utility methods to create {@link Configuration.Factory} and {@link Inspector}
 * objects.
 */
public final class ConfigurationTools {

  public static < C extends Configuration > Factory< C > newFactory(
      final Class< C > configurationClass
  ) {
    return new TemplateBasedFactory< C >( configurationClass ) { } ;
  }


  /**
   * Creates a new {@link Inspector}.
   * Warning: during its lifetime, an {@link Inspector} keeps track of every method call on the
   * {@link Configuration} object. For this reason, a too broadly scoped {@link Inspector}
   * can lead to excessive memory consumption.
   */
  public static < C extends Configuration > Inspector< C > newInspector(
      final C configuration
  ) {
    final ConfigurationInspector.InspectorEnabled inspectorEnabled
        = ( ConfigurationInspector.InspectorEnabled ) configuration ;
    final ThreadLocal< Map< Inspector, List< Property > > > inspectorsThreadLocal
        = inspectorEnabled.$$inspectors$$() ;
    Map< Inspector, List< Property > > inspectors = inspectorsThreadLocal.get() ;
    if( inspectors == null ) {
      inspectors = new WeakHashMap<>() ;
      inspectorsThreadLocal.set( inspectors ) ;
    }
    final List< Property > accessedProperties = new ArrayList<>() ;

    @SuppressWarnings( "unchecked" )
    final ConfigurationInspector< C > inspector = new ConfigurationInspector(
        inspectorEnabled.$$properties$$(), accessedProperties ) ;
    inspectors.put(
        inspector,
        accessedProperties
    ) ;
    return inspector ;
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
