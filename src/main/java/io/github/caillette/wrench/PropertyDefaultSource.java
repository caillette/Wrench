package io.github.caillette.wrench;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Internal {@link Configuration.Source} that represents values set using
 * {@link Configuration.Annotations.DefaultValue}.
 *
 * @author Laurent Caillette
 */
class PropertyDefaultSource implements Configuration.Source {

  private final String sourceName ;
  private final ImmutableMap< String, String > map ;

  public PropertyDefaultSource(
      final Class<? extends Configuration> configurationClass,
      final ImmutableSet< ? extends Configuration.Property< ? >> properties
  ) {
    this.sourceName = "java:Annotations{" + configurationClass.getName() + "}" ;

    final ImmutableMap.Builder< String, String > stringMapBuilder = ImmutableMap.builder() ;

    for( final Configuration.Property property : properties ) {
      if( property.defaultValueAsString() != null ) {
        stringMapBuilder.put(
            property.name(),
            property.defaultValueAsString()
        ) ;
      }
    }
    this.map = stringMapBuilder.build() ;
  }

  @Override
  public ImmutableMap< String, String > map() {
    return map ;
  }

  @Override
  public String sourceName() {
    return sourceName ;
  }

}
