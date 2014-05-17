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
        final Configuration.Property<C> property,
        final String message
    ) {
      this.property = checkNotNull( property ) ;
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

    private final Configuration.Support< C > support ;

    public Accumulator( C configuration ) {
      this.support = ConfigurationTools.support( configuration ) ;
    }

    public ImmutableSet< Infrigement< C > > done() {
      return builder.build() ;
    }

    public void throwExceptionIfHasInfrigements() throws ValidationException {
      final ImmutableSet< Infrigement > done = ( ImmutableSet< Infrigement >  ) ( ImmutableSet ) done() ;
      if( done.size() > 0 ) {
        throw new ValidationException( done ) ;
      }
    }

    public Accumulator< C > verify( final boolean mustBeTrue ) {
      if( ! mustBeTrue ) {
        addInfrigement( null ) ;
      }
      return this ;
    }

    public Accumulator< C > verify( final boolean mustBeTrue, String message ) {
      if( ! mustBeTrue ) {
        addInfrigement( message ) ;
      }
      return this ;
    }

    public Accumulator< C > addInfrigement( String message ) {
      final Configuration.Property< C > property = support.lastAccessed() ;
      return addInfrigement( property, message ) ;
    }

    public Accumulator< C > addInfrigement(
        final Configuration.Property< C > property,
        String message
    ) {
      checkState( property != null ) ;
      builder.add( new Infrigement<>(
          property,
          support.stringValueOf( property ),
          support.sourceOf( property ),
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
