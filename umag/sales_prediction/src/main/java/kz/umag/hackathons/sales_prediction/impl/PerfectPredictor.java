package kz.umag.hackathons.sales_prediction.impl;

import kz.umag.hackathons.sales_prediction.api.SalesDataHandler;
import kz.umag.hackathons.sales_prediction.api.SalesPredictor;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.OptionalInt;
import java.util.stream.LongStream;

/**
 * Implementation of {@link SalesPredictor} that makes perfect predictions.
 */
class PerfectPredictor implements SalesPredictor {

  private static final int PREDICTION_WINDOW = 5;

  private final SalesDataHandler salesDataHandler;

  @Inject
  public PerfectPredictor(SalesDataHandler salesDataHandler) {
    this.salesDataHandler = salesDataHandler;
  }

  @Override
  public OptionalInt predict(Long storeId, Long productBarcode, LocalDate curDate) {
    if (!salesDataHandler.hasData(storeId, productBarcode)) {
      return OptionalInt.empty();
    }

    return OptionalInt.of(LongStream.rangeClosed(1, PREDICTION_WINDOW).parallel()
        .mapToObj(days ->
            salesDataHandler.getSales(storeId, productBarcode, curDate.plusDays(days)))
        // At this point, we know for sure that the prediction is present.
        .filter(OptionalInt::isPresent)
        .mapToInt(OptionalInt::getAsInt)
        .sum());
  }
}
