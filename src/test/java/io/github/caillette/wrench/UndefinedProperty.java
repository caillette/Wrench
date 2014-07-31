package io.github.caillette.wrench;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class UndefinedProperty {

  public interface MyConfiguration extends Configuration {
    String string() ;
  }

  @Test
  public void allowUndefinedPropertyInSource() throws Exception {
    final Configuration.Factory< MyConfiguration > factory
        = new TemplateBasedFactory< MyConfiguration >( MyConfiguration.class )
    {
      @Override
      protected void initialize() {
        checkAllPropertiesDefined( false ) ;
      }
    } ;
    final MyConfiguration configuration = factory.create(
        Sources.newSource( "string = s", "undefined = u" ) ) ;

    assertThat( configuration.string() ).isEqualTo( "s" ) ;

  }

  @Test
  public void mandatoryByDefault() throws Exception {
    final Configuration.Factory< MyConfiguration > factory
        = new TemplateBasedFactory< MyConfiguration >( MyConfiguration.class )
    {
      @Override
      protected void initialize() {
        checkAllPropertiesDefined( false ) ;
      }
    } ;
    try {
      factory.create( Sources.newSource( "" ) ) ;
      fail( "Failed to throw " + DeclarationException.class.getSimpleName() ) ;
    } catch ( DeclarationException e ) {
      assertThat( e.getMessage() ).contains( "[ string ] No value set" ) ;
    }

  }

}
