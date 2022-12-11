package src;

import src.entity.*;

import java.io.File;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String name = "test.csv";
        Map<Timestamp, StringGlass> mapFromFile = Repository.readFile(new File(name));
        OrderInstaller orderInstaller = new OrderInstaller(3,
                0.02, Duration.ofMillis(1000000));
        RequestDTOCustomerOrder requestOrder = new RequestDTOCustomerOrder(6350,
                200000,
                true,
                new Timestamp(1585714695000L),
                Duration.ofMillis(72000000L),
                true);
        System.out.println(requestOrder);
        List<ClientTransaction> listForOptimalOrder = orderInstaller.makeOptimalOrder(requestOrder, mapFromFile);
        List<ClientTransaction> listForMarketOrder = orderInstaller.makeMarketOrder(requestOrder, mapFromFile);
        System.out.println("Optimal orders:" + listForOptimalOrder);
        System.out.println("Market orders:" + listForMarketOrder);

        double averagePriceForOptimalOrder = orderInstaller.getAveragePrice(listForOptimalOrder);
        double averagePriceForMarketOrder = orderInstaller.getAveragePrice(listForMarketOrder);
        System.out.println("Average price for optimal order:" + averagePriceForOptimalOrder);
        System.out.println("Average price for market order:" + averagePriceForMarketOrder);

        System.out.println("Profit:" + Math.ceil((averagePriceForMarketOrder - averagePriceForOptimalOrder)
                / averagePriceForMarketOrder * 100 * 1000) / 1000 + "%");


       System.out.println(mapFromFile.entrySet());

    }
}

