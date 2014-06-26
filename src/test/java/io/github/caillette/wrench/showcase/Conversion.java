package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.Converters;
import io.github.caillette.wrench.TemplateBasedFactory;
import org.junit.Test;

import java.util.regex.Pattern;

import static io.github.caillette.wrench.Configuration.Factory;
import static io.github.caillette.wrench.Sources.newSource;
import static org.assertj.core.api.Assertions.assertThat;

public class Conversion {

  public interface Converted extends Configuration {
    Pattern pattern() ;
  }

  @Test
  public void test() throws Exception {
    final Factory< Converted > factory = new TemplateBasedFactory< Converted >( Converted.class ) {
      @Override
      protected void initialize() {
        property( using.pattern() ).converter( Converters.from( Pattern::compile ) ) ;
      }
    } ;
    final Converted configuration = factory.create( newSource( "pattern = .*" ) ) ;

    assertThat( configuration.pattern().pattern() ).isEqualTo( ".*" ) ;
  }
}
