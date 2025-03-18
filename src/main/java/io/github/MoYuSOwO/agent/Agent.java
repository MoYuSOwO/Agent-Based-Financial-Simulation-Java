package io.github.MoYuSOwO.agent;

import io.github.MoYuSOwO.EventBus;
import io.github.MoYuSOwO.stock.Order;
import io.github.MoYuSOwO.stock.OrderData;

import java.math.BigDecimal;
import java.util.concurrent.*;

public abstract class Agent {
    protected static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);
    protected static ThreadLocalRandom randomGenerator = ThreadLocalRandom.current();
    protected AgentAccount account;
    protected ScheduledFuture<?> future;
    protected EventBus.CurrentPriceListener dataListener;
    public static void shutdownScheduler() {
        scheduler.shutdown();
    }
    public Agent(double cash, int position, double costPerPosition, EventBus.OrderSubmitListener listener, EventBus.CurrentPriceListener dataListener) {
        this.account = new AgentAccount(cash, position, costPerPosition, listener);
        this.future = null;
        this.dataListener = dataListener;
    }
    public BigDecimal getCash() {
        return this.account.getCash();
    }
    public int getPosition() {
        return this.account.getPosition();
    }
    public BigDecimal getCostPerPosition() {
        return this.account.getCostPerPosition();
    }
    public void execute() {
        if (future == null || future.isDone() || future.isCancelled()) {
            future = scheduler.schedule(
                this::makeDecision,
                this.getIntervalTime(),
                TimeUnit.MILLISECONDS
            );
        }
    }
    protected abstract long getIntervalTime();
    protected abstract void makeDecision();
}
