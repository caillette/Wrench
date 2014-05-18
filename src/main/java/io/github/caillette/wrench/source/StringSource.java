package io.github.caillette.wrench.source;

import com.google.common.collect.ImmutableMap;
import io.github.caillette.wrench.Configuration;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

/**
 * Reads a single {@code String} under {@code java.util.Properties} format.
 */
public class StringSource implements Configuration.Source.Stringified {

  private final ImmutableMap< String, String > map ;

  public StringSource( final String string ) {
    map = buildMap( string ) ;
  }

  @Override
  public ImmutableMap< String, String > map() {
    return map ;
  }

  @Override
  public String sourceName() {
    return "java:" + getClass().getName() ;
  }

  public static ImmutableMap< String, String > buildMap( String string ) {
    try {
      return buildMap( new StringReader( string ) ) ;
    } catch ( IOException e ) {
      throw new RuntimeException( e ) ;
    }
  }

  public static ImmutableMap< String, String > buildMap( Reader reader ) throws IOException {
    final Properties properties = new Properties() ;
    properties.load( reader ) ;
    final ImmutableMap.Builder< String, String > builder = ImmutableMap.builder() ;
    for( final Map.Entry< Object, Object > entry : properties.entrySet() ) {
      builder.put( ( String ) entry.getKey(), ( String ) entry.getValue() ) ;
    }
    return builder.build() ;
  }


}
