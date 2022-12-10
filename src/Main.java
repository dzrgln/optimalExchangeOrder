package src;

import src.entity.RequestDTOCustomerOrder;
import src.entity.StringGlass;


import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.time.Duration;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String name = "test.csv";
        Map<Timestamp, StringGlass> listFromFile = Utility.readFile(new File(name));
//        OptimalOrderInstaller optimalOrderInstaller = new OptimalOrderInstaller(3,
//                0.02, 10);
//        RequestDTOCustomerOrder requestOrder = new RequestDTOCustomerOrder(6420,
//                200000,
//                true,
//                new Timestamp(1585780984000L),
//                Duration.ofMillis(10800000L),
//                true);

//        System.out.println(optimalOrderInstaller.requestOrderToOrder(requestOrder, listFromFile));


        System.out.println(listFromFile.entrySet());

    }
}

