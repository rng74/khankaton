package kz.umag.hackathons.sales_prediction.api;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;

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

  /**
   * Returns a list of all stores (IDs) present in the database.
   */
  ImmutableSet<Long> getAllStores();

  /**
   * Returns a list of all products (barcodes) for the corresponding store present in the database.
   *
   * <p>Warning: might return empty when the store is not present in the database or has no
   * products.</p>
   */
  ImmutableSet<Long> getAllProductsOfStore(Long storeId);

  /**
   * Returns a history of sales for the given store and product sorted by date.
   *
   * <p>Warning: might return empty when the store-product pair is not present in the database or
   * has no no history.</p>
   */
  ImmutableSortedMap<LocalDate, Integer> getSalesSeries(Long storeId, Long productBarcode);
}
