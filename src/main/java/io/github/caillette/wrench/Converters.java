package io.github.caillette.wrench;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

/**
 * Default {@link Configuration.Converter}s.
 */
public final class Converters {

  private Converters() { }

  public static class ConverterToString implements Configuration.Converter< String > {
    @Override
    public String convert( Class< String > targetClass, String input ) {
      return input ;
    }
  }

  public static class ConverterToIntegerPrimitive implements Configuration.Converter< Integer > {
    @Override
    public Integer convert( Class< Integer > targetClass, String input ) throws ConvertException {
      try {
        return Integer.parseInt( input ) ;
      } catch ( NumberFormatException e ) {
        throw new ConvertException( "Can't parse '" + input + "'" ) ;
      }
    }
  }

  public static class ConverterToIntegerObject implements Configuration.Converter< Integer > {
    @Override
    public Integer convert( Class< Integer > targetClass, String input ) throws ConvertException {
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

  public static final ImmutableMap< Class< ? >, Configuration.Converter > DEFAULTS
      = ImmutableMap.of(
          ( Class< ? > ) String.class, ( Configuration.Converter ) new ConverterToString(),
          Integer.TYPE, new ConverterToIntegerPrimitive(),
          Integer.class, new ConverterToIntegerObject()
      )
  ;

}
