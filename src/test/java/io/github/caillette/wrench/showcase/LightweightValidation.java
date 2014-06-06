package io.github.caillette.wrench.showcase;

import com.google.common.collect.ImmutableSet;
import io.github.caillette.wrench.*;
import org.junit.Test;

import static io.github.caillette.wrench.Validator.Accumulator;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class LightweightValidation {

  public interface Validated extends Configuration {
    int x() ;
    int y() ;
  }

  @Test
  public void test() throws Exception {
    final Configuration.Factory< Validated > factory
        = new TemplateBasedFactory< Validated >( Validated.class )
    {
      @Override
      protected ImmutableSet<Validator.Bad< Validated >> validate(
          final Validated configuration
      ) {
        final Accumulator< Validated > accumulator = new Accumulator<>( configuration ) ;
        // The verify*( ... ) methods remember last accessed property and prepend it to the message.
        accumulator.verify( configuration.x() > 0, "Must be > 0" ) ;
        accumulator.verify( configuration.y() > 0, "Must be > 0" ) ;
        // The just*( ... ) methods don't affect content of the message.
        accumulator.justVerify( configuration.x() + configuration.y() < 10, "Sum must be < 10" ) ;
        return accumulator.done() ;
      }
    } ;

    try {
      factory.create( Sources.newSource( "x = 12", "y = -1" ) ) ;
      fail( "Should have thrown an exception" ) ;
    } catch ( ConfigurationException e ) {
      assertThat( e.getMessage() ).contains( "[ y -> -1 ] Must be > 0" ) ;
      assertThat( e.getMessage() ).contains( "Sum must be < 10" ) ;
    }
  }
}
