package io.github.caillette.wrench.showcase;

import com.google.common.collect.ImmutableSet;
import io.github.caillette.wrench.*;
import org.junit.Test;

import static io.github.caillette.wrench.Configuration.Annotations.ValidateWith;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class LightweightValidation {

  @ValidateWith( BoundsAndRelationships.class )
  public interface Validated extends Configuration {
    int x() ;
    int y() ;
  }

  public static class BoundsAndRelationships implements Validator< Validated > {
    @Override
    public ImmutableSet< Infrigement< Validated > > validate( Validated configuration ) {
      final Accumulator< Validated > accumulator = new Accumulator<>( configuration ) ;
      // The smart*( ... ) methods remember last accessed property and add it to the message.
      accumulator.smartVerify( configuration.x() > 0, "Must be > 0" ) ;
      accumulator.smartVerify( configuration.y() > 0, "Must be > 0" ) ;
      accumulator.verify( configuration.x() + configuration.y() < 10, "Sum must be < 10" ) ;
      return accumulator.done() ;
    }
  }


  @Test
  public void test() throws Exception {
    final Configuration.Factory< Validated > factory
        = ConfigurationTools.newFactory( Validated.class ) ;

    try {
      factory.create( Sources.newSource( "x = 12", "y = -1" ) ) ;
      fail( "Should have thrown an exception" ) ;
    } catch ( ConfigurationException e ) {
      assertThat( e.getMessage() ).contains( "y -> '-1' - Must be > 0" ) ;
      assertThat( e.getMessage() ).contains( "Sum must be < 10" ) ;
    }
  }
}
