package io.github.caillette.wrench;

import com.google.common.collect.ImmutableSet;

import static io.github.caillette.wrench.Configuration.Annotations.*;
import static io.github.caillette.wrench.NameTransformers.LowerDot;
import static io.github.caillette.wrench.NameTransformers.LowerHyphen;

@SuppressWarnings( "UnusedDeclaration" )
public interface ConfigurationFixture {

  interface JustInteger extends Configuration {
    Integer number() ;
  }

  interface StringAndNumber extends Configuration {
    String string() ;
    int number() ;
  }

  interface StringWithDefault extends Configuration {
    @DefaultValue( "STRING" )
    String string() ;
  }

  interface StringWithDefaultNull extends Configuration {
    // Can't do that: @DefaultValue( value = null )
    @DefaultNull
    String string() ;
  }

  interface IncompatibleAnnotations extends Configuration {
    @DefaultValue( "STRING" )
    @DefaultNull
    String string() ;
  }

  interface UnparseableDefault extends Configuration {
    @DefaultValue( "CANNOT_PARSE" )
    int number() ;
  }

  interface LocalNameTransformation extends Configuration {
    @TransformName( LowerHyphen.class )
    int multipartMethodName() ;
  }

  @TransformName( LowerHyphen.class )
  interface GlobalNameTransformation extends Configuration {
    int multipartMethodName() ;
  }

  @TransformName( LowerHyphen.class )
  interface MixedNameTransformation extends Configuration {
    @TransformName( LowerDot.class )
    int multipartMethodName() ;
  }

  @ValidateWith( MyValidator.class )
  interface Validated extends Configuration {
    String foo() ;
    String bar() ;
  }

  class MyValidator implements Validator< Validated > {
    @Override
    public ImmutableSet< Infrigement< Validated > > validate( Validated configuration ) {
      return new Accumulator<>( configuration )
          .smartVerify( "FOO".equals( configuration.foo() ), "Should be 'FOO'" )
          .smartVerify( "BAR".equals( configuration.bar() ), "Should be 'BAR'" )
          .done()
      ;
    }
  }
}
