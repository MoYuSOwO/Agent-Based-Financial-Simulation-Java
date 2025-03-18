package io.github.MoYuSOwO.agent;

import io.github.MoYuSOwO.EventBus;
import io.github.MoYuSOwO.stock.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

public class AgentAccount implements EventBus.OrderStatusListener {
    private static int nextAccountId = 0;
    private final int accountId;
    private BigDecimal cash;
    private int position;
    private BigDecimal costPerPosition;
    private final HashMap<Long, Order> orders;
    private final EventBus.OrderSubmitListener listener;
    public AgentAccount(double cash, int position, double costPerPosition, EventBus.OrderSubmitListener listener) {
        this.accountId = nextAccountId++;
        this.cash = BigDecimal.valueOf(cash).setScale(Order.SCALE, RoundingMode.HALF_UP);
        this.position = position;
        this.costPerPosition = BigDecimal.valueOf(costPerPosition).setScale(Order.SCALE, RoundingMode.HALF_UP);
        this.orders = new HashMap<>();
        this.listener = listener;
    }
    @Override
    public void onOrderAccepted(Order order) {
        this.orders.put(order.getId(), order);
    }
    @Override
    public void onOrderFilled(long id, int filledQuantity, BigDecimal filledPrice) {
        if (this.orders.get(id) == null) {
            return;
        }
        if (this.orders.get(id).getDirection() == Order.orderDirection.BUY) {
            BigDecimal filledQuantityBD = BigDecimal.valueOf(filledQuantity);
            BigDecimal positionBD = BigDecimal.valueOf(this.position);
            this.orders.get(id).reduceQuantity(filledQuantity);
            BigDecimal originalAmount = Order.round(this.costPerPosition.multiply(positionBD));
            BigDecimal filledAmount = Order.round(filledPrice.multiply(filledQuantityBD));
            BigDecimal totalAmount = Order.round(originalAmount.add(filledAmount));
            BigDecimal totalQuantity = Order.round(positionBD.add(filledQuantityBD));
            this.costPerPosition = totalAmount.divide(totalQuantity, Order.SCALE, RoundingMode.HALF_UP);
            this.position += filledQuantity;
            this.cash = Order.round(this.cash.subtract(filledAmount));
        }
        else if (this.orders.get(id).getDirection() == Order.orderDirection.SELL) {
            BigDecimal filledQuantityBD = BigDecimal.valueOf(filledQuantity);
            this.orders.get(id).reduceQuantity(filledQuantity);
            BigDecimal filledAmount = Order.round(filledQuantityBD.multiply(filledPrice));
            this.position -= filledQuantity;
            this.cash = Order.round(this.cash.add(filledAmount));
        }
        if (this.orders.get(id).getQuantity() == 0) {
            this.orders.remove(id);
        }
    }
    @Override
    public void onOrderCanceled(long id) {
        if (this.orders.get(id) != null) {
            this.orders.remove(id);
        }
    }
    public void addOrder(int quantity, Order.orderDirection direction, double price) {
        listener.onOrderSubmitted(this.accountId, quantity, direction, price);
    }
    public void addOrder(int quantity, Order.orderDirection direction, BigDecimal price) {
        listener.onOrderSubmitted(this.accountId, quantity, direction, price);
    }
    public void addOrder(int quantity, Order.orderDirection direction) {
        listener.onOrderSubmitted(this.accountId, quantity, direction);
    }
    public int getAccountId() {
        return this.accountId;
    }
    public BigDecimal getCash() {
        return this.cash;
    }
    public int getPosition() {
        return this.position;
    }
    public BigDecimal getCostPerPosition() {
        return this.costPerPosition;
    }
}
