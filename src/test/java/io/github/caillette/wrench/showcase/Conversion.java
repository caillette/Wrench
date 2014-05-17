package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static io.github.caillette.wrench.Configuration.Annotations.Convert;
import static io.github.caillette.wrench.Configuration.Converter;
import static io.github.caillette.wrench.Configuration.Factory;
import static io.github.caillette.wrench.ConfigurationTools.newFactory;
import static io.github.caillette.wrench.Sources.newSource;
import static org.fest.assertions.Assertions.assertThat;

public class Conversion {

  public interface Converted extends Configuration {
    @Convert( IntoPattern.class )
    Pattern pattern() ;
  }

  public static class IntoPattern implements Converter< Pattern > {
    @Override
    public Pattern convert( Method definingMethod, String input ) {
      return Pattern.compile( input ) ;
    }
  }

  @Test
  public void test() throws Exception {
    final Factory< Converted > factory = newFactory( Converted.class ) ;
    final Converted configuration = factory.create( newSource( "pattern = .*" ) ) ;

    assertThat( configuration.pattern().pattern() ).isEqualTo( ".*" ) ;
  }
}
