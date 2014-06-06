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
  ImmutableSet< Infrigement< C > > validate( C configuration ) ;

  class Infrigement< C extends Configuration > {
    public final Configuration.Property< C > property ;
    public final String propertyValue ;
    public final String message ;
    public final Configuration.Source source ;

    public Infrigement(
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

    public Infrigement(
        final Configuration.Property< C > property,
        final String message
    ) {
      this.property = checkNotNull( property ) ;
      this.propertyValue = null ;
      this.source = null ;
      this.message = message ;
    }

    public Infrigement( final String message ) {
      this.property = null ;
      this.propertyValue = null ;
      this.source = null ;
      this.message = message ;
    }

  }

  /**
   * Offers built-in validation methods that accumulate {@link Validator.Infrigement} instances.
   */
  class Accumulator< C extends Configuration > {

    private final ImmutableSet.Builder< Infrigement< C > > builder
        = ImmutableSet.builder() ;

    private final Configuration.Inspector< C > inspector;

    public Accumulator( C configuration ) {
      this.inspector = ConfigurationTools.support( configuration ) ;
    }

    public ImmutableSet< Infrigement< C > > done() {
      return builder.build() ;
    }

    public void throwExceptionIfHasInfrigements() throws ValidationException {
      final ImmutableSet< Infrigement > done
          = ( ImmutableSet< Infrigement >  ) ( ImmutableSet ) done() ;
      if( done.size() > 0 ) {
        throw new ValidationException( done ) ;
      }
    }


    public Accumulator< C > verify( final boolean mustBeTrue, String message ) {
      if( ! mustBeTrue ) {
        add( message ) ;
      }
      return this ;
    }



    public Accumulator< C > add( String message ) {
      builder.add( new Infrigement< C >( message ) ) ;
      return this ;
    }



    public Accumulator< C > smartVerify( final boolean mustBeTrue ) {
      if( ! mustBeTrue ) {
        smartAdd( null ) ;
      }
      return this ;
    }

    public Accumulator< C > smartVerify( final boolean mustBeTrue, String message ) {
      if( ! mustBeTrue ) {
        smartAdd( message ) ;
      }
      return this ;
    }

    public Accumulator< C > smartAdd( String message ) {
      final Configuration.Property< C > property = inspector.lastAccessed() ;
      return smartAdd( property, message ) ;
    }

    public Accumulator< C > smartAdd(
        final Configuration.Property< C > property,
        String message
    ) {
      checkState( property != null ) ;
      builder.add( new Infrigement<>(
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
      builder.add( new Infrigement<>(
          property,
          message
      ) ) ;
      return this ;
    }

  }
}
