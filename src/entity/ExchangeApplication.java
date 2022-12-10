package src.entity;

import java.sql.Timestamp;
import java.util.Objects;

public class ExchangeApplication {
    private final int amount;
    private final double price;


    public int getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public ExchangeApplication(int amount, double price) {
        this.amount = amount;
        this.price = price;
    }

    @Override
    public String toString() {
        return "\n EA: amount: " + amount + "; price: " + price;
    }
}
