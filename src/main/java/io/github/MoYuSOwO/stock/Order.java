package io.github.MoYuSOwO.stock;

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
    private final int accountId;
    private int quantity;
    private final orderDirection direction;
    private final orderType type;
    private final BigDecimal price;
    public Order(long id, int accountId, int quantity, orderDirection direction, double price) {
        this.id = id;
        this.accountId = accountId;
        this.quantity = quantity;
        this.direction = direction;
        this.type = orderType.LIMIT;
        this.price = BigDecimal.valueOf(price).setScale(SCALE, RoundingMode.HALF_UP);
    }
    public Order(long id, int accountId, int quantity, orderDirection direction, BigDecimal price) {
        this.id = id;
        this.accountId = accountId;
        this.quantity = quantity;
        this.direction = direction;
        this.type = orderType.LIMIT;
        this.price = price;
    }
    public Order(long id, int accountId, int quantity, orderDirection direction) {
        this.id = id;
        this.accountId = accountId;
        this.quantity = quantity;
        this.direction = direction;
        this.type = orderType.MARKET;
        this.price = null;
    }
    public Order copy() {
        if (this.type == orderType.MARKET) return new Order(this.id, this.accountId, this.quantity, this.direction);
        return new Order(this.id, this.accountId, this.quantity, this.direction, this.price);
    }
    public void reduceQuantity(int quantity) {
        this.quantity -= quantity;
    }
    public int getAccountId() {
        return this.accountId;
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
    public static orderDirection toOrderDirection(String s) {
        if (s.equals("BUY")) return orderDirection.BUY;
        else return orderDirection.SELL;
    }
    public static orderType toOrderType(String s) {
        if (s.equals("MARKET")) return orderType.MARKET;
        else return orderType.LIMIT;
    }
}
