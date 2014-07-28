package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.Sources;
import io.github.caillette.wrench.TemplateBasedFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MixInterfaces {

  public interface WithHeight {
    int height() ;
  }

  public interface WithWidth {
    int width() ;
  }

  public interface WithDimension extends WithWidth, WithHeight {
    String unit() ;
  }

  public interface MyConfiguration extends Configuration, WithDimension {
    String name() ;
  }

  @Test
  public void test() throws Exception {
    final Configuration.Factory<MyConfiguration> factory
        = new TemplateBasedFactory< MyConfiguration >( MyConfiguration.class ) {
      @Override
      protected void initialize() {
        property( using.unit() ).defaultValue( "cm" ) ;
      }
    } ;
    final MyConfiguration configuration = factory.create(
        Sources.newSource( "height = 11", "width = 22", "name=rectangle" ) ) ;

    assertThat( configuration.height( ) ).isEqualTo( 11 ) ;
    assertThat( configuration.width( ) ).isEqualTo( 22 ) ;
    assertThat( configuration.unit( ) ).isEqualTo( "cm" ) ;
    assertThat( configuration.name( ) ).isEqualTo( "rectangle" ) ;
  }
}
