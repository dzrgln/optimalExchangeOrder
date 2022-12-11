package src;

import src.entity.*;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;


public class OrderInstaller {
    private final long DIVISOR_FOR_DURATION;
    private final double SHARE_OF_PROFIT_AND_COMMISSION;
    private final Duration CRITICAL_TIMEOUT;

    public OrderInstaller(long DIVISOR_FOR_DURATION, double SHARE_OF_PROFIT_AND_COMMISSION, Duration CRITICAL_TIMEOUT) {
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

    public List<ClientTransaction> makeOptimalOrder(RequestDTOCustomerOrder requestDTOCustomerOrder,
                                                              Map<Timestamp, StringGlass> historicData) {
        List<ClientTransaction> optimalTransactions = new ArrayList<>();
        CustomerOrder customerOrder = requestOrderToOrder(requestDTOCustomerOrder, historicData);
        Map<Timestamp, StringGlass> futureData = getFutureData(historicData, customerOrder.startTimestamp());
        int amount = customerOrder.customerAmount();
        Duration duration = customerOrder.durationOfCustomerOrder();
        //for purchase
        if (customerOrder.isBuy()) {
            //analise exchange glass
            for (Map.Entry<Timestamp, StringGlass> entry : futureData.entrySet()) {
                // check, that time is enough and price is optimal
                if (amount > 0) {
                    Timestamp finishTime = new Timestamp(customerOrder.startTimestamp().getTime() +
                            duration.toMillis());
                    if (finishTime.getTime() - entry.getKey().getTime() > CRITICAL_TIMEOUT.toMillis()) {
                        if (entry.getValue().getMarketPrice().priceForBuy() < customerOrder.optimalPrice()) {
                            // record successful transaction
                            optimalTransactions.add(
                                    makeTransaction(amount, entry.getValue().getAskExchangeApplicationList()));
                            //remove application from glass
                            entry.getValue().getAskExchangeApplicationList().remove(0);
                            //reduce remaining amount
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
                                optimalTransactions.add(
                                        makeTransaction(amount, entry.getValue().getAskExchangeApplicationList()));
                                entry.getValue().getAskExchangeApplicationList().remove(0);
                            }
                        }
                    }
                } else {
                    break;
                }
            }
        }
        return optimalTransactions;
    }

    public List<ClientTransaction> makeMarketOrder(RequestDTOCustomerOrder requestDTOCustomerOrder,
                                                             Map<Timestamp, StringGlass> historicData) {
        Map<Timestamp, ClientTransaction> optimalTransactions = new TreeMap<>();
        List<ClientTransaction> transactions = new ArrayList<>();
        CustomerOrder customerOrder = requestOrderToOrder(requestDTOCustomerOrder, historicData);
        Map<Timestamp, StringGlass> futureData = getFutureData(historicData, customerOrder.startTimestamp());
        int amount = customerOrder.customerAmount();
        Duration duration = customerOrder.durationOfCustomerOrder();
        //for purchase
        if (customerOrder.isBuy()) {
            //analise exchange glass
            for (Map.Entry<Timestamp, StringGlass> entry : futureData.entrySet()) {
                if (amount > 0) {
                    if (!entry.getValue().getAskExchangeApplicationList().isEmpty()) {
                        transactions.add(makeTransaction(amount, entry.getValue().getAskExchangeApplicationList()));
                        entry.getValue().getAskExchangeApplicationList().remove(0);
                        amount -= transactions.get(transactions.size()-1).amount();
                    }
                } else {
                    break;
                }
            }
        }
        return transactions;
    }


    public double getAveragePrice(List<ClientTransaction> list){
        double sum = 0;
        for(ClientTransaction clTr: list){
            sum +=clTr.price();
        }
        return sum/list.size();
    }
    private ClientTransaction makeTransaction(int amount, List<ExchangeApplication> applications) {
        ClientTransaction clientTransaction = null;
        if (applications.get(0).getAmount() <= amount) {
            clientTransaction = new ClientTransaction(applications.get(0).getAmount(), applications.get(0).getPrice());
        } else {
            clientTransaction = new ClientTransaction(applications.get(0).getAmount() - amount,
                    applications.get(0).getPrice());
        }
        return clientTransaction;
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
