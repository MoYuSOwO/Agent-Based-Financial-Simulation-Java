package io.github.MoYuSOwO.stock;

import io.github.MoYuSOwO.EventBus;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentSkipListMap;

public class OrderData implements EventBus.CurrentPriceListener {
    private final ConcurrentSkipListMap<Long, BigDecimal> priceHistory;

    public OrderData(double startPrice) {
        this.priceHistory = new ConcurrentSkipListMap<>();
        this.priceHistory.put(System.nanoTime(), Order.round(BigDecimal.valueOf(startPrice)));
    }

    public void updatePrice(BigDecimal price) {
        this.priceHistory.put(System.nanoTime(), price);
    }

    @Override
    public BigDecimal getCurrentPrice() {
        return this.priceHistory.lastEntry().getValue();
    }
}
