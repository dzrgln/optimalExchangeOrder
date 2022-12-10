package src.entity;

public record MarketPrice(double priceForBuy, double priceForSell) {
    @Override
    public String toString() {
        return "MarketPrice{" +
                "priceForBuy=" + priceForBuy +
                ", priceForSell=" + priceForSell +
                '}';
    }
}
