package io.github.caillette.wrench;

import org.junit.Test;

import static io.github.caillette.wrench.Configuration.Inspector;
import static io.github.caillette.wrench.Configuration.Property;
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

    final Inspector< WithDefaults > inspector = ConfigurationTools.newInspector( configuration ) ;
    assertThat( configuration.string() ).isNull() ;
    final Property< WithDefaults > stringProperty = inspector.lastAccessed().get( 0 ) ;
    assertThat( inspector.stringValueOf( stringProperty ) ).isNull() ;
    assertThat( ConfigurationTools.lastValueAsString( inspector ) ).isNull() ;

  }
}
