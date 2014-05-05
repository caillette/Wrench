package io.github.caillette.wrench;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.caillette.wrench.source.DashedCommandLineSource;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Laurent Caillette
 */
public class DashedCommandLineSourceTest {

  @Test
  public void simpleParse() throws Exception {
    final DashedCommandLineSource dashedCommandLineSource
        = new DashedCommandLineSource( ImmutableList.of(
            "--file-name", "my/file.txt",
            "--timeout", "2000"
        )
    ) ;
    assertThat( dashedCommandLineSource.map() ).isEqualTo( ImmutableMap.of(
        "file-name", "my/file.txt",
        "timeout", "2000"
    ) ) ;

  }
}
