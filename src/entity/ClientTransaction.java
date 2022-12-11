package src.entity;

public record ClientTransaction(int amount, double price) {
    @Override
    public String toString() {
        return "ClientTransaction{" +
                "amount=" + amount +
                ", price=" + price +
                "}\n";
    }
}
