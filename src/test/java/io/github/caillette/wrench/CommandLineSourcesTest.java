package io.github.caillette.wrench;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import io.github.caillette.wrench.source.CommandLineSources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class CommandLineSourcesTest {

  @Test
  public void multipleSources() throws Exception {
    final File file1 = folder.newFile( "1.properties" ) ;
    Files.write( "string=foo", file1, Charsets.UTF_8 ) ;
    final File file2 = folder.newFile( "2.properties" ) ;
    Files.write( "number=42", file2, Charsets.UTF_8 ) ;

    final ImmutableList< String > arguments = ImmutableList.of(
        "--configuration-files", file1.getAbsolutePath(), file2.getAbsolutePath(),
        "--number", "43",
        "--", "ignore"
    ) ;

    final Configuration.Factory< ConfigurationFixture.StringAndNumber > factory
        = ConfigurationTools.newAnnotationBasedFactory( ConfigurationFixture.StringAndNumber.class ) ;

    final ConfigurationFixture.StringAndNumber configuration
        = CommandLineSources.createConfiguration( factory, arguments ) ;

    assertThat( configuration.string() ).isEqualTo( "foo" ) ;
    assertThat( configuration.number() ).isEqualTo( 43 ) ;
  }

// =======
// Fixture
// =======

  @Rule
  public TemporaryFolder folder = new TemporaryFolder() ;
}
