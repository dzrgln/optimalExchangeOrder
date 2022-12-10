package src;

import src.entity.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;

public class OptimalOrderInstaller {
    private final long DIVISOR_FOR_DURATION;
    private final double SHARE_OF_PROFIT_AND_COMMISSION;
    private final Duration CRITICAL_TIMEOUT;

    public OptimalOrderInstaller(long DIVISOR_FOR_DURATION, double SHARE_OF_PROFIT_AND_COMMISSION, Duration CRITICAL_TIMEOUT) {
        this.DIVISOR_FOR_DURATION = DIVISOR_FOR_DURATION;
        this.SHARE_OF_PROFIT_AND_COMMISSION = SHARE_OF_PROFIT_AND_COMMISSION;
        this.CRITICAL_TIMEOUT = CRITICAL_TIMEOUT;
    }

    private Map<Timestamp, StringGlass> getHistoricData(Duration duration, Timestamp startTimeStamp, Map<Timestamp, StringGlass> data) {
        Map<Timestamp, StringGlass> historicData = new HashMap<>();
        long pastNanoSeconds = (startTimeStamp.getNanos() - duration.getNano()) / 1000 / DIVISOR_FOR_DURATION;
        Timestamp pastTimeStamp = new Timestamp(pastNanoSeconds);
        for (Map.Entry<Timestamp, StringGlass> entry : data.entrySet()) {
            if (entry.getKey().before(startTimeStamp) && entry.getKey().after(pastTimeStamp)) {
                historicData.put(entry.getKey(), entry.getValue());
            }
        }
        return historicData;
    }

    private double getAverageAmount(RequestDTOCustomerOrder requestOrder, Map<Timestamp, StringGlass> data) {
        int sumAmount = 0;
        int countAmount = 0;
        List<ExchangeApplication> applications = new ArrayList<>();
        for (StringGlass stringGlass : getHistoricData(requestOrder.durationOfCustomerOrder(),
                requestOrder.startTimestamp(), data).values()) {
            if (requestOrder.isBuy()) {
                applications = stringGlass.getAskExchangeApplicationList();
            } else {
                applications = stringGlass.getBidExchangeApplicationList();
            }
            for (ExchangeApplication eApplication : applications) {
                sumAmount += eApplication.getAmount();
                countAmount++;
            }
        }
        return (double) sumAmount / countAmount;
    }

    public Map<Timestamp, ClientTransaction> makeOptimalOrder(RequestDTOCustomerOrder requestDTOCustomerOrder,
                                                              Map<Timestamp, StringGlass> historicData) {
        Map<Timestamp, ClientTransaction> optimalTransactions = new HashMap<>();
        CustomerOrder customerOrder = requestOrderToOrder(requestDTOCustomerOrder, historicData);
        Map<Timestamp, StringGlass> futureData = getFutureData(historicData, customerOrder.startTimestamp());
        int amount = customerOrder.customerAmount();
        Duration duration = customerOrder.durationOfCustomerOrder();
        //for purchase
        if (customerOrder.isBuy()) {
            //analise exchange glass
            for (Map.Entry<Timestamp, StringGlass> entry : futureData.entrySet()) {
                // check, that time is enough and price is optimal
                if (amount != 0) {
                    Timestamp finishTime = new Timestamp(customerOrder.startTimestamp().getTime() +
                            duration.getNano() / 1000);
                    if (finishTime.getTime() - entry.getKey().getTime() > CRITICAL_TIMEOUT.toMillis()) {
                        if (entry.getValue().getMarketPrice().priceForBuy() < customerOrder.optimalPrice()) {
                            // record successful transaction
                            optimalTransactions.put(entry.getKey(),
                                    makeTransaction(entry.getValue().getAskExchangeApplicationList()));
                            //remove application from glass
                            //reduce remaining amount
                            entry.getValue().getAskExchangeApplicationList().remove(0);
                            amount -= entry.getValue().getAskExchangeApplicationList().get(0).getAmount();
                        } else {
                            // continue waiting best price
                            continue;
                        }
                    } else {
                        // if client is ready to wait more, that we will increase duration
                        if (customerOrder.isWait()) {
                            duration = Duration.ofMillis(duration.toMillis() * 2);
                            // else buy at the market price
                        } else {
                            if (!entry.getValue().getAskExchangeApplicationList().isEmpty()) {
                                optimalTransactions.put(entry.getKey(),
                                        makeTransaction(entry.getValue().getAskExchangeApplicationList()));
                                entry.getValue().getAskExchangeApplicationList().remove(0);
                            }
                        }
                    }
                } else {
                    break;
                }
                //remove current string from future data
                futureData.remove(entry.getKey());
            }


        }
        return optimalTransactions;
    }

    private ClientTransaction makeTransaction(List<ExchangeApplication> applications){
                return new ClientTransaction(
                        applications.get(0).getAmount(),
                        applications.get(0).getPrice());
    }

    private Map<Timestamp, StringGlass> getFutureData(Map<Timestamp, StringGlass> data, Timestamp currentTime) {
        Map<Timestamp, StringGlass> futureData = new TreeMap<>();
        for (Map.Entry<Timestamp, StringGlass> entry : data.entrySet()) {
            if (entry.getKey().after(currentTime)) {
                futureData.put(entry.getKey(), entry.getValue());
            }
        }
        return futureData;
    }

    public CustomerOrder requestOrderToOrder(RequestDTOCustomerOrder requestOrder, Map<Timestamp, StringGlass> data) {
        return new CustomerOrder(requestOrder.customerPrice(),
                calculateOptimalPrice(requestOrder.isBuy(), requestOrder.customerPrice()),
                requestOrder.customerAmount(),
                getChildAmounts(getAverageAmount(requestOrder, data), requestOrder.customerAmount()),
                requestOrder.isBuy(),
                requestOrder.startTimestamp(),
                requestOrder.durationOfCustomerOrder(),
                requestOrder.isWait()
        );
    }

    private double calculateOptimalPrice(boolean isBuy, double customerPrice) {
        double optimalPrice;
        if (isBuy) {
            optimalPrice = customerPrice * (1 - SHARE_OF_PROFIT_AND_COMMISSION);
        } else {
            optimalPrice = customerPrice * (1 + SHARE_OF_PROFIT_AND_COMMISSION);
        }
        return optimalPrice;
    }

    private List<Integer> getChildAmounts(double averageAmount, int customerAmount) {
        List<Integer> listOfChildAmounts = new ArrayList<>();
        int numberOfAmounts = (int) Math.floor(customerAmount / averageAmount);
        for (int i = 0; i <= numberOfAmounts; i++) {
            if (i != numberOfAmounts) {
                listOfChildAmounts.add((int) Math.floor(averageAmount));
            } else {
                listOfChildAmounts.add(customerAmount - (int) Math.floor(averageAmount) * numberOfAmounts);
            }
        }
        return listOfChildAmounts;
    }
}
