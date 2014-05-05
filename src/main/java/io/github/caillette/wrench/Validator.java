package io.github.caillette.wrench;

import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Validates a whole {@link Configuration} before a {@link Configuration.Factory}
 * makes it available.
 *
 * @author Laurent Caillette
 */
public interface Validator< C extends Configuration > {

  /**
   * @return a non-{@code null} object.
   */
  ImmutableSet< Infrigement< C > > validate( C configuration ) ;

  class Infrigement< C extends Configuration > {
    public final Configuration.Property< C > propertyProperty;
    public final String propertyValue ;
    public final String message ;
    public final Configuration.Source source ;

    public Infrigement(
        final Configuration.Property< C > propertyProperty,
        final String propertyValue,
        final Configuration.Source source,
        final String message
    ) {
      this.propertyProperty = checkNotNull( propertyProperty ) ;
      this.propertyValue = propertyValue ;
      this.source = checkNotNull( source ) ;
      this.message = message ;
    }
  }

  /**
   * Offers built-in validation methods that accumulate {@link Validator.Infrigement} instances.
   *
   * @author Laurent Caillette
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
      checkState( property != null ) ;
      builder.add( new Infrigement<>(
          property,
          support.stringValueOf( property ),
          support.sourceOf( property ),
          message
      ) ) ;
      return this ;
    }

  }
}
