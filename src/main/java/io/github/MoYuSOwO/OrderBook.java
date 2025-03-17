package io.github.MoYuSOwO;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

public class OrderBook {
    public interface OrderListener {
        void onDeal(Order.orderStatus status);
    }
    private final TreeSet<Order> buyQueue;
    private final TreeSet<Order> sellQueue;
    private final HashMap<Integer, OrderListener> listener;
    public OrderBook() {
        Comparator<Order> buyComparator = (o1, o2) -> {
            int cmpPrice = Double.compare(o2.getPrice(), o1.getPrice());
            if (cmpPrice != 0) {
                return cmpPrice;
            }
            int cmpQuantity = Integer.compare(o2.getQuantity(), o1.getQuantity());
            if (cmpQuantity != 0) {
                return cmpQuantity;
            }
            return Integer.compare(o1.getId(), o2.getId());
        };
        this.buyQueue = new TreeSet<>(buyComparator);
        Comparator<Order> sellComparator = (o1, o2) -> {
            if (o1.getId() == o2.getId()) {
                return 0;
            }
            int cmpPrice = Double.compare(o1.getPrice(), o2.getPrice());
            if (cmpPrice != 0) {
                return cmpPrice;
            }
            return Integer.compare(o2.getQuantity(), o1.getQuantity());
        };
        this.sellQueue = new TreeSet<>(sellComparator);
        this.listener = new HashMap<>();
    }
}
