package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.ConfigurationTools;
import io.github.caillette.wrench.Sources;
import io.github.caillette.wrench.TemplateBasedFactory;
import org.junit.Test;

import static io.github.caillette.wrench.Configuration.Inspector;
import static org.fest.assertions.Assertions.assertThat;

public class Inspection {

  public interface Simple extends Configuration {
    Integer number() ;
    String string() ;
  }

  @Test
  public void test() throws Exception {
    final Configuration.Factory< Simple > factory
        = new TemplateBasedFactory<Simple>( Simple.class )
    {
      @Override
      protected void initialize() {
        on( template.number() ).defaultValue( 1 ).documentation( "Some number." ) ;
      }
    } ;
    final Simple configuration = factory.create( Sources.newSource( "string = foo" ) ) ;

    final Inspector< Simple > inspector = ConfigurationTools.inspector( configuration ) ;
    configuration.number() ; {
      assertThat( inspector.lastAccessed().documentation() ).isEqualTo( "Some number." ) ;
      assertThat( inspector.lastAccessed().defaultValue() ).isEqualTo( 1 ) ;
      assertThat( inspector.usingDefault( inspector.lastAccessed() ) ).isTrue() ;
    }
    configuration.string() ; {
      assertThat( inspector.lastAccessed().documentation() ).isEqualTo( "" ) ;
      assertThat( inspector.lastAccessed().defaultValue() ).isNull() ;
      assertThat( inspector.usingDefault( inspector.lastAccessed() ) ).isFalse() ;
    }
  }
}
