Wrench
======

Wrench is a Java library for reading configuration files and command-line arguments in the safest possible way. It use static typing and fail-fast wherever it can.

Wrench started as a conceptual spin-off of the [OWNER](https://github.com/lviggiano/owner) project. Annotations quickly showed their limits when defining default values or property constraints, which must be Strings or classes instead of real objects. Wrench promotes a purely imperative approach.    

Wrench is tailored for usage in a closed-source project. So it's main purpose here on Github is to showcase a pair of coding ideas.

Wrench requires at least Java 7. It doesn't use Java 8 constructs yet, but this is planned (first-class methods look promising). 


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
- Bulk transformation of method names (`myName() -> 'my.name' or 'my-name'`).
- Immutability wherever possible, using Guava's `Immutable*`. Wrench heavily depends on Guava.
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
```

See [tests](https://github.com/caillette/Wrench/tree/master/src/test/java/io/github/caillette/wrench/showcase) for more use cases.


