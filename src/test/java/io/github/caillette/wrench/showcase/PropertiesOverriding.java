package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.ConfigurationTools;
import io.github.caillette.wrench.Sources;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class PropertiesOverriding {

  public interface Simple extends Configuration {
    int n() ;
  }

  @Test
  public void test() throws Exception {
    final Configuration.Factory< Simple > factory = ConfigurationTools.newFactory( Simple.class ) ;
    final Simple configuration = factory.create(
        // Default value would act as a Source declared first.
        Sources.newSource( "n = 1" ),
        Sources.newSource( "" ),
        Sources.newSource( "n = 2" ) // Last wins.
    ) ;

    assertThat( configuration.n( ) ).isEqualTo( 2 ) ;
  }
}
