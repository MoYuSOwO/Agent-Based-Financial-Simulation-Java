package io.github.MoYuSOwO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

public class AgentAccount implements OrderBook.OrderListener {
    private BigDecimal cash;
    private int position;
    private BigDecimal costPerPosition;
    private final HashMap<Long, Order> orders;
    public AgentAccount(double cash, int position, double costPerPosition) {
        this.cash = BigDecimal.valueOf(cash).setScale(Order.SCALE, RoundingMode.HALF_UP);
        this.position = position;
        this.costPerPosition = BigDecimal.valueOf(costPerPosition).setScale(Order.SCALE, RoundingMode.HALF_UP);
        this.orders = new HashMap<>();
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
    public void addOrder(Order order) {
        this.orders.put(order.getId(), order);
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
