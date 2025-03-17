package io.github.MoYuSOwO;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Order {
    public enum orderStatus {
        PENDING,
        PARTLY_FILL,
        FILL,
        CANCELED
    }
    public enum orderDirection {
        BUY,
        SELL
    }
    public enum orderType {
        MARKET,
        LIMIT
    }
    private final int id;
    private int quantity;
    private final orderDirection direction;
    private final orderType type;
    private BigDecimal price;
    public Order(int id, int quantity, orderDirection direction, orderType type, double price) {
        this.id = id;
        this.quantity = quantity;
        this.direction = direction;
        this.type = type;
        this.price = new BigDecimal(price).setScale(3, RoundingMode.HALF_UP);
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public void setPrice(double price) {
        this.price = BigDecimal.valueOf(price);
    }
    public int getId() {
        return this.id;
    }
    public int getQuantity() {
        return this.quantity;
    }
    public orderDirection getDirection() {
        return this.direction;
    }
    public orderType getType() {
        return this.type;
    }
    public double getPrice() {
        return this.price.doubleValue();
    }
}
