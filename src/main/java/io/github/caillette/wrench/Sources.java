package io.github.caillette.wrench;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.caillette.wrench.source.DashedCommandLineSource;
import io.github.caillette.wrench.source.PropertiesFileSource;
import io.github.caillette.wrench.source.StringSource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Static methods for quick creation of {@link Configuration.Source} objects.
 */
public final class Sources {

  private Sources() { }

  public static Configuration.Source newSource( final String... propertiesObjectLines ) {
    final String singlePropertiesObject = Joiner.on( "\n" ).join( propertiesObjectLines ) ;
    return new StringSource( singlePropertiesObject ) ;
  }

  public static Configuration.Source newSource(
      final ImmutableList< String > commandLineArguments
  ) {
    return new DashedCommandLineSource( commandLineArguments ) ;
  }

  public static Configuration.Source newSource( final File file ) 
      throws IOException 
  {
    return new PropertiesFileSource( file ) ;
  }


}
