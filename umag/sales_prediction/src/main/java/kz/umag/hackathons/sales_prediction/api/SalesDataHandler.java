package kz.umag.hackathons.sales_prediction.api;

import java.time.LocalDate;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * API for handling database transactions of sales data.
 */
public interface SalesDataHandler {

  /**
   * Returns whether or not the database has data for the store-product pair.
   */
  boolean hasData(Long storeId, Long productBarcode);

  /**
   * Returns sales for the corresponding store and product.
   *
   * @return empty if {@code storeId} and/or {@code productBarcode} is not present in the database.
   */
  OptionalInt getSales(Long storeId, Long productBarcode, LocalDate curDate);

  /**
   * Returns average sales for the corresponding store and product.
   *
   * @return empty if {@code storeId} and/or {@code productBarcode} is not present in the database.
   */
  OptionalDouble getSalesMean(Long storeId, Long productBarcode);
}
