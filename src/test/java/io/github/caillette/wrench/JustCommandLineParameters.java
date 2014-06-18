package io.github.caillette.wrench;

import com.google.common.collect.ImmutableList;
import io.github.caillette.wrench.source.CommandLineSources;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JustCommandLineParameters {

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
