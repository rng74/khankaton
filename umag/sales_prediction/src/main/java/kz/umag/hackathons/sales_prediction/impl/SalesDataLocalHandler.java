package kz.umag.hackathons.sales_prediction.impl;

import kz.umag.hackathons.sales_prediction.api.SalesDataHandler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Scanner;

/**
 * Implementation of {@link SalesDataHandler} that uses local database.
 */
class SalesDataLocalHandler implements SalesDataHandler {

  private final ImmutableMap<Long, ImmutableMap<
      Long, ImmutableSortedMap<LocalDate, Integer>>> salesData;
  private final ImmutableMap<Long, ImmutableMap<Long, Double>> salesMean;

  @Inject
  public SalesDataLocalHandler(
      @Named("cli.small") boolean isSmall
  ) {
    ImmutableMap.Builder<Long, ImmutableMap<
        Long, ImmutableSortedMap<LocalDate, Integer>>> salesDataBuilder = ImmutableMap.builder();
    ImmutableMap.Builder<Long, ImmutableMap<Long, Double>> salesMeanBuilder =
        ImmutableMap.builder();

    // Helper structures
    Long prevStoreId = null;
    ImmutableMap.Builder<Long, ImmutableSortedMap<LocalDate, Integer>> storeProductSales =
        ImmutableMap.builder();
    ImmutableMap.Builder<Long, Double> storeProductMeanSales = ImmutableMap.builder();

    String dataFileName = String.format("%s_sales_data.txt", isSmall ? "small" : "full");

    try (Scanner fileScanner = new Scanner(getClass().getResourceAsStream(dataFileName))) {
      while (fileScanner.hasNextLine()) {
        try (Scanner lineScanner = new Scanner(fileScanner.nextLine())) {
          // Each line is guaranteed to start with a store id and barcode.
          Long storeId = lineScanner.nextLong();
          Long productBarcode = lineScanner.nextLong();

          if (prevStoreId != null && !storeId.equals(prevStoreId)) {
            salesDataBuilder.put(prevStoreId, storeProductSales.build());
            storeProductSales = ImmutableMap.builder();

            salesMeanBuilder.put(prevStoreId, storeProductMeanSales.build());
            storeProductMeanSales = ImmutableMap.builder();
          }
          prevStoreId = storeId;

          ImmutableSortedMap.Builder<LocalDate, Integer> salesBuilder =
              ImmutableSortedMap.naturalOrder();
          while (lineScanner.hasNext()) {
            // Each sale is guaranteed to have a date in ISO format and an integer value.
            salesBuilder.put(LocalDate.parse(lineScanner.next()), lineScanner.nextInt());
          }

          ImmutableSortedMap<LocalDate, Integer> sales = salesBuilder.build();
          storeProductSales.put(productBarcode, sales);

          Double mean = sales.values().parallelStream().mapToDouble(Integer::doubleValue)
              .sum() / sales.size();
          storeProductMeanSales.put(productBarcode, mean);
        }
      }
    }

    if (prevStoreId != null) {
      salesDataBuilder.put(prevStoreId, storeProductSales.build());
      salesMeanBuilder.put(prevStoreId, storeProductMeanSales.build());
    }

    salesData = salesDataBuilder.build();
    salesMean = salesMeanBuilder.build();
  }

  @Override
  public boolean hasData(Long storeId, Long productBarcode) {
    return salesData.containsKey(storeId)
        && salesData.get(storeId).containsKey(productBarcode)
        && !salesData.get(storeId).get(productBarcode).isEmpty();
  }

  @Override
  public OptionalInt getSales(Long storeId, Long productBarcode, LocalDate curDate) {
    return hasData(storeId, productBarcode) ?
        OptionalInt.of(salesData.get(storeId).get(productBarcode).getOrDefault(curDate, 0))
        : OptionalInt.empty();
  }

  @Override
  public OptionalDouble getSalesMean(Long storeId, Long productBarcode) {
    return hasData(storeId, productBarcode) ?
        OptionalDouble.of(salesMean.get(storeId).get(productBarcode))
        : OptionalDouble.empty();
  }

  @Override
  public ImmutableSet<Long> getAllStores() {
    return salesData.keySet();
  }

  @Override
  public ImmutableSet<Long> getAllProductsOfStore(Long storeId) {
    return salesData.getOrDefault(storeId, ImmutableMap.of()).keySet();
  }

  @Override
  public ImmutableSortedMap<LocalDate, Integer> getSalesSeries(Long storeId, Long productBarcode) {
    return salesData.getOrDefault(storeId, ImmutableMap.of())
        .getOrDefault(productBarcode, ImmutableSortedMap.of());
  }
}
