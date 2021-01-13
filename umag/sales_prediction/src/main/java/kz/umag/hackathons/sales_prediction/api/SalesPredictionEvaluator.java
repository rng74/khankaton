package kz.umag.hackathons.sales_prediction.api;

/**
 * Class that evaluates specific {@link SalesPredictor} implementations.
 */
public interface SalesPredictionEvaluator {

  /**
   * Returns evaluation results in a human-readable form.
   *
   * @return empty if the file with test cases does not exist.
   */
  String evaluate();
}
