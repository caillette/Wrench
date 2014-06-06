package io.github.caillette.wrench.showcase;

import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.NameTransformers;
import io.github.caillette.wrench.Sources;
import io.github.caillette.wrench.TemplateBasedFactory;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class CustomPropertyNames {

  public interface AllRenamings extends Configuration {
    String stringZero() ;
    String stringOne() ;
    String stringTwo() ;
  }

  private static final Configuration.NameTransformer ALL_UPPER_CASE
      = new Configuration.NameTransformer()
  {
    @Override
    public String transform( String javaMethodName ) {
      return javaMethodName.toUpperCase() ;
    }
  } ;


  @Test
  public void test() throws Exception {
    final Configuration.Factory< AllRenamings > factory
        = new TemplateBasedFactory<AllRenamings>( AllRenamings.class )
    {
      @Override
      protected void initialize() {
        on( template.stringZero() ).name( "string-zero" ) ;
        on( template.stringOne() ).nameTransformer( NameTransformers.LOWER_DOT ) ;
        setGlobalNameTransformer( ALL_UPPER_CASE ) ;
      }
    } ;
    final AllRenamings configuration = factory.create( Sources.newSource(
        "string-zero = zero",
        "string.one = one",
        "STRINGTWO = two"
    ) ) ;

    assertThat( configuration.stringZero() ).isEqualTo( "zero" ) ;
    assertThat( configuration.stringOne() ).isEqualTo( "one" ) ;
    assertThat( configuration.stringTwo() ).isEqualTo( "two" ) ;
  }
}
