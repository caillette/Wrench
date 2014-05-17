package io.github.caillette.wrench;

import java.lang.reflect.Method;

public class ConvertException extends ConfigurationException {
  public ConvertException( String message ) {
    super( message ) ;
  }

  public ConvertException( String message, Throwable cause ) {
    super( message, cause ) ;
  }

  static ConvertException toConvertException(
      final Exception e,
      Configuration.Property property,
      Configuration.Source source
  ) {
    return new ConvertException(
        "From " + property.converter().toString() + ": "
        + e.getMessage()
        + " for property '" + property.name()
        + "' — in " + source.sourceName() );
  }

  static ConvertException toConvertException(
      final Exception e,
      final Configuration.Converter converter,
      final Method method
  ) {
    return new ConvertException(
        "From " + converter.toString() + ": "
        + e.getMessage()
        + " for property '" + method.toGenericString()
    ) ;
  }
}
