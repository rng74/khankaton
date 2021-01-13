package kz.umag.hackathons.sales_prediction.impl;

import kz.umag.hackathons.sales_prediction.api.SalesPredictionEvaluator;
import kz.umag.hackathons.sales_prediction.api.SalesPredictor;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Bindings for different implementations of {@link SalesPredictor}.
 */
public final class SalesPredictionModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(SalesPredictor.class).annotatedWith(Names.named("target")).to(PerfectPredictor.class);
    bind(SalesPredictor.class).annotatedWith(Names.named("baseline")).to(BaselinePredictor.class);
    bind(SalesPredictor.class).annotatedWith(Names.named("solution")).to(CustomPredictor.class);
    bind(SalesPredictionEvaluator.class).to(GeneralEvaluator.class);
  }
}
