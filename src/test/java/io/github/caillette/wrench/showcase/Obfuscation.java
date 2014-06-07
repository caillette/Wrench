package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.TemplateBasedFactory;
import org.junit.Test;

import java.util.regex.Pattern;

import static io.github.caillette.wrench.ConfigurationTools.newInspector;
import static io.github.caillette.wrench.Sources.newSource;
import static org.fest.assertions.Assertions.assertThat;

public class Obfuscation {

  public interface Obfuscated extends Configuration {
    String credential() ;
  }

  @Test
  public void test() throws Exception {
    final Configuration.Factory< Obfuscated > factory
        = new TemplateBasedFactory< Obfuscated >( Obfuscated.class )
    {
      @Override
      protected void initialize() {
        property( using.credential() ).obfuscator( Pattern.compile( "(?<=^.*:).*" ) ) ;
      }
    } ;
    System.out.println( "Properties: " + factory.properties() ) ;

    final Obfuscated obfuscated = factory.create( newSource( "credential = foo:bar" ) ) ;
    final Configuration.Inspector< Obfuscated > inspector = newInspector( obfuscated ) ;

    assertThat( obfuscated.credential() ).isEqualTo( "foo:bar" ) ;
    assertThat( inspector.safeValueOf( inspector.lastAccessed().get( 0 ), "[undisclosed]" ) )
        .isEqualTo( "foo:[undisclosed]" ) ;


  }
}
