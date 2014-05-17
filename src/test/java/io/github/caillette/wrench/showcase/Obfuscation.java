package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import org.junit.Test;

import static io.github.caillette.wrench.Configuration.Annotations.Obfuscator;
import static io.github.caillette.wrench.ConfigurationTools.newFactory;
import static io.github.caillette.wrench.ConfigurationTools.support;
import static io.github.caillette.wrench.Sources.newSource;
import static org.fest.assertions.Assertions.assertThat;

public class Obfuscation {

  public interface Obfuscated extends Configuration {
    @Obfuscator( "(?<=^.*:).*" )
    String credential() ;
  }

  @Test
  public void test() throws Exception {
    final Configuration.Factory< Obfuscated > factory = newFactory( Obfuscated.class ) ;
    System.out.println( "Properties: " + factory.properties() ) ;

    final Obfuscated obfuscated = factory.create( newSource( "credential = foo:bar" ) ) ;
    final Configuration.Support< Obfuscated > support = support( obfuscated ) ;

    assertThat( obfuscated.credential() ).isEqualTo( "foo:bar" ) ;
    assertThat( support.safeValueOf( support.lastAccessed(), "[undisclosed]" ) )
        .isEqualTo( "foo:[undisclosed]" ) ;


  }
}
