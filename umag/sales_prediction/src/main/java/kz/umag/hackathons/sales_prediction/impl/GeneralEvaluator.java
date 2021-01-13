package kz.umag.hackathons.sales_prediction.impl;

import kz.umag.hackathons.sales_prediction.api.SalesDataHandler;
import kz.umag.hackathons.sales_prediction.api.SalesPredictionEvaluator;
import kz.umag.hackathons.sales_prediction.api.SalesPredictor;

import com.google.common.flogger.FluentLogger;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.stream.LongStream;

/**
 * Implementation of {@link SalesPredictionEvaluator} that evaluates {@link BaselinePredictor} and
 * {@link CustomPredictor}.
 */
class GeneralEvaluator implements SalesPredictionEvaluator {

  private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();

  private final SalesDataHandler salesDataHandler;
  private final SalesPredictor targetPredictor;
  private final SalesPredictor baselinePredictor;
  private final SalesPredictor solutionPredictor;
  private final String testsFilePath;

  @Inject
  public GeneralEvaluator(
      SalesDataHandler salesDataHandler,
      @Named("target") SalesPredictor targetPredictor,
      @Named("baseline") SalesPredictor baselinePredictor,
      @Named("solution") SalesPredictor solutionPredictor,
      @Named("cli.small") boolean small
  ) {
    this.salesDataHandler = salesDataHandler;
    this.targetPredictor = targetPredictor;
    this.baselinePredictor = baselinePredictor;
    this.solutionPredictor = solutionPredictor;
    this.testsFilePath = String.format("%s_tests.txt", small ? "small" : "full");
  }

  @Override
  public String evaluate() {
    OptionalDouble baselineResult = getEvaluationResult(baselinePredictor);
    OptionalDouble solutionResult = getEvaluationResult(solutionPredictor);

    return baselineResult.isPresent() && solutionResult.isPresent() ?
        String.format(
            "Scores (lower is better)\nBaseline: %f\nSolution: %f",
            baselineResult.getAsDouble(), solutionResult.getAsDouble())
        : "";
  }

  private OptionalDouble getEvaluationResult(SalesPredictor predictor) {
    int numberOfTests = 0;
    double errorSum = 0.0;

    try (Scanner fileScanner = new Scanner(getClass().getResourceAsStream(testsFilePath))) {
      // First line is guaranteed to have test start and end dates.
      Scanner lineScanner = new Scanner(fileScanner.nextLine());

      LocalDate startDate = LocalDate.parse(lineScanner.next());
      LocalDate endDate = LocalDate.parse(lineScanner.next());

      lineScanner.close();

      while (fileScanner.hasNextLine()) {
        lineScanner = new Scanner(fileScanner.nextLine());

        Long storeId = lineScanner.nextLong();
        Long productBarcode = lineScanner.nextLong();

        lineScanner.close();

        OptionalDouble normalizer = salesDataHandler.getSalesMean(storeId, productBarcode);
        if (normalizer.isEmpty()) {
          LOGGER.atWarning()
              .log(
                  "Store [%d] and/or product [%d] is not present in the database",
                  storeId, productBarcode);
          return OptionalDouble.empty();
        }

        double[] diffs = LongStream.range(0, ChronoUnit.DAYS.between(startDate, endDate))
            .parallel()
            .mapToObj(
                days -> {
                  OptionalInt target =
                      targetPredictor.predict(storeId, productBarcode, startDate.plusDays(days));
                  OptionalInt prediction =
                      predictor.predict(storeId, productBarcode, startDate.plusDays(days));

                  if (target.isPresent() && prediction.isPresent()) {
                    return OptionalDouble.of(Math.abs(
                        target.getAsInt() - prediction.getAsInt()) / normalizer.getAsDouble());
                  }
                  return OptionalDouble.empty();
                })
            .filter(OptionalDouble::isPresent)
            .mapToDouble(OptionalDouble::getAsDouble)
            .toArray();

        numberOfTests += diffs.length;
        errorSum += Arrays.stream(diffs).parallel().sum();
      }
    }

    return numberOfTests > 0 ? OptionalDouble.of(errorSum / numberOfTests) : OptionalDouble.empty();
  }
}
