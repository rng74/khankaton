package kz.umag.hackathons.sales_prediction.impl;

import kz.umag.hackathons.sales_prediction.api.SalesDataHandler;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Scanner;

/**
 * Implementation of {@link SalesDataHandler} that uses local database.
 */
class SalesDataLocalHandler implements SalesDataHandler {

  private final Map<String, Map<LocalDate, Integer>> salesData;
  private final Map<String, Double> salesMean;

  @Inject
  public SalesDataLocalHandler(
      @Named("cli.small") boolean small
  ) {
    ImmutableMap.Builder<String, Map<LocalDate, Integer>> salesDataBuilder = ImmutableMap.builder();
    ImmutableMap.Builder<String, Double> salesMeanBuilder = ImmutableMap.builder();

    String dataFileName = String.format("%s_sales_data.txt", small ? "small" : "full");

    try (Scanner fileScanner = new Scanner(getClass().getResourceAsStream(dataFileName))) {
      while (fileScanner.hasNextLine()) {
        try (Scanner lineScanner = new Scanner(fileScanner.nextLine())) {
          // Each line is guaranteed to start with a store id and barcode.
          Long storeId = lineScanner.nextLong();
          Long productBarcode = lineScanner.nextLong();

          ImmutableMap.Builder<LocalDate, Integer> salesBuilder = ImmutableMap.builder();
          while (lineScanner.hasNext()) {
            // Each sale is guaranteed to have a date in ISO format and an integer value.
            salesBuilder.put(LocalDate.parse(lineScanner.next()), lineScanner.nextInt());
          }

          String id = getCombinedId(storeId, productBarcode);
          ImmutableMap<LocalDate, Integer> sales = salesBuilder.build();
          Double mean = sales.values().parallelStream().mapToDouble(Integer::doubleValue)
              .sum() / sales.size();

          salesDataBuilder.put(id, sales);
          salesMeanBuilder.put(id, mean);
        }
      }
    }

    salesData = salesDataBuilder.build();
    salesMean = salesMeanBuilder.build();
  }

  @Override
  public boolean hasData(Long storeId, Long productBarcode) {
    return salesData.containsKey(getCombinedId(storeId, productBarcode));
  }

  @Override
  public OptionalInt getSales(Long storeId, Long productBarcode, LocalDate curDate) {
    return hasData(storeId, productBarcode) ?
        OptionalInt.of(
            salesData.get(getCombinedId(storeId, productBarcode)).getOrDefault(curDate, 0))
        : OptionalInt.empty();
  }

  @Override
  public OptionalDouble getSalesMean(Long storeId, Long productBarcode) {
    return hasData(storeId, productBarcode) ?
        OptionalDouble.of(
            salesMean.get(getCombinedId(storeId, productBarcode)))
        : OptionalDouble.empty();
  }

  private static String getCombinedId(Long storeId, Long productBarcode) {
    return String.format("%d-%d", storeId, productBarcode);
  }
}
