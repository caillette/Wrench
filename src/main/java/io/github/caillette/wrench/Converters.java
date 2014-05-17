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
      return ConfigurationFactory.getNiceName( getClass() ) ;
    }
  }

  public static class ConverterToString extends AbstractConverter< String > {
    @Override
    public String convert( Method definingMethod, String input ) {
      return input ;
    }
  }

  public static class ConverterToIntegerPrimitive extends AbstractConverter< Integer > {
    @Override
    public Integer convert( Method definingMethod, String input ) throws ConvertException {
      try {
        return Integer.parseInt( input ) ;
      } catch ( NumberFormatException e ) {
        throw new ConvertException( "Can't parse '" + input + "'" ) ;
      }
    }
  }

  public static class ConverterToIntegerObject extends AbstractConverter< Integer > {
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
          ( Class< ? > ) String.class, ( Converter ) new ConverterToString(),
          Integer.TYPE, new ConverterToIntegerPrimitive(),
          Integer.class, new ConverterToIntegerObject()
      )
  ;

}
