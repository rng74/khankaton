package kz.umag.hackathons.sales_prediction;

import com.google.common.collect.ImmutableSortedMap;
import kz.umag.hackathons.sales_prediction.api.SalesDataHandler;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class ExtractData {

    private final SalesDataHandler salesDataHandler;

    @Inject
    public ExtractData(SalesDataHandler salesDataHandler) {
        this.salesDataHandler = salesDataHandler;
    }

    public void extract() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("D://data"));
            for (Long storeId : salesDataHandler.getAllStores()) {
                for (Long barcode : salesDataHandler.getAllProductsOfStore(storeId)) {
                    if (salesDataHandler.hasData(storeId, barcode)) {
                        ImmutableSortedMap<LocalDate, Integer> map = salesDataHandler.getSalesSeries(storeId, barcode);
                        for (LocalDate date : map.keySet()) {
                            writer.write(storeId + " " + barcode + " " + date.toString() + " " + map.get(date));
                            writer.newLine();
                        }
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
