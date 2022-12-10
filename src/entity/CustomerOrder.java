package src.entity;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;

public record CustomerOrder(double customerPrice,
                            double optimalPrice,
                            int customerAmount,
                            List<Integer> childAmounts,
                            boolean isBuy,
                            Timestamp startTimestamp,
                            Duration durationOfCustomerOrder,
                            boolean isWait) {

    @Override
    public String toString() {
        return "CustomerOrder{" +
                "customerPrice=" + customerPrice +
                ", optimalPrice=" + optimalPrice +
                ", customerAmount=" + customerAmount +
                ", childAmounts=" + childAmounts +
                ", isBuy=" + isBuy +
                ", startTimestamp=" + startTimestamp +
                ", durationOfCustomerOrder=" + durationOfCustomerOrder +
                ", isWait=" + isWait +
                '}';
    }
}
