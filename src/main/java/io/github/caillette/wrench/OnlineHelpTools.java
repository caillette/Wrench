package io.github.caillette.wrench;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

public final class OnlineHelpTools {

  private OnlineHelpTools() { }

  private static final int INDENT = 2 ;
  private static final int LINE_LENGTH = 80 ;

  public static String helpAsString( final Configuration.Factory< ? > factory ) {
    return helpAsString( factory, INDENT, LINE_LENGTH ) ;
  }
  public static String helpAsString(
      final Configuration.Factory< ? > factory,
      final int indent,
      final int lineLength
  ) {
    final StringWriter stringWriter = new StringWriter() ;
    try {
      writeHelp( stringWriter, factory, indent, lineLength ) ;
    } catch ( final IOException e ) {
      throw new RuntimeException( "Can't happen", e ) ;
    }
    return stringWriter.toString() ;
  }

  public static String errorMessageAndHelpAsString(
      final DeclarationException declarationException
  ) {
    return errorMessageAndHelpAsString( declarationException, INDENT, LINE_LENGTH ) ;
  }

  public static String errorMessageAndHelpAsString(
      final DeclarationException declarationException,
      final int indent,
      final int lineLength
  ) {
    final StringWriter stringWriter = new StringWriter() ;
    try {
      writeErrorMessageAndHelp( stringWriter, declarationException, indent, lineLength ) ;
    } catch ( final IOException e ) {
      throw new RuntimeException( "Can't happen", e ) ;
    }
    return stringWriter.toString() ;
  }

  public static void writeErrorMessageAndHelp(
      final Writer writer,
      final DeclarationException declarationException
  ) throws IOException {
    writeErrorMessageAndHelp( writer, declarationException, INDENT, LINE_LENGTH ) ;
  }

  public static void writeErrorMessageAndHelp(
      final Writer writer,
      final DeclarationException declarationException,
      final int indent,
      final int lineLength
  ) throws IOException {
    writeWrapped(
        writer,
        "Could not create a " + Configuration.class.getSimpleName() + " from "
            + ConfigurationTools.getNiceName( declarationException.factory.configurationClass() ),
        0,
        lineLength
    ) ;
    writeExceptionOnly( writer, declarationException, indent, lineLength ) ;
    writeWrapped( writer, "\nUsage:", 0, lineLength ) ;
    writeHelp( writer, declarationException.factory, indent, lineLength ) ;
  }

  public static String exceptionAsMultilineString(
      final DeclarationException declarationException
  ) {
    final StringWriter writer = new StringWriter() ;
    try {
      writeExceptionOnly( writer, declarationException, 0, LINE_LENGTH ) ;
    } catch ( IOException e ) {
      throw new RuntimeException( "Should not happen", e ) ;
    }
    return writer.toString() ;
  }

  public static String causesAsMultilineString( final ImmutableList< Validation.Bad > causes ) {
    final StringWriter writer = new StringWriter() ;
    try {
      writeWrapped( writer, causes, 0, LINE_LENGTH ) ;
    } catch ( IOException e ) {
      throw new RuntimeException( "Should not happen", e ) ;
    }
    return writer.toString() ;
  }

  private static void writeExceptionOnly(
      final Writer writer,
      final DeclarationException declarationException,
      final int indent,
      final int lineLength
  ) throws IOException {
    if( declarationException.causes.isEmpty() ) {
      writeWrapped( writer, declarationException.getMessage(), indent, lineLength ) ;
    } else {
      final ImmutableList< Validation.Bad > causes = declarationException.causes ;
      writeWrapped( writer, causes, indent, lineLength ) ;
    }
  }

  private static void writeWrapped(
      final Writer writer,
      final ImmutableList< Validation.Bad > causes,
      final int indent,
      final int lineLength
  ) throws IOException {
    final Map< Configuration.Property, Configuration.Source > valuedPropertiesWithSource
        = new HashMap<>() ;
    for( final Validation.Bad bad : causes ) {
      final StringBuilder lineBuilder = new StringBuilder() ;
      for( final ValuedProperty valuedProperty : bad.properties ) {
        if( valuedProperty.source != Sources.UNDEFINED  ) {
          valuedPropertiesWithSource.put( valuedProperty.property, valuedProperty.source ) ;
        }
        lineBuilder.append( "[ " ) ;
        lineBuilder.append( valuedProperty.property.name() ) ;
        if( valuedProperty.resolvedValue != ValuedProperty.NO_VALUE ) {
          lineBuilder.append( " = " ) ;
          lineBuilder.append( valuedProperty.resolvedValue == ValuedProperty.NULL_VALUE
              ? "null" : valuedProperty.stringValue ) ;
        }
        lineBuilder.append( " ] " ) ;
      }
      lineBuilder.append( bad.message ) ;
      writeWrapped( writer, lineBuilder.toString(), indent, lineLength ) ;

    }
    if( ! valuedPropertiesWithSource.isEmpty() ) {
      writeWrapped(
          writer,
          "\nSource" + ( valuedPropertiesWithSource.entrySet().size() > 1 ? "s:" : ":" ),
          indent,
          lineLength
      ) ;
      for( final Map.Entry<Configuration.Property, Configuration.Source> entries
          : valuedPropertiesWithSource.entrySet()
          ) {
        writeWrapped(
            writer,
            entries.getKey().name() + " <- " + entries.getValue().sourceName(),
            indent * 2,
            lineLength
        ) ;
      }
    }
  }

  public static void writeHelp(
      final Writer writer,
      final Configuration.Factory< ? > factory
  ) throws IOException {
    writeHelp( writer, factory, INDENT, LINE_LENGTH ) ;
  }

  public static void writeHelp(
      final Writer writer,
      final Configuration.Factory< ? > factory,
      final int indent,
      final int lineLength
  ) throws IOException {
    final String leftPadding1 = Strings.repeat( " ", indent ) ;
    final String leftPadding2 = Strings.repeat( " ", indent * 2 ) ;

    for( final Configuration.Property< ? > property : factory.properties().values() ) {
      writer.append( "\n" ) ;
      writer.append( leftPadding1 ) ;
      writer.append( property.name() ) ;
      writer.append( "\n" ) ;
      final String defaultValueAsString = property.defaultValueAsString() ;
      if( defaultValueAsString != null ) {
        writer.append( leftPadding2 ) ;
        writer.append( "Default value: '" ) ;
        writer.append( defaultValueAsString ) ;
        writer.append( "'\n" ) ;
      }
      if( defaultValueAsString == null && ! property.maybeNull() ) {
        writer.append( leftPadding2 ) ;
        writer.append( "(Requires explicit value)" ) ;
        writer.append( "\n" ) ;
      }
      final String documentation = property.documentation() ;
      if( ! Strings.isNullOrEmpty( documentation ) ) {
        writeWrapped( writer, documentation, indent * 2, lineLength ) ;
      }
    }
  }

  private static void writeWrapped(
      final Writer writer,
      final String text,
      final int indent,
      final int width
  ) throws IOException {
    final Iterable< String > lines = Splitter.on( LINEBREAK_MATCHER ).split( text ) ;
    for( final String line : lines ) {
      writeWrapped( writer, Splitter.on( WHITESPACE_MATCHER ).split( line ), indent, width ) ;
      writer.append( "\n" ) ;
    }
  }

  /**
   * Inspired by <a href="http://stackoverflow.com/a/5689524" >StackOverflow</a>.
   */
  private static void writeWrapped(
      final Writer writer,
      final Iterable< String > words,
      final int indent,
      final int width
  ) throws IOException {
    int lineLength = 0 ;
    final String leftPadding = Strings.repeat( " ", indent ) ;
    writer.append( leftPadding ) ;
    final Iterator< String > iterator = words.iterator() ;
    if( iterator.hasNext() ) {
      final String next = iterator.next() ;
      writer.append( next ) ;
      lineLength += next.length() ;
      while( iterator.hasNext() ) {
        final String word = iterator.next() ;
        if( word.length() + 1 + lineLength > width ) {
          writer.append( '\n' ) ;
          writer.append( leftPadding ) ;
          lineLength = 0 ;
        } else {
          lineLength++ ;
          writer.append( ' ' ) ;
        }
        writer.append( word ) ;
        lineLength += word.length() ;
      }
    }
  }

  private static final CharMatcher WHITESPACE_MATCHER = CharMatcher.WHITESPACE ;

  private static final CharMatcher LINEBREAK_MATCHER = new CharMatcher() {
    @Override
    public boolean matches( final char c ) {
      return ( c == '\n' || c == '\r' ) ;
    }
  } ;


}
