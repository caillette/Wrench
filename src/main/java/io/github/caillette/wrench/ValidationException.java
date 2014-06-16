package io.github.caillette.wrench;

import com.google.common.collect.ImmutableList;

/**
 * Thrown if values read from {@link Configuration.Source}s infrige some validation rules.
 *
 * @see io.github.caillette.wrench.TemplateBasedFactory#create(io.github.caillette.wrench.Configuration.Source, io.github.caillette.wrench.Configuration.Source...)
 * @see io.github.caillette.wrench.TemplateBasedFactory#validate(Configuration)
 */
public class ValidationException extends DeclarationException {

  public ValidationException(
      final Configuration.Factory factory,
      final ImmutableList< Validation.Bad > causes
  ) {
    super( factory, causes ) ;
  }


}
