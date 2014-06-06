package io.github.caillette.wrench;

import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Validates a whole {@link Configuration} before a {@link Configuration.Factory}
 * makes it available.
 */
public interface Validator< C extends Configuration > {

  /**
   * @return a non-{@code null} object.
   */
  ImmutableSet< Bad< C > > validate( C configuration ) ;

  class Bad< C extends Configuration > {
    public final Configuration.Property< C > property ;
    public final String propertyValue ;
    public final String message ;
    public final Configuration.Source source ;

    public Bad(
        final Configuration.Property< C > property,
        final String propertyValue,
        final Configuration.Source source,
        final String message
    ) {
      this.property = checkNotNull( property ) ;
      this.propertyValue = propertyValue ;
      this.source = checkNotNull( source ) ;
      this.message = message ;
    }

    public Bad(
        final Configuration.Property< C > property,
        final String message
    ) {
      this.property = checkNotNull( property ) ;
      this.propertyValue = null ;
      this.source = null ;
      this.message = message ;
    }

    public Bad( final String message ) {
      this.property = null ;
      this.propertyValue = null ;
      this.source = null ;
      this.message = message ;
    }

  }

  /**
   * Offers built-in validation methods that accumulate {@link io.github.caillette.wrench.Validator.Bad} instances.
   */
  class Accumulator< C extends Configuration > {

    private final ImmutableSet.Builder< Bad< C > > builder
        = ImmutableSet.builder() ;

    private final Configuration.Inspector< C > inspector;

    public Accumulator( C configuration ) {
      this.inspector = ConfigurationTools.inspector( configuration ) ;
    }

    public ImmutableSet< Bad< C > > done() {
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
      builder.add( new Bad< C >( message ) ) ;
      return this ;
    }



    public Accumulator< C > verify( final boolean mustBeTrue ) {
      if( ! mustBeTrue ) {
        add( null ) ;
      }
      return this ;
    }

    public Accumulator< C > verify( final boolean mustBeTrue, String message ) {
      if( ! mustBeTrue ) {
        add( message ) ;
      }
      return this ;
    }

    public Accumulator< C > add( String message ) {
      final Configuration.Property< C > property = inspector.lastAccessed() ;
      return add( property, message ) ;
    }

    public Accumulator< C > add(
        final Configuration.Property<C> property,
        String message
    ) {
      checkState( property != null ) ;
      builder.add( new Bad<>(
          property,
          inspector.stringValueOf( property ),
          inspector.sourceOf( property ),
          message
      ) ) ;
      return this ;
    }

    Accumulator< C > addInfrigementForNullity(
        final Configuration.Property< C > property,
        String message
    ) {
      checkState( property != null ) ;
      builder.add( new Bad<>(
          property,
          message
      ) ) ;
      return this ;
    }

  }
}
