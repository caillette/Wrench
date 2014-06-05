package io.github.caillette.wrench;

import com.google.common.base.CaseFormat;

/**
 * Class to reference using the
 * {@link io.github.caillette.wrench.Configuration.Annotations.TransformName} annotation.
 */
public final class NameTransformers {

  private NameTransformers() { }

  public static class WithCaseFormat implements Configuration.NameTransformer {
    private final CaseFormat caseFormat ;

    protected WithCaseFormat( CaseFormat caseFormat ) {
      this.caseFormat = caseFormat ;
    }

    @Override
    public String transform( String javaMethodName ) {
      return CaseFormat.LOWER_CAMEL.to( caseFormat, javaMethodName ) ;
    }
  }

  public static final Configuration.NameTransformer LOWER_HYPHEN = new LowerHyphen() ;

  public static class LowerHyphen extends WithCaseFormat {
    public LowerHyphen() {
      super( CaseFormat.LOWER_HYPHEN ) ;
    }
  }

  public static final Configuration.NameTransformer LOWER_DOT = new LowerDot() ;

  public static class LowerDot implements Configuration.NameTransformer {
    private static final Configuration.NameTransformer LOWER_HYPHEN = new LowerHyphen() ;
    @Override
    public String transform( String javaMethodName ) {
      return LOWER_HYPHEN.transform( javaMethodName ).replace( '-', '.' ) ;
    }
  }

  public static final Configuration.NameTransformer IDENTITY = new Configuration.NameTransformer() {
    @Override
    public String transform( String javaMethodName ) {
      return javaMethodName ;
    }
  };


}
