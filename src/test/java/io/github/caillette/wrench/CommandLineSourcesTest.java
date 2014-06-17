package io.github.caillette.wrench;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import io.github.caillette.wrench.source.CommandLineSources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandLineSourcesTest {

  public interface Simple extends Configuration {
    int number() ;
  }

  @Test
  public void noFiles() throws Exception {

    final ImmutableList< String > arguments = ImmutableList.of(
        "--number", "43"
    ) ;

    final Configuration.Factory< Simple > factory = ConfigurationTools.newFactory( Simple.class ) ;

    final Simple configuration = CommandLineSources.createConfiguration( factory, arguments ) ;

    assertThat( configuration.number() ).isEqualTo( 43 ) ;
  }

// =======
// Fixture
// =======

}
