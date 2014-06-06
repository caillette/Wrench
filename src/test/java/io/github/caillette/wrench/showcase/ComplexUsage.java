package io.github.caillette.wrench.showcase;

import com.google.common.collect.ImmutableSet;
import io.github.caillette.wrench.*;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static io.github.caillette.wrench.Configuration.Inspector;
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
            .name( "my-binary-number" )
            .maybeNull()
            .converter( new Configuration.Converter() {
              @Override
              public Object convert( Method definingMethod, String input ) throws Exception {
                return Integer.parseInt( input, 2 ) ;
              }
            } )
            .documentation( "Just a number." )
        ;
        on( template.myString() )
            .defaultValue( "FOO" )
            .documentation( "Just a string." )
            .obfuscator( Pattern.compile( "OO" ) )
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

    final Simple configuration = factory.create( Sources.newSource(
        "my-binary-number = 1111011" ) ) ;

    final Inspector< Simple > inspector = ConfigurationTools.inspector( configuration ) ;
    assertThat( configuration.myNumber() ).isEqualTo( 123 ) ;
    assertThat( inspector.usingDefault( inspector.lastAccessed() ) ).isFalse() ;
    assertThat( configuration.myString() ).isEqualTo( "FOO" ) ;
    assertThat( inspector.usingDefault( inspector.lastAccessed() ) ).isTrue() ;
    assertThat( inspector.lastAccessed().name() ).isEqualTo( "my-string" ) ;
    assertThat( inspector.safeValueOf( inspector.lastAccessed(), "**" ) ).isEqualTo( "F**" ) ;
  }


}
