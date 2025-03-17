package io.github.MoYuSOwO;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Order {
    public enum orderDirection {
        BUY,
        SELL
    }
    public enum orderType {
        MARKET,
        LIMIT
    }
    public static final int SCALE = 3;
    private final long id;
    private int quantity;
    private final orderDirection direction;
    private final orderType type;
    private final BigDecimal price;
    public Order(long id, int quantity, orderDirection direction, orderType type, double price) {
        this.id = id;
        this.quantity = quantity;
        this.direction = direction;
        this.type = type;
        this.price = BigDecimal.valueOf(price).setScale(SCALE, RoundingMode.HALF_UP);
    }
    public void reduceQuantity(int quantity) {
        this.quantity -= quantity;
    }
    public long getId() {
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
    public BigDecimal getPrice() {
        return this.price;
    }
    public static BigDecimal round(BigDecimal val) {
        return val.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
