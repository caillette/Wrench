package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.ConfigurationException;
import io.github.caillette.wrench.ConfigurationTools;
import org.junit.Test;

import static io.github.caillette.wrench.Sources.newSource;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class FailFastOnUndefinedProperty {

  public interface Simple extends Configuration {
    @SuppressWarnings( "UnusedDeclaration" )
    String string() ;
  }

  @Test
  public void test() throws Exception {
    final Configuration.Factory< Simple > factory
        = ConfigurationTools.newFactory( Simple.class ) ;

    try {
      factory.create( newSource( "" ) ) ;
      fail( "Should have thrown an exception" ) ;
    } catch ( ConfigurationException e ) {
      assertThat( e.getMessage() ).contains( "string -> 'null' - No value set - No source" ) ;
    }
  }

}
