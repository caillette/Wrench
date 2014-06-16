package io.github.caillette.wrench.showcase;

import com.google.common.collect.ImmutableMap;
import io.github.caillette.wrench.*;
import org.junit.Test;

import static io.github.caillette.wrench.Configuration.Factory;
import static io.github.caillette.wrench.Configuration.Inspector;
import static io.github.caillette.wrench.Configuration.Property;
import static org.assertj.core.api.Assertions.assertThat;

public class Tweaking {

  public interface Simple extends Configuration {
    int number() ;
    Boolean positive() ;
  }

  @Test
  public void test() throws Exception {
    final Factory< Simple > factory = new TemplateBasedFactory<Simple>( Simple.class )
    {
      @Override
      protected void initialize() {
        property( using.positive() ).defaultValue( null ) ;
      }

      @Override
      protected ImmutableMap< Property< Simple >, TweakedValue > tweak(
          final Simple configuration
      ) {
        final Inspector<Simple> inspector = ConfigurationTools.newInspector( configuration );
        final int number = configuration.number() ;
        final Boolean sign = configuration.positive() ;
        final Property< Simple > stringProperty = inspector.lastAccessed().get( 0 ) ;
        if ( sign == null ) {
          final TweakedValue tweakedValue ;
          if ( number > 0 ) {
            tweakedValue = new TweakedValue( true, "+1" ) ;
          } else if ( number < 0 ) {
            tweakedValue = new TweakedValue( false, "-1" ) ;
          } else {
            tweakedValue = new TweakedValue( null, "0" ) ;
          }
          return ImmutableMap.of( stringProperty, tweakedValue ) ;
        }
        return null ;
      }
    } ;
    final Simple configuration = factory.create( Sources.newSource( "number = -1" ) ) ;

    assertThat( configuration.number( ) ).isEqualTo( -1 ) ;
    assertThat( configuration.positive() ).isEqualTo( Boolean.FALSE ) ;
  }
}
