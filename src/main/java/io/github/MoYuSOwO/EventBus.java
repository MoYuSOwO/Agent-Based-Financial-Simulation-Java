package io.github.MoYuSOwO;

import io.github.MoYuSOwO.stock.Order;

import java.math.BigDecimal;

public class EventBus {
    public interface OrderStatusListener {
        void onOrderAccepted(Order id);
        void onOrderFilled(long id, int filledQuantity, BigDecimal filledPrice);
        void onOrderCanceled(long id);
    }
    public interface OrderSubmitListener {
        void onOrderSubmitted(int accountId, int quantity, Order.orderDirection direction, double price);
        void onOrderSubmitted(int accountId, int quantity, Order.orderDirection direction, BigDecimal price);
        void onOrderSubmitted(int accountId, int quantity, Order.orderDirection direction);
    }
    public interface CurrentPriceListener {
        BigDecimal getCurrentPrice();
    }
}
