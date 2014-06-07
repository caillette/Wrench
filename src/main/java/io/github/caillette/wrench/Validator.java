package io.github.caillette.wrench;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.github.caillette.wrench.ConfigurationTools.newInspector;

/**
 * Validates a whole {@link Configuration} before a {@link Configuration.Factory}
 * makes it available.
 */
public interface Validator< C extends Configuration > {

  /**
   * @return a non-{@code null} object.
   */
  ImmutableSet< Bad > validate( C configuration ) ;

  class Bad {
    final ImmutableList< ValuedProperty > properties ;
    public final String message ;

    public Bad( final ImmutableList< ValuedProperty > properties, final String message ) {
      this.properties = checkNotNull( properties ) ;
      this.message = message ;
    }

    public Bad( final String message ) {
      this.properties = ImmutableList.of() ;
      this.message = message ;
    }

  }

  /**
   * Offers built-in validation methods that accumulate {@link io.github.caillette.wrench.Validator.Bad} instances.
   */
  class Accumulator< C extends Configuration > {

    private final ImmutableSet.Builder< Bad > builder
        = ImmutableSet.builder() ;

    private final ConfigurationInspector< C > inspector ;

    public Accumulator( C configuration ) {
      this.inspector = ( ConfigurationInspector< C > ) newInspector( configuration ) ;
    }

    public ImmutableSet< Bad > done() {
      return builder.build() ;
    }

    public void throwValidationExceptionIfHasInfrigements() throws ValidationException {
      final ImmutableSet<Bad> done
          = ( ImmutableSet<Bad>  ) ( ImmutableSet ) done() ;
      if( done.size() > 0 ) {
        throw new ValidationException( done ) ;
      }
    }

    public void throwDeclarationExceptionIfHasInfrigements() throws DeclarationException {
      final ImmutableSet<Bad> done
          = ( ImmutableSet<Bad>  ) ( ImmutableSet ) done() ;
      if( done.size() > 0 ) {
        DeclarationException.throwWith( done ) ;
      }
    }


    public Accumulator< C > justVerify( final boolean mustBeTrue, String message ) {
      if( ! mustBeTrue ) {
        justAdd( message ) ;
      }
      return this ;
    }



    public Accumulator< C > justAdd( String message ) {
      builder.add( new Bad( message ) ) ;
      return this ;
    }



    public Accumulator< C > verify( final boolean mustBeTrue ) {
      if( ! mustBeTrue ) {
        add( null ) ;
      }
      inspector.clearLastAccessed() ;
      return this ;
    }

    public Accumulator< C > verify( final boolean mustBeTrue, String message ) {
      if( ! mustBeTrue ) {
        add( message ) ;
      }
      inspector.clearLastAccessed() ;
      return this ;
    }

    public Accumulator< C > add( String message ) {
      return add( resolveValuedProperties( inspector.lastAccessed() ), message ) ;
    }

    private ImmutableList< ValuedProperty > resolveValuedProperties(
        final ImmutableList< Configuration.Property< C > > properties
    ) {
      final ImmutableList.Builder< ValuedProperty > builder = ImmutableList.builder() ;
      for( final Configuration.Property< C > property : properties ) {
        final ValuedProperty valuedProperty = inspector.valuedProperty( property ) ;
        if ( valuedProperty == null ) {
          builder.add( new ValuedProperty( property ) ) ;
        } else {
          builder.add( valuedProperty ) ;
        }
      }
      return builder.build() ;
    }

    public Accumulator< C > add(
        final ImmutableList< ValuedProperty > property,
        String message
    ) {
      builder.add( new Bad(
          property,
          message
      ) ) ;
      return this ;
    }

    Accumulator< C > addInfrigementForNullity(
        final ImmutableList< Configuration.Property< C > > properties,
        String message
    ) {
      builder.add( new Bad( resolveValuedProperties( properties ), message ) ) ;
      return this ;
    }

  }
}
