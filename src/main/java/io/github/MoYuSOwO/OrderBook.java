package io.github.MoYuSOwO;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class OrderBook {
    public interface OrderListener {
        void onOrderFilled(long id, int filledQuantity, BigDecimal filledPrice);
        void onOrderCanceled(long id);
    }
    private Thread matchingThread;
    private volatile boolean running;
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final TreeSet<Order> buyOrders;
    private final TreeSet<Order> sellOrders;
    private final LinkedBlockingQueue<Order> orderQueue;
    private final ConcurrentHashMap<Long, Boolean> isOrderCanceled;
    private final ArrayList<OrderListener> listeners;
    private volatile BigDecimal currentPrice;
    public OrderBook(double currentPrice) {
        Comparator<Order> buyComparator = (o1, o2) -> {
            int cmpPrice = o2.getPrice().compareTo(o1.getPrice());
            if (cmpPrice != 0) {
                return cmpPrice;
            }
            return Long.compare(o1.getId(), o2.getId());
        };
        this.buyOrders = new TreeSet<>(buyComparator);
        Comparator<Order> sellComparator = (o1, o2) -> {
            int cmpPrice = o1.getPrice().compareTo(o2.getPrice());
            if (cmpPrice != 0) {
                return cmpPrice;
            }
            return Long.compare(o1.getId(), o2.getId());
        };
        this.sellOrders = new TreeSet<>(sellComparator);
        this.orderQueue = new LinkedBlockingQueue<>();
        this.isOrderCanceled = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.running = false;
        this.currentPrice = BigDecimal.valueOf(currentPrice);
        this.start();
    }
    public void start() {
        if (this.running) return;
        this.running = true;
        this.matchingThread = new Thread(() -> {
            while (this.running) {
                try {
                    this.handleNextOrder();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        });
        this.matchingThread.start();
    }
    public void stop() {
        if (!this.running) return;
        this.running = false;
        if (this.matchingThread != null) {
            this.matchingThread.interrupt();
        }
    }
    public void addListener(OrderListener listener) {
        this.listeners.add(listener);
    }
    public Order addOrder(int quantity, Order.orderDirection direction, Order.orderType type, double price) {
        Order order = new Order(this.idGenerator.getAndIncrement(), quantity, direction, type, price);
        this.orderQueue.offer(order);
        return order;
    }
    public void cancelOrder(long id) {
        this.isOrderCanceled.put(id, true);
    }
    public BigDecimal getCurrentPrice() {
        return this.currentPrice;
    }
    public void _printOrderBook_() {
        System.out.println("Buy:");
        for (Order order : this.buyOrders) {
            System.out.println(Long.toString(order.getId()) + " " + Long.toString(order.getQuantity()) + " " + order.getPrice().toString());
        }
        System.out.println("Sell:");
        for (Order order : this.sellOrders) {
            System.out.println(Long.toString(order.getId()) + " " + Long.toString(order.getQuantity()) + " " + order.getPrice().toString());
        }
    }
    private void handleNextOrder() throws Exception {
        Order order = this.orderQueue.take();
        if (this.isOrderCanceled.containsKey(order.getId())) {
            this.isOrderCanceled.remove(order.getId());
            return;
        }
        if (order.getType() == Order.orderType.LIMIT) this.handleLimitOrder(order);
        else if (order.getType() == Order.orderType.MARKET) this.handleMarketOrder(order);
    }
    private void handleMarketOrder(Order order) {
        if (order.getDirection() == Order.orderDirection.BUY) {
            while (!this.sellOrders.isEmpty()) {
                Order first = this.sellOrders.pollFirst();
                if (this.isOrderCanceled.containsKey(first.getId())) {
                    this.isOrderCanceled.remove(first.getId());
                    return;
                }
                if (first.getQuantity() >= order.getQuantity()) {
                    int filledQuantity = order.getQuantity();
                    BigDecimal filledPrice = Order.round(first.getPrice());
                    first.reduceQuantity(filledQuantity);
                    this.sendOrderFilled(first.getId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), filledQuantity, filledPrice);
                    this.currentPrice = filledPrice;
                    if (first.getQuantity() > 0) {
                        this.sellOrders.add(first);
                    }
                    return;
                }
                else {
                    int filledQuantity = first.getQuantity();
                    BigDecimal filledPrice = Order.round(first.getPrice());
                    order.reduceQuantity(filledQuantity);
                    this.sendOrderFilled(first.getId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), filledQuantity, filledPrice);
                    this.currentPrice = filledPrice;
                }
            }
            if (this.sellOrders.isEmpty()) {
                this.sellOrders.add(order);
            }
        }
        else if (order.getDirection() == Order.orderDirection.SELL) {
            while (!this.buyOrders.isEmpty()) {
                Order first = this.buyOrders.pollFirst();
                if (this.isOrderCanceled.containsKey(first.getId())) {
                    this.isOrderCanceled.remove(first.getId());
                    return;
                }
                if (first.getQuantity() >= order.getQuantity()) {
                    int filledQuantity = order.getQuantity();
                    BigDecimal filledPrice = Order.round(first.getPrice());
                    first.reduceQuantity(filledQuantity);
                    this.sendOrderFilled(first.getId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), filledQuantity, filledPrice);
                    this.currentPrice = filledPrice;
                    if (first.getQuantity() > 0) {
                        this.buyOrders.add(first);
                    }
                    return;
                }
                else {
                    int filledQuantity = first.getQuantity();
                    BigDecimal filledPrice = Order.round(first.getPrice());
                    order.reduceQuantity(filledQuantity);
                    this.sendOrderFilled(first.getId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), filledQuantity, filledPrice);
                    this.currentPrice = filledPrice;
                }
            }
            if (this.buyOrders.isEmpty()) {
                this.sellOrders.add(order);
            }
        }
    }
    private void handleLimitOrder(Order order) {
        if (order.getDirection() == Order.orderDirection.BUY) {
            while (!this.sellOrders.isEmpty()) {
                Order first = this.sellOrders.pollFirst();
                if (this.isOrderCanceled.containsKey(first.getId())) {
                    this.isOrderCanceled.remove(first.getId());
                    return;
                }
                if (first.getPrice().compareTo(order.getPrice()) > 0) {
                    this.buyOrders.add(order);
                    this.sellOrders.add(first);
                    return;
                }
                else if (first.getQuantity() >= order.getQuantity()) {
                    int filledQuantity = order.getQuantity();
                    BigDecimal filledPrice = Order.round(first.getPrice());
                    first.reduceQuantity(filledQuantity);
                    this.sendOrderFilled(first.getId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), filledQuantity, filledPrice);
                    this.currentPrice = filledPrice;
                    if (first.getQuantity() > 0) {
                        this.sellOrders.add(first);
                    }
                    return;
                }
                else {
                    int filledQuantity = first.getQuantity();
                    BigDecimal filledPrice = Order.round(first.getPrice());
                    order.reduceQuantity(filledQuantity);
                    this.sendOrderFilled(first.getId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), filledQuantity, filledPrice);
                }
            }
            if (this.sellOrders.isEmpty()) {
                this.buyOrders.add(order);
            }
        }
        else if (order.getDirection() == Order.orderDirection.SELL) {
            while (!this.buyOrders.isEmpty()) {
                Order first = this.buyOrders.pollFirst();
                if (this.isOrderCanceled.containsKey(first.getId())) {
                    this.isOrderCanceled.remove(first.getId());
                    return;
                }
                if (first.getPrice().compareTo(order.getPrice()) < 0) {
                    this.sellOrders.add(order);
                    this.buyOrders.add(first);
                    return;
                }
                else if (first.getQuantity() >= order.getQuantity()) {
                    int filledQuantity = order.getQuantity();
                    BigDecimal filledPrice = Order.round(first.getPrice());
                    first.reduceQuantity(filledQuantity);
                    this.sendOrderFilled(first.getId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), filledQuantity, filledPrice);
                    this.currentPrice = filledPrice;
                    if (first.getQuantity() > 0) {
                        this.buyOrders.add(first);
                    }
                    return;
                }
                else {
                    int filledQuantity = first.getQuantity();
                    BigDecimal filledPrice = Order.round(first.getPrice());
                    order.reduceQuantity(filledQuantity);
                    this.sendOrderFilled(first.getId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), filledQuantity, filledPrice);
                    this.currentPrice = filledPrice;
                }
            }
            if (this.buyOrders.isEmpty()) {
                this.sellOrders.add(order);
            }
        }
    }
    private void sendOrderFilled(long id, int filledQuantity, BigDecimal filledPrice) {
        for (OrderListener listener : listeners) {
            listener.onOrderFilled(id, filledQuantity, filledPrice);
        }
    }
    private void sendOrderCanceled(long id) {
        for (OrderListener listener : listeners) {
            listener.onOrderCanceled(id);
        }
    }
}
