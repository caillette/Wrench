package io.github.caillette.wrench;

import java.util.HashMap;
import java.util.Map;

import static io.github.caillette.wrench.Configuration.Property;
import static io.github.caillette.wrench.Configuration.Source;

/**
 * Base class for exceptions thrown when something goes bad.
 */
public class ConfigurationException extends Exception {

  public ConfigurationException( String message ) {
    super( message ) ;
  }

  public ConfigurationException( Iterable< ConfigurationException > causes ) {
    super( "Exception list: " + singleMessageFromExceptions( causes ) ) ;
  }

  private static String singleMessageFromExceptions( Iterable< ConfigurationException > causes ) {
    final StringBuilder stringBuilder = new StringBuilder() ;
    for( final ConfigurationException configurationException : causes ) {
      stringBuilder.append( "\n    " ) ;
      stringBuilder.append( configurationException.getClass().getSimpleName() ) ;
      stringBuilder.append( " - " ) ;
      stringBuilder.append( configurationException.getMessage() ) ;
    }
    return stringBuilder.toString() ;
  }

  public ConfigurationException( String message, Throwable cause ) {
    super( message, cause );
  }

  public ConfigurationException( Throwable cause ) {
    super( cause );
  }

  public static String singleMessageFromInfrigements( final Iterable< Validation.Bad > causes ) {
    final Map< Property, Source > valuedPropertiesWithSource = new HashMap<>() ;
    final StringBuilder stringBuilder = new StringBuilder() ;
    for( final Validation.Bad bad : causes ) {
      stringBuilder.append( "\n    " ) ;
      for( final ValuedProperty valuedProperty : bad.properties ) {
        if( valuedProperty.source != Sources.UNDEFINED  ) {
          valuedPropertiesWithSource.put( valuedProperty.property, valuedProperty.source ) ;
        }
        stringBuilder.append( "[ " ) ;
        stringBuilder.append( valuedProperty.property.name() ) ;
        if( valuedProperty.resolvedValue != ValuedProperty.NO_VALUE ) {
          stringBuilder.append( " = " ) ;
          stringBuilder.append( valuedProperty.resolvedValue == ValuedProperty.NULL_VALUE
              ? null : valuedProperty.resolvedValue ) ;
        }
        stringBuilder.append( " ] " ) ;
      }
      stringBuilder.append( bad.message ) ;

    }
    if( ! valuedPropertiesWithSource.isEmpty() ) {
      stringBuilder.append( "\n    Sources:" ) ;
      for( final Map.Entry< Property, Source > entries : valuedPropertiesWithSource.entrySet() ) {
        stringBuilder
            .append( "\n      " )
            .append( entries.getKey().name() )
            .append( " <- " )
            .append( entries.getValue()
            ) ;
      }
    }
    return stringBuilder.toString() ;
  }
}
