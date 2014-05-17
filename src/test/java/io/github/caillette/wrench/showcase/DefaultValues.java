package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.ConfigurationTools;
import io.github.caillette.wrench.Sources;
import org.junit.Test;

import static io.github.caillette.wrench.Configuration.Annotations.DefaultNull;
import static io.github.caillette.wrench.Configuration.Annotations.DefaultValue;
import static org.fest.assertions.Assertions.assertThat;

public class DefaultValues {

  public interface WithDefaults extends Configuration {
    @DefaultValue( "1" )
    int number() ;
    @DefaultNull // Compiler refuses @DefaultValue( null )
    String string() ;
  }

  @Test
  public void test() throws Exception {
    final Configuration.Factory< WithDefaults > factory
        = ConfigurationTools.newFactory( WithDefaults.class ) ;
    final WithDefaults configuration = factory.create( Sources.newSource( "" ) ) ;

    assertThat( configuration.number( ) ).isEqualTo( 1 ) ;
    assertThat( configuration.string( ) ).isNull( ) ;
  }
}
