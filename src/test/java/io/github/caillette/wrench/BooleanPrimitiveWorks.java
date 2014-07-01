package io.github.caillette.wrench;

import org.junit.Test;

import static io.github.caillette.wrench.Configuration.Factory;
import static org.assertj.core.api.Assertions.assertThat;

public class BooleanPrimitiveWorks {

  public interface Simple extends Configuration {
    boolean trueOrFalse() ;
  }

  @Test
  public void test() throws Exception {
    final Factory< Simple > factory = new TemplateBasedFactory<Simple>( Simple.class )
    {
      @Override
      protected void initialize() {
        property( using.trueOrFalse() ).defaultValue( true ) ;
      }
    } ;


    final Simple simple = factory.create( Sources.newSource( "" ) ) ;
    assertThat( simple.trueOrFalse() ).isTrue() ;
  }
}
