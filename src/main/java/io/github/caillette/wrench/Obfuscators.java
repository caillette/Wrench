package io.github.caillette.wrench;

import com.google.common.collect.ImmutableList;

import java.util.regex.Pattern;

/**
 * Creates {@link io.github.caillette.wrench.Configuration.Obfuscator}s.
 */
public final class Obfuscators {

  private Obfuscators() { }

  public static Configuration.Obfuscator from( final Pattern first, final Pattern... rest ) {
    final ImmutableList< Pattern > patterns = ImmutableList.< Pattern >builder()
        .add( first ).add( rest ).build() ;
    return new Configuration.Obfuscator() {
      @Override
      public String obfuscate( String propertyAsString, String replacement ) {
        for( final Pattern pattern : patterns ) {
          if( pattern.matcher( propertyAsString ).find() ) {
            return propertyAsString.replaceAll( pattern.pattern(), replacement ) ;
          }
        }
        return propertyAsString ;
      }
    } ;
  }

}
