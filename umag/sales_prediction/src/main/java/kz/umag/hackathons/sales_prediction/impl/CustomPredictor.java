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
    private static final int DEFAULT_VAL = 0;

    private final SalesDataHandler salesDataHandler;

    @Inject
    public CustomPredictor(SalesDataHandler salesDataHandler) {
        this.salesDataHandler = salesDataHandler;
    }

    @Override
    public OptionalInt predict(Long storeId, Long productBarcode, LocalDate curDate) {
        if (!salesDataHandler.hasData(storeId, productBarcode))
            return OptionalInt.empty();

        double result = 0.0f;
        double[] magicCoefficients = {0.34, 0.16, 0.12, 0.09, 0.062, 0.05, 0.02, 0.02, 0.01}; // 0.375

        long curWeek = DEFAULT_VAL;
        for (int i = 0, j = 0; j < magicCoefficients.length; i++) {
            curWeek += salesDataHandler
                    .getSales(storeId, productBarcode, curDate.minusDays(i))
                    .orElse(DEFAULT_VAL);
            if ((i + 1) % PREDICTION_WINDOW == 0) {
                result += (magicCoefficients[j] * curWeek);
                j++;
                curWeek = DEFAULT_VAL;
            }
        }
        return OptionalInt.of((int) result);
    }
}
