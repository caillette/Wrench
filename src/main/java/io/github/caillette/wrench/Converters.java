package io.github.caillette.wrench;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Method;

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

  public static class IntoString extends AbstractConverter< String > {
    @Override
    public String convert( Method definingMethod, String input ) {
      return input ;
    }
  }

  public static class IntoIntegerPrimitive extends AbstractConverter< Integer > {
    @Override
    public Integer convert( Method definingMethod, String input ) throws ConvertException {
      try {
        return Integer.parseInt( input ) ;
      } catch ( NumberFormatException e ) {
        throw new ConvertException( "Can't parse '" + input + "'" ) ;
      }
    }
  }

  public static class IntoIntegerObject extends AbstractConverter< Integer > {
    @Override
    public Integer convert( Method definingMethod, String input ) throws Exception {
      if( Strings.isNullOrEmpty( input ) ) {
        return null ;
      }
      try {
        return Integer.parseInt( input ) ;
      } catch ( NumberFormatException e ) {
        throw new ConvertException( "Can't parse '" + input + "'" ) ;
      }
    }
  }

  public static final ImmutableMap< Class< ? >, Converter > DEFAULTS
      = ImmutableMap.of(
          ( Class< ? > ) String.class, ( Converter ) new IntoString(),
          Integer.TYPE, new IntoIntegerPrimitive(),
          Integer.class, new IntoIntegerObject()
      )
  ;

}
