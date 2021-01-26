package kz.umag.hackathons.sales_prediction;

import com.google.common.collect.ImmutableSortedMap;
import de.bwaldvogel.liblinear.*;
import kz.umag.hackathons.sales_prediction.api.SalesDataHandler;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.primitives.Longs.max;

public class CustomTrain {

    private final SalesDataHandler salesDataHandler;

    @Inject
    public CustomTrain(SalesDataHandler salesDataHandler) {
        this.salesDataHandler = salesDataHandler;
    }

    public static FeatureNode[] convertNodes(List<FeatureNode> integers) {
        FeatureNode[] ret = new FeatureNode[integers.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = integers.get(i);
        }
        return ret;
    }

    public void main() {
        Problem problem = new Problem();
        problem.n = 4;

        SolverType solver = SolverType.L2R_LR;
        double C = 1.0;    // cost of constraints violation
        double eps = 0.01; // stopping criteria

        Parameter parameter = new Parameter(solver, C, eps);

        ArrayList<List<FeatureNode>> list = new ArrayList<>();

        List<Double> yList = new ArrayList<>();

        long maxAmountBetween = 0L;

        for (Long storeId : salesDataHandler.getAllStores()) {
            for (Long barcode : salesDataHandler.getAllProductsOfStore(storeId)) {
                long lastDay = 0L;
                if (salesDataHandler.hasData(storeId, barcode)) {
                    ImmutableSortedMap<LocalDate, Integer> map = salesDataHandler.getSalesSeries(storeId, barcode);
                    for (LocalDate date : map.keySet()) {
                        if (lastDay != 0L) {
                            long amountOfDayBetween = (date.toEpochDay() - lastDay - 1);
                            maxAmountBetween = max(maxAmountBetween, amountOfDayBetween);
//                            amountOfDayBetween /= 2;
                            for (int i = 1; i <= amountOfDayBetween; i++) {
                                yList.add(0.0);
                                List<FeatureNode> tmpList = new ArrayList<>();
                                tmpList.add(new FeatureNode(1, storeId));
                                tmpList.add(new FeatureNode(2, barcode));
                                tmpList.add(new FeatureNode(3, date.minusDays(i).getDayOfWeek().getValue()));
                                tmpList.add(new FeatureNode(4, date.minusDays(i).getDayOfMonth()));
                                list.add(tmpList);
                            }
                        }
                        yList.add((double) map.get(date));
                        List<FeatureNode> tmpList = new ArrayList<>();
                        tmpList.add(new FeatureNode(1, storeId));
                        tmpList.add(new FeatureNode(2, barcode));
                        tmpList.add(new FeatureNode(3, date.getDayOfWeek().getValue()));
                        tmpList.add(new FeatureNode(4, date.getDayOfMonth()));
                        list.add(tmpList);

                        lastDay = date.toEpochDay();
                    }
                }
            }
        }
        double[] yTmp = new double[yList.size()];
        for (int i = 0; i < yList.size(); i++) {
            yTmp[i] = yList.get(i);
        }

        problem.y = yTmp;
        problem.l = yList.size();


        FeatureNode[][] nodes = new FeatureNode[yList.size()][problem.n];

        int i = 0;
        for (List<FeatureNode> featureNodes : list) {
            nodes[i] = convertNodes(featureNodes);
            i++;
        }
        problem.x = nodes;

        Model model = Linear.train(problem, parameter);

        File modelFile = new File("D://model2");

        System.out.println("Max amount between -> " + maxAmountBetween);

        try {
            model.save(modelFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
