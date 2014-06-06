package io.github.caillette.wrench.showcase;

import com.google.common.collect.ImmutableMap;
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

    final ImmutableMap< String, Configuration.Property< Simple > > properties
        = inspector.properties() ;
    assertThat( properties ).hasSize( 2 ) ;
    assertThat( properties.get( "number" ).name() ).isEqualTo( "number" ) ;
    assertThat( properties.get( "string" ).name() ).isEqualTo( "string" ) ;

    configuration.number() ;
    {
      final Configuration.Property< Simple > property = inspector.lastAccessed() ;
      assertThat( property.name() ).isEqualTo( "number" ) ;
      assertThat( property.documentation() ).isEqualTo( "Some number." ) ;
      assertThat( property.defaultValue() ).isEqualTo( 1 ) ;
      assertThat( inspector.usingDefault( property ) ).isTrue() ;
    }
    configuration.string() ; {
      final Configuration.Property< Simple > property = inspector.lastAccessed() ;
      assertThat( property.name() ).isEqualTo( "string" ) ;
      assertThat( property.documentation() ).isEqualTo( "" ) ;
      assertThat( property.defaultValue() ).isNull() ;
      assertThat( inspector.usingDefault( property ) ).isFalse() ;
    }
  }
}
