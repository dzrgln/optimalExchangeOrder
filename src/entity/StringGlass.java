package src.entity;

import java.util.List;

public class StringGlass {
    private MarketPrice marketPrice;
    private List<ExchangeApplication> askExchangeApplicationList;
    private List<ExchangeApplication> bidExchangeApplicationList;

    public StringGlass(MarketPrice marketPrice, List<ExchangeApplication> askExchangeApplicationList,
                       List<ExchangeApplication> bidExchangeApplicationList) {
        this.marketPrice = marketPrice;
        this.askExchangeApplicationList = askExchangeApplicationList;
        this.bidExchangeApplicationList = bidExchangeApplicationList;
    }

    public List<ExchangeApplication> getAskExchangeApplicationList() {
        return askExchangeApplicationList;
    }

    public void setAskExchangeApplicationList(List<ExchangeApplication> askExchangeApplicationList) {
        this.askExchangeApplicationList = askExchangeApplicationList;
    }

    public List<ExchangeApplication> getBidExchangeApplicationList() {
        return bidExchangeApplicationList;
    }

    public void setBidExchangeApplicationList(List<ExchangeApplication> bidExchangeApplicationList) {
        this.bidExchangeApplicationList = bidExchangeApplicationList;
    }

    public MarketPrice getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(MarketPrice marketPrice) {
        this.marketPrice = marketPrice;
    }

    @Override
    public String toString() {
/*        return "SG: marketPrice: " + marketPrice + "; Ask_list: " + askExchangeApplicationList
                + "; Bid_list: " + bidExchangeApplicationList;    */
        return "\n +marketPrice: " + marketPrice;
    }
}
