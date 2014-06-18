package io.github.caillette.wrench;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NullityByDefault {

  public interface WithDefaults extends Configuration {
    String string() ;
  }

  @Test
  public void test() throws Exception {
    final Configuration.Factory< WithDefaults > factory
        = new TemplateBasedFactory< WithDefaults >( WithDefaults.class )
    {
      @Override
      protected void initialize() {
        property( using.string() ).maybeNull() ;
      }
    } ;
    final WithDefaults configuration = factory.create( Sources.newSource( "" ) ) ;

    assertThat( configuration.string() ).isNull() ;
  }
}
