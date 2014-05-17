package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.ConfigurationException;
import io.github.caillette.wrench.ConfigurationTools;
import org.junit.Test;

import static io.github.caillette.wrench.Sources.newSource;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class FailFastOnUnknownPropertyName {

  public interface Empty extends Configuration { }

  @Test
  public void test() throws Exception {
    final Configuration.Factory< Empty > factory
        = ConfigurationTools.newFactory( Empty.class ) ;
    try {
      factory.create( newSource( "foo=bar" ) ) ;
      fail( "Should have thrown an exception" ) ;
    } catch ( ConfigurationException e ) {
      assertThat( e.getMessage() ).contains( "Unknown property name 'foo'" ) ;
    }
  }

}
