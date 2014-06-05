package io.github.caillette.wrench;

import com.google.common.collect.ImmutableMap;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Pattern;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Tagging interface for interfaces defining a proxy-backed configuration.
 * Methods must have no parameter.
 * Each method represents a property, which has a
 * {@link io.github.caillette.wrench.Configuration.Property} and a value.
 *
 * @see io.github.caillette.wrench.Configuration.Annotations
 *
 */
public interface Configuration {

  /**
   * Represents something a part of, or a whole {@link Configuration}.
   */
  public interface Source {

    /**
     * Helps identifying the file or the code values come from.
     */
    String sourceName() ;


    /**
     * Contains {@code String}s to
     * {@link io.github.caillette.wrench.Configuration.Converter#convert(java.lang.reflect.Method, String) convert}
     * into expected type.
     */
    interface Stringified extends Source {
      ImmutableMap< String, String > map() ;
    }

    /**
     * Contains property values as {@link io.github.caillette.wrench.Configuration} methods
     * should return them.
     */
    interface Raw< C extends Configuration > extends Source {
      ImmutableMap< Property< C >, Object > map() ;
    }
  }


  /**
   * Creates a {@link Configuration} object given one or more {@link Source}s.
   *
     */
  public static interface Factory< C extends Configuration > {

    /**
     * Values in last {@link Source} objects override those in first ones.
     */
    C create( Source source1, Source... others ) throws ConfigurationException ;

    Class< C > configurationClass() ;

    ImmutableMap< String, Property< C > > properties() ;
  }

  /**
   * Defines the behavior and the constraints of a property corresponding to a method
   * in a {@link Configuration}.
   */
  public static interface Property< C extends Configuration > {

    Method declaringMethod() ;

    String name() ;

    Class< ? > type() ;

    /**
     * Default value extracted from {@link #defaultValueAsString()} after using
     * available {@link Configuration.Converter}s.
     *
     * It makes sense to calculate it when creating {@link io.github.caillette.wrench.Configuration.Property} objects as we can
     * fail fast if annotations define a wrong default value.
     *
     * @return {@code null} if {@link #defaultValueAsString()} returns {@code null}.
     */
    Object defaultValue() ;

    /**
     * Original representation of {@link #defaultValue()} as a {@code String}.
     *
     * @return always {@code null} if {@link #defaultValue()} returns {@code null} or if
     *     {@link #maybeNull()} returns {@code true}.
     */
    String defaultValueAsString() ;

    /**
     * A pattern indicating sensible part that should not appear in clear in logs.
     *
     * @see Configuration.Support#safeValueOf(Configuration.Property, String)
     * @return a possibly {@code null} value.
     */
    Pattern obfuscatorPattern() ;

    Converter converter() ;

    boolean maybeNull() ;


    Comparator< Property > COMPARATOR = ConfigurationProperty.COMPARATOR ;
  }

  /**
   * Transforms a plain {@code String} into a property value.
   */
  public static interface Converter< T > {
    /**
     * @param definingMethod the defining method, could help in some cases.
     * @param input a possibly null {@code String}
     */
    T convert( Method definingMethod, String input ) throws Exception ;
  }


  /**
   * Transforms a Java method name from an interface defining a {@link Configuration}
   * into another kind of name that could use forbidden characters, like full stop (aka dot)
   * or hyphen (aka dash).
   */
  interface NameTransformer {

    String transform( String javaMethodName ) ;

  }

  /**
   * Exposes {@link Configuration} metadata.
   */
  public static interface Support< C extends Configuration> {

    public ImmutableMap< String, Property< C > > properties() ;

    public boolean usingDefault( Property< C > property ) ;

    Source sourceOf( Property< C > property ) ;

    String stringValueOf( Property< C > property ) ;

    /**
     * Returns a possibly obfuscated value if property was annotated with an
     * {@link io.github.caillette.wrench.Configuration.Annotations.Obfuscator}.
     */
    String safeValueOf( Property< C > property, String replacement ) ;

    /**
     * Returns the {@link io.github.caillette.wrench.Configuration.Property} corresponding
     * to the last property-related method call on a {@link Configuration} object.
     * This makes this object mutable but in a way that does not affect its fundamental behavior.
     *
     * @return a possibly {@code null} value that means that no call happened yet
     *     in current thread.
     */
    Property< C > lastAccessed() ;

  }

  interface PropertySetup {

    public final class DefaultValue< C extends Configuration, T > {

      private final Property< C > lastAccessed ;
      private final Map< Property< C >, Object > builder ;

      public DefaultValue(
          final Property< C > lastAccessed,
          final Map< Property< C >, Object > builder
      ) {
        this.lastAccessed = lastAccessed ;
        this.builder = builder ;
      }

      public void defaultValue( T value ) {
        builder.put( lastAccessed, value ) ;
      }
    }

  }


  interface Annotations {

    /**
     * Default value to be used if no property is found.
     * No quoting (other than normal Java string quoting) is done.
     * Mutually exclusive with {@link DefaultNull}.
     */
    @Retention( RUNTIME )
    @Target( METHOD )
    @Documented
    @interface DefaultValue {
      String value() ;
    }

    /**
     * Means nullable.
     * Mutually exclusive with {@link DefaultValue}.
     */
    @Retention( RUNTIME )
    @Target( METHOD )
    @Documented
    @interface DefaultNull { }

    /**
     * The exact property used for lookup for the property.
     * If not present, the property will be generated based on the unqualified method name.
     * Mutually exclusive with {@link TransformName} if applies at method level.
     */
    @Retention( RUNTIME )
    @Target( METHOD )
    @Documented
    @interface Name {
      String value() ;
    }

    /**
     * References the {@link NameTransformer} to apply to method names.
     * Mutually exclusive with {@link Name} if {@link TransformName} applies at method level.
     */
    @Retention( RUNTIME )
    @Target( { METHOD, TYPE } )
    @Documented
    @interface TransformName {
      Class< ? extends NameTransformer > value() ;
    }

    /**
     * References the {@link Validator} for a freshly-created {@link Configuration} object.
     */
    @Retention( RUNTIME )
    @Target( { TYPE } )
    @Documented
    @interface ValidateWith {
      Class< ? extends Validator > value() ;
    }

    /**
     * Human-readable text to explain the role of the property.
     */
    @Retention( RUNTIME )
    @Target( METHOD )
    @Documented
    @interface Documentation {
      String value() ;
    }

    /**
     * Dedicated {@link Converter}.
     */
    @Retention( RUNTIME )
    @Target( METHOD )
    @Documented
    @interface Convert {
      Class< ? extends Converter > value() ;
    }

    /**
     * Regular expression to obfuscate password or other sensitive information.
     */
    @Retention( RUNTIME )
    @Target( METHOD )
    @Documented
    @interface Obfuscator {
      String value() ;
    }

  }

  public final class PropertySetup2< C extends Configuration, T > {

    private final Method lastAccessed ;
    private final SetupAcceptor setupAcceptor ;


    public PropertySetup2(
        final Method lastAccessed,
        final SetupAcceptor setupAcceptor
    ) {
      this.lastAccessed = lastAccessed ;
      this.setupAcceptor = setupAcceptor ;
    }

    /**
     * Don't call this after calling {@link #mandatory()}.
     */
    public PropertySetup2< C, T > defaultValue( T value ) {
      setupAcceptor.accept( lastAccessed, Feature.DEFAULT_VALUE, value ) ;
      return this ;
    }

    public PropertySetup2< C, T > name( final String name ) {
      return this ;
    }

    /**
     * Don't call this after calling {@link #mandatory()}.
     */
    public PropertySetup2< C, T > maybeNull() {
      return this ;
    }

    /**
     * Don't call this after calling {@link #defaultValue(Object)} or {@link #maybeNull()}.
     */
    public PropertySetup2< C, T > mandatory() {
      return this ;
    }

    public PropertySetup2< C, T > converter( final Converter converter ) {
      return this ;
    }

    public PropertySetup2< C, T > obfuscator( final Pattern pattern ) {
      return this ;
    }

    public PropertySetup2< C, T > documentation( final String text ) {
      return this ;
    }

    /**
     * Call only after a call to {@link #defaultValue(Object)}.
     */
    public PropertySetup2< C, T > stringValueForDefault( final String text ) {
      return this ;
    }

    enum Feature {
      DEFAULT_VALUE,
      NAME,
      MAYBE_NULL,
      MANDATORY,
      CONVERTER,
      OBFUSCATOR,
      DOCUMENTATION,
      DEFAULT_VALUE_AS_STRING
    }

    interface SetupAcceptor {
      void accept( Method method, Feature feature, Object object ) ;
    }

  }
}
