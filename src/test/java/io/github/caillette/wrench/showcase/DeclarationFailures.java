package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.ConfigurationTools;
import io.github.caillette.wrench.DeclarationException;
import io.github.caillette.wrench.Sources;
import org.junit.Test;

import static io.github.caillette.wrench.Sources.newSource;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class DeclarationFailures {

  @SuppressWarnings( "UnusedDeclaration" )
  public interface Simple extends Configuration {
    Integer number() ;
  }

  @Test
  public void unparseable() throws Exception {
    final Configuration.Factory< Simple > factory = ConfigurationTools.newFactory( Simple.class ) ;

    try {
      factory.create( Sources.newSource( "number = unparseable" ) ) ;
      fail( "Should have thrown an exception" ) ;
    } catch ( DeclarationException e ) {
      assertThat( e.getMessage() ).contains(
          "Conversion failed on property 'number': java.lang.NumberFormatException, "
              + "For input string: \"unparseable\""
      ) ;
    }
  }

  @Test
  public void sourceWithUndefinedProperty() throws Exception {
    final Configuration.Factory< Simple > factory = ConfigurationTools.newFactory( Simple.class ) ;

    try {
      factory.create( Sources.newSource( "unknown = -" ) ) ;
      fail( "Should have thrown an exception" ) ;
    } catch ( DeclarationException e ) {
      assertThat( e.getMessage() ).contains( "Unknown property name 'unknown'" ) ;
    }
  }



  @Test
  public void missingValue() throws Exception {
    final Configuration.Factory< Simple > factory = ConfigurationTools.newFactory( Simple.class ) ;

    try {
      factory.create( newSource( "" ) ) ;
      fail( "Should have thrown an exception" ) ;
    } catch ( final DeclarationException e ) {
      System.out.println( e.getMessage() ) ;
      assertThat( e.getMessage() ).contains( "[ number ] No value set" ) ;
    }
  }

}
