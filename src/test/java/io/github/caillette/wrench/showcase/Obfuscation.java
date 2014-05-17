package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.ConfigurationTools;
import org.junit.Ignore;
import org.junit.Test;

import static io.github.caillette.wrench.ConfigurationTools.newFactory;
import static io.github.caillette.wrench.Sources.newSource;

public class Obfuscation {

  public interface Obfuscated extends Configuration {
    @Configuration.Annotations.Obfuscator( "(?<=^.*:).*" )
    String credential() ;
  }

  @Test
  @Ignore( "Not implemented" )
  public void test() throws Exception {
    final Configuration.Factory< Obfuscated > factory = newFactory( Obfuscated.class ) ;
    System.out.println( "Properties: " + factory.properties() ) ;

    final Obfuscated obfuscated = factory.create( newSource( "credential=foo:bar" ) ) ;
    final Configuration.Support< Obfuscated > support = ConfigurationTools.support( obfuscated ) ;

    final Configuration.Support< Obfuscated > support1 = ConfigurationTools.support( obfuscated ) ;
    final String loginPasswrd = obfuscated.credential() ;
    support1.lastAccessed() ;



  }
}
