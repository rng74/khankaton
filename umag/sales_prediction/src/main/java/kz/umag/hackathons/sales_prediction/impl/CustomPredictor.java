package kz.umag.hackathons.sales_prediction.impl;

import com.google.common.collect.ImmutableSortedMap;
import de.bwaldvogel.liblinear.*;
import kz.umag.hackathons.sales_prediction.api.SalesDataHandler;
import kz.umag.hackathons.sales_prediction.api.SalesPredictor;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.LongStream;

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

//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter("D://data_test.csv", true));
//            writer.write(storeId + " " + productBarcode + " " + curDate.toString());
//            writer.newLine();
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if (!salesDataHandler.hasData(storeId, productBarcode))
            return OptionalInt.empty();
/*   this is ML predictor
        Model model;
        File modelFile = new File("D://model2");
        double result = 0f;

        try {
            model = Model.load(modelFile.toPath());

            for (int i = 0; i < PREDICTION_WINDOW; i++) {
                Feature[] instance = {
                        new FeatureNode(1, storeId.intValue()),
                        new FeatureNode(2, productBarcode.intValue()),
                        new FeatureNode(3, curDate.plusDays(i).getDayOfWeek().getValue()),
                        new FeatureNode(4, curDate.plusDays(i).getDayOfMonth())
                };
                double predictedAmount = Linear.predict(model, instance);
                System.out.println("!CHECK! - " + salesDataHandler.getSales(storeId, productBarcode, curDate) + " || predictedAmount - " + predictedAmount);
                result += predictedAmount;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return OptionalInt.of((int) Math.round(result));*/

        double result = 0.0f;
        double[] coef = {0.3, 0.2, 0.125, 0.1, 0.062, 0.05, 0.025, 0.012, 0.001};

        long curM = 0L;
        for (int i = 0, j = 0; j < coef.length; i++) {
            curM += salesDataHandler.getSales(storeId, productBarcode, curDate.minusDays(i)).orElse(0);
            if ((i + 1) % 5 == 0) {
                result += (coef[j] * curM);
                j++;
                curM = 0;
            }
        }
        return OptionalInt.of((int) result);
    }
}
