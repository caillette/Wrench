Wrench
======

Wrench is a Java library for reading configuration files and command-line arguments. It takes its inspiration from the [OWNER](https://github.com/lviggiano/owner) project. Wrench doesn't aim to compete with OWNER. Wrench is tailored for usage in a closed-source project. So it's main purpose here on Github is to showcase a pair of coding ideas.


Basic usage
-----------

Declare an interface like this:

```java
interface StringAndNumber extends Configuration {
  String string() ;
  int number() ;
}
```

Create a `my.properties` file:

```properties
string = Hello
number = 43
```

Read the file and get an implementation:

```java
final Factory< StringAndNumber > factory = newFactory( StringAndNumber.class ) ;
final File file = new File( "my.properties" ) ;
final StringAndNumber configuration = factory.create( newSource( file ) ) ;

assertThat( configuration.string() ).isEqualTo( "Hello" ) ;
assertThat( configuration.number() ).isEqualTo( 43 ) ;
```

Got the idea? There is more:

- Default values by annotations.
- Fail-fast on unknown property names.
- Fail-fast on undefined properties.
- Error messages with source location.
- Multiple error messages for batching corrections.
- Custom property names by method-level annotation.
- Custom property names by method name transformation.
- Immutability wherever possible, using Guava's `Immutable*`.
- Property overriding when using multiple sources.
- Sources can be files, or command-line parameters, or anything.
- Lightweight validation (applies on the whole ´Configuration´ object).

See tests for more details.
