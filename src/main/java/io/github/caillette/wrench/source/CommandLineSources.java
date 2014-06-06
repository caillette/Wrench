package io.github.caillette.wrench.source;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import io.github.caillette.wrench.Configuration;
import io.github.caillette.wrench.ConfigurationException;
import io.github.caillette.wrench.Sources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Composes a list of {@link Configuration.Source} objects from command-line parameters,
 * with the first parameters being (optionally) a reference to {@link PropertiesFileSource}s.
 * <p>
 * Considering this code:
 * <pre>
 * CommandLineSources.createConfiguration( factory, ImmutableList.of(
 *     "--configuration-files", "file1.properties", "file2.properties",
 *     "--key1", "value1", "--key2", "value2",
 *     "--", "ignored"
 * ) )
 * </pre>
 * It creates 3 sources, for {@code file1}, {@code file2}, and {@code key1->value1, key2->value2}
 * respectively. If several sources define different values for the same keys, the last wins.
 */
public final class CommandLineSources {

  private CommandLineSources() { }

  public static < C extends Configuration > C createConfiguration(
      final Configuration.Factory< C > factory,
      final ImmutableList< String > filenamesAndCommandLineArguments
  ) throws ConfigurationException, IOException {
    return createConfiguration(
        factory,
        "--configuration-files",
        "--",
        "--",
        filenamesAndCommandLineArguments
    ) ;
  }

  public static < C extends Configuration > C createConfiguration(
      final Configuration.Factory< C > factory,
      final String fileListArgumentName,
      final String propertyNameMarker,
      final String argumentListEndMarker,
      final ImmutableList< String > filenamesAndCommandLineArguments
  ) throws ConfigurationException, IOException {
    final Configuration.Source[] sources = createSources(
        fileListArgumentName,
        propertyNameMarker,
        argumentListEndMarker,
        filenamesAndCommandLineArguments
    ) ;
    final Configuration.Source[] rest = sources.length == 0
        ? new Configuration.Source[ 0 ]
        : new Configuration.Source[ sources.length - 1 ]
    ;
    System.arraycopy( sources, 1, rest, 0, sources.length - 1 ) ;
    return factory.create(
        sources[ 0 ],
        rest
    ) ;
  }

  public static Configuration.Source[] createSources(
      final String fileListArgumentName,
      final String propertyNameMarker,
      final String argumentListEndMarker,
      final ImmutableList< String > filenamesAndCommandLineArguments
  ) throws IOException {
    checkArgument( ! Strings.isNullOrEmpty( fileListArgumentName ) ) ;
    checkArgument( ! Strings.isNullOrEmpty( propertyNameMarker ) ) ;
    checkArgument( ! Strings.isNullOrEmpty( argumentListEndMarker ) ) ;
    final ImmutableList.Builder<File> filesBuilder = ImmutableList.builder() ;
    final ImmutableList.Builder< String > overridingProperties = ImmutableList.builder() ;
    int index = 0 ;
    while( index < filenamesAndCommandLineArguments.size() ) {
      String argument = filenamesAndCommandLineArguments.get( index ) ;
      if( argumentListEndMarker.equals( argument ) ) {
        break ;
      }
      if( index == 0 && fileListArgumentName.equals( argument ) ) {
        argument = filenamesAndCommandLineArguments.get( ++ index ) ;
        while( index < filenamesAndCommandLineArguments.size()
            && ! argumentListEndMarker.equals( argument )
            && ! argument.startsWith( propertyNameMarker )
        ) {
          filesBuilder.add( new File( argument ) ) ;
          argument = filenamesAndCommandLineArguments.get( ++ index ) ;
        }
      }
      while( index < filenamesAndCommandLineArguments.size()
          && ! argumentListEndMarker.equals( argument )
          ) {
        overridingProperties.add( argument ) ;
        argument = filenamesAndCommandLineArguments.get( ++ index ) ;
      }
    }

    final ImmutableList< File > files = filesBuilder.build() ;
    final ImmutableList< String > moreProperties = overridingProperties.build() ;
    final List< Configuration.Source > sources
        = new ArrayList<>( files.size() + moreProperties.size() ) ;
    for( final File file : files ) {
      sources.add( Sources.newSource( file ) ) ;
    }
    sources.add( Sources.newSource( moreProperties ) ) ;

    return sources.toArray( new Configuration.Source[ sources.size() ] ) ;
  }


}
