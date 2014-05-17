package io.github.caillette.wrench;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static io.github.caillette.wrench.Configuration.Converter;
import static io.github.caillette.wrench.Configuration.Factory;
import static io.github.caillette.wrench.ConfigurationTools.newFactory;
import static io.github.caillette.wrench.Sources.newSource;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ConfigurationToolsTest {

  @Test
  public void simpleStringAndNumber() throws Exception {
    final Factory< ConfigurationFixture.StringAndNumber > factory
        = newFactory( ConfigurationFixture.StringAndNumber.class ) ;
    System.out.println( "Properties: " + factory.properties() ) ;

    final ConfigurationFixture.StringAndNumber configuration
        = factory.create( newSource( "string=AZER", "number=123" ) ) ;

    assertThat( configuration.string() ).isEqualTo( "AZER" ) ;
    assertThat( configuration.number() ).isEqualTo( 123 ) ;
    assertThat( configuration.toString() ).isEqualTo(
        ConfigurationFixture.class.getSimpleName() + "$"
        + ConfigurationFixture.StringAndNumber.class.getSimpleName() + "{"
        + "number=123; "
        + "string=AZER"
        + "}"
    ) ;
  }

  @Test
  public void resolveStringDefault() throws Exception {
    final Factory< ConfigurationFixture.StringWithDefault > factory
        = newFactory( ConfigurationFixture.StringWithDefault.class ) ;
    final ConfigurationFixture.StringWithDefault configuration
        = factory.create( newSource( "" ) ) ;

    assertThat( configuration.string() ).isEqualTo( "STRING" ) ;
  }

  @Test( expected = DefinitionException.class )
  public void unparseableDefault() throws Exception {
    newFactory( ConfigurationFixture.UnparseableDefault.class ) ;
  }

  @Test
  public void allowNullityFromDefault() throws Exception {
    final Factory< ConfigurationFixture.StringWithDefaultNull > factory
        = newFactory( ConfigurationFixture.StringWithDefaultNull.class ) ;
    final ConfigurationFixture.StringWithDefaultNull configuration
        = factory.create( newSource( "" ) ) ;

    assertThat( configuration.string() ).isNull() ;
  }

  @Test
  public void allowExplicitNullity() throws Exception {
    final Factory< ConfigurationFixture.JustInteger > factory
        = newFactory( ConfigurationFixture.JustInteger.class ) ;
    final ConfigurationFixture.JustInteger configuration
        = factory.create( newSource( "number=" ) ) ;

    assertThat( configuration.number() ).isNull() ;
  }

  @Test( expected = DefinitionException.class )
  public void incompatibleAnnotations() throws Exception {
    final Factory< ConfigurationFixture.IncompatibleAnnotations > factory
        = newFactory( ConfigurationFixture.IncompatibleAnnotations.class ) ;
    factory.create( newSource( "" ) ) ;
  }

  @Test
  public void undefinedProperties() throws Exception {
    final Factory< ConfigurationFixture.StringWithDefault > factory
        = newFactory( ConfigurationFixture.StringWithDefault.class ) ;
    try {
      factory.create( newSource( "foo=bar", "boo=yaa" ) ) ;
      fail( "Should have thrown an exception" ) ;
    } catch ( ConfigurationException e ) {
      assertThat( e.getMessage() ).contains( "Unknown property name 'boo'" ) ;
      assertThat( e.getMessage() ).contains( "Unknown property name 'foo'" ) ;
    }
  }

  @Test
  public void unparseableroperty() throws Exception {
    final Factory< ConfigurationFixture.StringAndNumber > factory
        = newFactory( ConfigurationFixture.StringAndNumber.class ) ;
    try {
      factory.create( newSource( "string=bar", "number=UNPARSEABLE" ) ) ;
      fail( "Should have thrown an exception" ) ;
    } catch ( ConfigurationException e ) {
      assertThat( e.getMessage() ).contains( "Can't parse 'UNPARSEABLE'" ) ;
    }
  }

  @Test
  public void localNameTransformer() throws Exception {
    final Factory< ConfigurationFixture.LocalNameTransformation > factory
        = newFactory( ConfigurationFixture.LocalNameTransformation.class ) ;
    System.out.println( "Properties: " + factory.properties() ) ;

    assertThat( factory.properties() ).hasSize( 1 ) ;
    final Configuration.Property< ConfigurationFixture.LocalNameTransformation >
        property = factory.properties().values().iterator().next() ;
    assertThat( property.name() ).isEqualTo( "multipart-method-name" ) ;
  }

  @Test
  public void globalNameTransformer() throws Exception {
    final Factory< ConfigurationFixture.GlobalNameTransformation > factory
        = newFactory( ConfigurationFixture.GlobalNameTransformation.class ) ;
    System.out.println( "Properties: " + factory.properties() ) ;

    assertThat( factory.properties() ).hasSize( 1 ) ;
    final Configuration.Property< ConfigurationFixture.GlobalNameTransformation >
        property = factory.properties().values().iterator().next() ;
    assertThat( property.name() ).isEqualTo( "multipart-method-name" ) ;
  }

  @Test
  public void mixedNameTransformer() throws Exception {
    final Factory< ConfigurationFixture.MixedNameTransformation > factory
        = newFactory( ConfigurationFixture.MixedNameTransformation.class ) ;
    System.out.println( "Properties: " + factory.properties() ) ;

    assertThat( factory.properties() ).hasSize( 1 ) ;
    final Configuration.Property< ConfigurationFixture.MixedNameTransformation >
        property = factory.properties().values().iterator().next() ;
    assertThat( property.name() ).isEqualTo( "multipart.method.name" ) ;
  }

  @Test
  public void support() throws Exception {
    final Factory< ConfigurationFixture.StringWithDefault > factory
        = newFactory( ConfigurationFixture.StringWithDefault.class ) ;
    final ConfigurationFixture.StringWithDefault configuration
        = factory.create( newSource( "" ) ) ;

    final Configuration.Support< ConfigurationFixture.StringWithDefault > support
        = ConfigurationTools.support( configuration ) ;
    final Configuration.Property< ConfigurationFixture.StringWithDefault >
        property = support.properties().get( "string" ) ;

    assertThat( support.usingDefault( property ) ).isTrue() ;
    assertThat( support.sourceOf( property ).sourceName() ).isEqualTo(
          "java:Annotations{"
        + ConfigurationFixture.class.getName()
        + '$' + ConfigurationFixture.StringWithDefault.class.getSimpleName()
        + "}"
    ) ;

    assertThat( support.lastAccessed() ).isNull() ;
    configuration.string() ;
    assertThat( support.lastAccessed() ).isSameAs( property ) ;
  }

  @Test
  public void validation() throws Exception {
    final Factory< ConfigurationFixture.Validated > factory
        = newFactory( ConfigurationFixture.Validated.class ) ;
    try {
      factory.create( newSource( "foo=Foo", "bar=Bar" ) ) ;
      fail( "Should have thrown an exception" ) ;
    } catch ( ValidationException e ) {
      e.printStackTrace( System.out ) ;
      assertThat( e.getMessage() ).contains( "foo -> 'Foo' - Should be 'FOO'" ) ;
      assertThat( e.getMessage() ).contains( "bar -> 'Bar' - Should be 'BAR'" ) ;
    }
  }

  @Test
  public void converterFailsToConvert() throws Exception {
    final Factory< ConfigurationFixture.WithName > factory = newFactory(
        ConfigurationFixture.WithName.class,
        ImmutableMap.< Class< ? >, Converter >of(
            ConfigurationFixture.Name.class,
            new ConfigurationFixture.IntoNameConverter( true )
        )
    ) ;
    try {
      factory.create( newSource( "name1 = one", "name2 = two" ) ) ;
      fail( "Should have thrown an exception" ) ;
    } catch ( ConfigurationException e ) {
      e.printStackTrace( System.out ) ;
      assertThat( e.getMessage() ).contains(
          "Should be all upper case: 'two' for property 'name2'" ) ;
    }
  }
}
