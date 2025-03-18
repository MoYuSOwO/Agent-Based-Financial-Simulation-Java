package io.github.MoYuSOwO.agent;

import io.github.MoYuSOwO.EventBus;
import io.github.MoYuSOwO.stock.Order;

import java.math.BigDecimal;

public class NoiseAgent extends Agent {
    private static final int AVERAGE_TRADE = 15;
    private static final long AVERAGE_WAIT = 5000; // ms
    private static final BigDecimal MIN_START_CASH = Order.round(BigDecimal.valueOf(6000.0));
    private static final BigDecimal MAX_START_CASH = Order.round(BigDecimal.valueOf(12000.0));
    private static final int MIN_START_POSITION = 100;
    private static final int MAX_START_POSITION = 300;
    private static final BigDecimal MINIMUM_CASH = Order.round(BigDecimal.valueOf(600.0));
    public NoiseAgent(double cash, int position, double costPerPosition, EventBus.OrderSubmitListener listener, EventBus.CurrentPriceListener dataListener) {
        super(cash, position, costPerPosition, listener, dataListener);
    }
    public NoiseAgent(double costPerPosition, EventBus.OrderSubmitListener listener, EventBus.CurrentPriceListener dataListener) {
        super(randomGenerator.nextDouble(MIN_START_CASH.doubleValue(), MAX_START_CASH.doubleValue()), randomGenerator.nextInt(MIN_START_POSITION, MAX_START_POSITION+1), costPerPosition, listener, dataListener);
    }
    @Override
    protected long getIntervalTime() {
        double u = randomGenerator.nextDouble();
        return (long) (-AVERAGE_WAIT * Math.log(u));
    }
    @Override
    protected void makeDecision() {
        int tradeQuantity = (int) (-AVERAGE_TRADE * Math.log(randomGenerator.nextDouble()));
        double random = randomGenerator.nextDouble();
        if (account.getCash().compareTo(MINIMUM_CASH) < 0 || random < 0.4) {
            random = randomGenerator.nextDouble();
            if (random < 0.8) account.addOrder(tradeQuantity, Order.orderDirection.SELL, dataListener.getCurrentPrice().add(dataListener.getCurrentPrice()));
            else account.addOrder(tradeQuantity, Order.orderDirection.SELL);
        }
        else if (random < 0.8) {
            random = randomGenerator.nextDouble();
            if (random < 0.8) account.addOrder(tradeQuantity, Order.orderDirection.BUY, dataListener.getCurrentPrice().add(dataListener.getCurrentPrice()));
            else account.addOrder(tradeQuantity, Order.orderDirection.BUY);
        }
    }
}
