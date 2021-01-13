package kz.umag.hackathons.sales_prediction.impl;

import kz.umag.hackathons.sales_prediction.api.SalesDataHandler;
import kz.umag.hackathons.sales_prediction.api.SalesPredictor;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.OptionalInt;

/**
 * Implementation of {@link SalesPredictor} that makes custom predictions.
 */
class CustomPredictor implements SalesPredictor {

  private static final int PREDICTION_WINDOW = 5;

  private final SalesDataHandler salesDataHandler;

  @Inject
  public CustomPredictor(SalesDataHandler salesDataHandler) {
    this.salesDataHandler = salesDataHandler;
  }

  @Override
  public OptionalInt predict(Long storeId, Long productBarcode, LocalDate curDate) {
    // TODO: Replace the following with your solution.
    return OptionalInt.of(0);
  }
}
