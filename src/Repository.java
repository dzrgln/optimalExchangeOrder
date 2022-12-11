package src;

import src.entity.ExchangeApplication;
import src.entity.MarketPrice;
import src.entity.StringGlass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Repository {
    public static Map<Timestamp, StringGlass> readFile(File file) {
        Map<Timestamp, StringGlass> strings = new TreeMap<>();
        try (FileReader reader = new FileReader(file)) {
            BufferedReader br = new BufferedReader(reader);
            while (br.ready()) {
                String line = br.readLine();
                if (!line.isEmpty()) {
                    //      System.out.println(line.split(",")[0]);
                    if (!line.split(",")[0].equals("exchange")) {
                        String[] arrayFromString = line.split(",");
                        List<ExchangeApplication> askExchangeApplicationList = new ArrayList<>();
                        List<ExchangeApplication> bidExchangeApplicationList = new ArrayList<>();
                        // create entity for one string of Exchange Glass
                        StringGlass stringGlass = new StringGlass(
                                null
                                , askExchangeApplicationList
                                , bidExchangeApplicationList
                        );
                        // fill in all orders in one string of Exchange Glass
                        //for ask-orders
                        for (int i = 4; i < arrayFromString.length; i = i + 4) {
                            ExchangeApplication exchangeApplication = new ExchangeApplication(
                                    Integer.parseInt(arrayFromString[i + 1]),
                                    Double.parseDouble(arrayFromString[i])
                            );
                            stringGlass.getAskExchangeApplicationList().add(exchangeApplication);
                        }
                        //for bid-orders
                        for (int i = 6; i < arrayFromString.length; i = i + 4) {
                            ExchangeApplication exchangeApplication = new ExchangeApplication(
                                    Integer.parseInt(arrayFromString[i + 1]),
                                    Double.parseDouble(arrayFromString[i])
                            );
                            stringGlass.getBidExchangeApplicationList().add(exchangeApplication);
                        }
                        // Assign market price
                        stringGlass.setMarketPrice(getMarketPrice(stringGlass));
                        strings.put(new Timestamp(Long.parseLong(arrayFromString[3]) * 1000), stringGlass);
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Произошла ошибка во время чтения файла.");
        }
        return strings;
    }

    private static MarketPrice getMarketPrice(StringGlass stringGlass) {
        return new MarketPrice(stringGlass.getAskExchangeApplicationList().get(0).getPrice(),
                stringGlass.getBidExchangeApplicationList().get(0).getPrice());
    }
}
