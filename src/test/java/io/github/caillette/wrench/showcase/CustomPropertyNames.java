package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.ConfigurationTools;
import io.github.caillette.wrench.NameTransformers;
import io.github.caillette.wrench.Sources;
import org.junit.Test;

import static io.github.caillette.wrench.Configuration.Annotations.Name;
import static io.github.caillette.wrench.Configuration.Annotations.TransformName;
import static org.fest.assertions.Assertions.assertThat;

public class CustomPropertyNames {

  /**
   * Demonstrates all possible renamings.
   */
  @TransformName( AllUppercase.class )
  public interface Renamed extends Configuration {

    /**
     * Explicit name set at method level.
     * This renaming overrides class-level renaming.
     */
    @Name( "string-zero" )
    String stringZero() ;

    /**
     * Function-based renaming at method level.
     * This renaming overrides class-level renaming.
     */
    @TransformName( NameTransformers.LowerDot.class )
    String stringOne() ;

    /**
     * Function-based renaming at class level.
     */
    String stringTwo() ;
  }

  public static class AllUppercase implements Configuration.NameTransformer {
    @Override
    public String transform( String javaMethodName ) {
      return javaMethodName.toUpperCase() ;
    }
  }


  @Test
  public void test() throws Exception {
    final Configuration.Factory< Renamed > factory
        = ConfigurationTools.newFactory( Renamed.class ) ;
    final Renamed configuration = factory.create( Sources.newSource(
        "string-zero = zero",
        "string.one = one",
        "STRINGTWO = two"
    ) ) ;

    assertThat( configuration.stringZero() ).isEqualTo( "zero" ) ;
    assertThat( configuration.stringOne() ).isEqualTo( "one" ) ;
    assertThat( configuration.stringTwo() ).isEqualTo( "two" ) ;
  }
}
