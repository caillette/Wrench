package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.Sources;
import io.github.caillette.wrench.TemplateBasedFactory;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class DefaultValues {

  public interface WithDefaults extends Configuration {
    Integer number() ;
    String string() ;
  }

  @Test
  public void test() throws Exception {
    final Configuration.Factory< WithDefaults > factory
        = new TemplateBasedFactory< WithDefaults >( WithDefaults.class )
    {
      @Override
      protected void initialize() {
        property( using.number() ).defaultValue( 1 ) ;
        property( using.string() ).defaultValue( null ) ;
      }
    } ;
    final WithDefaults configuration = factory.create( Sources.newSource( "" ) ) ;

    assertThat( configuration.number( ) ).isEqualTo( 1 ) ;
    assertThat( configuration.string( ) ).isNull( ) ;
  }
}
