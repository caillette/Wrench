package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.ConfigurationTools;
import io.github.caillette.wrench.Sources;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class SimplestUsage {

  public interface Simple extends Configuration {
    Integer number() ;
    String string() ;
  }

  @Test
  public void test() throws Exception {
    final Configuration.Factory< Simple > factory = ConfigurationTools.newFactory( Simple.class ) ;
    final Simple configuration = factory.create( Sources.newSource(
        "number = 123",
        "string = foo"
    ) ) ;

    assertThat( configuration.number() ).isEqualTo( 123 ) ;
    assertThat( configuration.string() ).isEqualTo( "foo" ) ;
    assertThat( configuration.toString() )
        .isEqualTo( "SimplestUsage$Simple{number=123; string=foo}" ) ;
  }

}
