package io.github.caillette.wrench.showcase;

import com.google.common.collect.ImmutableSet;
import io.github.caillette.wrench.*;
import org.junit.Test;

import static io.github.caillette.wrench.Validator.Infrigement;
import static org.fest.assertions.Assertions.assertThat;

public class TemplateBasedDefinition {

  public interface Simple extends Configuration {
    Integer myNumber() ;
    String myString() ;
  }

  @Test
  public void test() throws Exception {
    final Configuration.Factory< Simple > factory ;
    factory = new TemplateBasedFactory< Simple >( Simple.class ) {
      @Override
      protected void initialize() {
        on( template.myNumber() )
            .defaultValue( INTEGER )
            .maybeNull()
            .converter( new Converters.IntoIntegerObject() )
            .documentation( "Just a number." )
        ;
        setGlobalNameTransformer( NameTransformers.LOWER_HYPHEN ) ;
      }

      @Override
      protected ImmutableSet< Infrigement< Simple > > validate( final Simple configuration ) {
        return super.validate( configuration ) ; // TODO: illustrate validation.
      }
    } ;
    final Simple configuration = factory.create( Sources.newSource( "my-string = foo" ) ) ;

    assertThat( configuration.myNumber() ).isSameAs( INTEGER ) ;
    assertThat( configuration.myString() ).isEqualTo( "foo" ) ;
  }

  private static final Integer INTEGER = 123 ;

}
