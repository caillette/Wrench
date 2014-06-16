package io.github.caillette.wrench;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.io.File;

import static io.github.caillette.wrench.Configuration.Converter;

/**
 * Default {@link Converter}s.
 */
public final class Converters {

  private Converters() { }



  public static abstract class AbstractConverter< T > implements Converter< T > {
    @Override
    public String toString() {
      return ConfigurationTools.getNiceName( getClass() ) ;
    }
  }

  public static final Converter< String > INTO_STRING = new AbstractConverter< String >() {
    @Override
    public String convert( String input ) {
      return input ;
    }
  } ;

  public static final Converter< Integer > INTO_INTEGER_PRIMITIVE
      = new AbstractConverter< Integer >()
  {
    @Override
    public Integer convert( final String input ) {
        return Integer.parseInt( input ) ;
    }
  } ;

  public static final Converter< Integer > INTO_INTEGER_OBJECT
      = new AbstractConverter< Integer >()
  {
    @Override
    public Integer convert( final String input ) throws Exception {
      if( Strings.isNullOrEmpty( input ) ) {
        return null ;
      }
      return Integer.parseInt( input ) ;
    }
  } ;

  public static final Converter< File > INTO_FILE = new AbstractConverter< File >() {
    @Override
    public File convert( final String input ) throws Exception {
      if( Strings.isNullOrEmpty( input ) ) {
        return null ;
      }
      return new File( input ) ;
    }
  } ;

  public static final ImmutableMap< Class< ? >, Converter > DEFAULTS
      = ImmutableMap.of(
          ( Class< ? > ) String.class, ( Converter ) INTO_STRING,
          Integer.TYPE, INTO_INTEGER_PRIMITIVE,
          Integer.class, INTO_INTEGER_OBJECT,
          File.class, INTO_FILE
      )
  ;

}
