package io.github.caillette.wrench.showcase;

import com.google.common.collect.ImmutableSet;
import io.github.caillette.wrench.*;
import org.junit.Test;

import java.lang.reflect.Method;

import static io.github.caillette.wrench.ConfigurationTools.newFactory;
import static io.github.caillette.wrench.Validator.Infrigement;
import static org.fest.assertions.Assertions.assertThat;

public class TemplateBasedDefinition {

  public interface Simple extends Configuration {
    Integer number() ;
    String string() ;
  }

  @Test
  public void test() throws Exception {
    final Configuration.Factory< Simple > factory ;
    factory = new TemplateBasedFactory< Simple >( Simple.class ) {
      @Override
      protected void initialize() {
        on( template.number() )
            .defaultValue( INTEGER )
            .maybeNull()
            .converter( new Converters.IntoIntegerObject() )
            .documentation( "Just a number property." )
        ;
        setGlobalNameTransformer( NameTransformers.LOWER_HYPHEN ) ;
      }

      @Override
      protected ImmutableSet< Infrigement< Simple > > validate( final Simple configuration ) {
        return super.validate( configuration ) ; // TODO: illustrate validation.
      }
    } ;
    final Simple configuration = factory.create( Sources.newSource( "string = foo" ) ) ;

    assertThat( configuration.number() ).isSameAs( INTEGER ) ;
    assertThat( configuration.string() ).isEqualTo( "foo" ) ;
  }

  private static final Integer INTEGER = 123 ;

}
