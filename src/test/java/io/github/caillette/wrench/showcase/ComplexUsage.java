package io.github.caillette.wrench.showcase;

import com.google.common.collect.ImmutableSet;
import io.github.caillette.wrench.*;
import org.junit.Test;

import static io.github.caillette.wrench.Validator.Accumulator;
import static io.github.caillette.wrench.Validator.Bad;
import static org.fest.assertions.Assertions.assertThat;

public class ComplexUsage {

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
            .defaultValue( 123 )
            .maybeNull()
            .converter( new Converters.IntoIntegerObject() )
            .documentation( "Just a number." )
        ;
        setGlobalNameTransformer( NameTransformers.LOWER_HYPHEN ) ;
      }

      @Override
      protected ImmutableSet< Bad< Simple > > validate( final Simple configuration ) {
        final Accumulator< Simple > accumulator = new Accumulator<>( configuration ) ;
        if( configuration.myNumber() != null ) {
          accumulator.verify( configuration.myNumber() > 0, "Must be > 0" ) ;
        }
        accumulator.verify(
            configuration.myString().equals( configuration.myString().toUpperCase() ),
            "Must be upper case"
        ) ;
        return accumulator.done() ;
      }
    } ;
    final Simple configuration = factory.create( Sources.newSource( "my-string = FOO" ) ) ;

    assertThat( configuration.myNumber() ).isEqualTo( 123 ) ;
    assertThat( configuration.myString() ).isEqualTo( "FOO" ) ;
  }


}
