Wrench
======

Wrench is a Java library for reading configuration files and command-line arguments in the safest possible way. It use static typing and fail-fast wherever it can.

Wrench started as a conceptual spin-off of the [OWNER](https://github.com/lviggiano/owner) project. Annotations quickly showed their limits when defining default values or property constraints, which must be Strings or classes instead of real objects. Wrench turns the broken declarative approach into an imperative one, through clever use of a dynamic proxy to transform a method call into property designation.(This esoteric gibberish will probably become clearer after reading the examples below.)   

Wrench is tailored for usage in a closed-source project. So it's main purpose here on Github is to showcase a pair of coding ideas.


Basic usage
-----------

Declare an interface like this:

```java
  public interface Simple extends Configuration {
    Integer number() ;
    String string() ;
  }
```

Create a `my.properties` file:

```properties
string = Hello
number = 43
```

Read the file and get an implementation:

```java
final Configuration.Factory< Simple > factory = ConfigurationTools.newFactory( Simple.class ) ;
final Simple configuration = factory.create( Sources.newSource(
    "myNumber = 123",
    "myString = foo"
) ) ;

assertThat( configuration.myNumber() ).isEqualTo( 123 ) ;
assertThat( configuration.myString() ).isEqualTo( "foo" ) ;
assertThat( configuration.toString() )
    .isEqualTo( "SimplestUsage$Simple{myNumber=123; myString=foo}" ) ;
```

Got the idea? There is more.

Features
--------

- Compiler-checked default values.
- Fail-fast on unknown property names.
- Fail-fast on undefined properties.
- Error messages with source location.
- Multiple error messages for batching corrections.
- Custom property names.
- Custom property names by method name transformation (`myName -> my.name or my-name`).
- Immutability wherever possible, using Guava's `Immutable*`. Yes Wrench heavily depends on Guava.
- Property overriding when using multiple sources.
- Sources can be files, or command-line parameters, or anything.
- Lightweight validation (applies on the whole ´Configuration´ object).
- Pattern-based obfuscation of sensible parts (like passwords), specified by annotations.

The snippet below illustrate some of those features.

```java
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
```

See tests for more use cases.


