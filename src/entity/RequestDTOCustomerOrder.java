package src.entity;

import java.sql.Timestamp;
import java.time.Duration;

public record RequestDTOCustomerOrder(double customerPrice,
                                      int customerAmount,
                                      boolean isBuy,
                                      Timestamp startTimestamp,
                                      Duration durationOfCustomerOrder,
                                      boolean isWait) {
}
