package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.source.ObjectSource;
import org.junit.Test;

import static io.github.caillette.wrench.ConfigurationTools.newFactory;
import static org.fest.assertions.Assertions.assertThat;

public class RawObjectsAsDefault {

  public interface Simple extends Configuration {
    Integer number() ;
  }

  @Test
  public void test() throws Exception {
    final Configuration.Factory< Simple > factory = newFactory( Simple.class ) ;
    final ObjectSource< Simple > objectSource = new ObjectSource< Simple >( factory ) {
      @Override
      protected void record( Collector collector, Simple template ) {
        // The template captures method call.
        // Creating another object to call the 'put' method on enforces strong typing.
        collector.with( template.number() ).put( INTEGER ) ;
      }
    } ;
    final Simple configuration = factory.create( objectSource ) ;

    assertThat( configuration.number() ).isSameAs( INTEGER ) ;
  }

  private static final Integer INTEGER = 123 ;

}
