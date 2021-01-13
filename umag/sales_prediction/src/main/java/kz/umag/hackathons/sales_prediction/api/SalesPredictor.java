package kz.umag.hackathons.sales_prediction.api;

import java.time.LocalDate;
import java.util.OptionalInt;

/**
 * API for predicting sales.
 */
public interface SalesPredictor {

  /**
   * Returns an aggregate 5-day-sale prediction for the corresponding store, product and date.
   *
   * @return empty if {@code storeId} and/or {@code productBarcode} is not present in the database.
   */
  OptionalInt predict(Long storeId, Long productBarcode, LocalDate curDate);
}
