package kz.umag.hackathons.sales_prediction;

import de.bwaldvogel.liblinear.Train;
import kz.umag.hackathons.sales_prediction.api.SalesPredictionEvaluator;
import kz.umag.hackathons.sales_prediction.impl.SalesDataLocalHandler;
import kz.umag.hackathons.sales_prediction.impl.SalesDataModule;
import kz.umag.hackathons.sales_prediction.impl.SalesPredictionModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

import net.israfil.gcommander.JCommanderModuleBuilder;

/**
 * Starter class of the application.
 */
public class App {

    public static void main(String[] args) {

        Injector injector = Guice.createInjector(
                JCommanderModuleBuilder
                        .bindParameters(Params.class)
                        .withPrefix("cli.")
                        .withArguments(args)
                        .build(),
                new SalesDataModule(),
                new SalesPredictionModule()
        );

        SalesPredictionEvaluator evaluator = injector.getInstance(SalesPredictionEvaluator.class);

//        ExtractData extractData = new ExtractData(new SalesDataLocalHandler(true));
//        extractData.extract();

//        CustomTrain tmp = new CustomTrain(new SalesDataLocalHandler(true));
//        tmp.main();

        System.out.println(evaluator.evaluate());
    }
}
