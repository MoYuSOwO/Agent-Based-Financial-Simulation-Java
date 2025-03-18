package io.github.MoYuSOwO.stock;

import io.github.MoYuSOwO.EventBus;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class OrderBook implements EventBus.OrderSubmitListener {
    private Thread matchingThread;
    private volatile boolean running;
    private final AtomicLong idGenerator = new AtomicLong(0);
    private final TreeSet<Order> buyOrders;
    private final TreeSet<Order> sellOrders;
    private final LinkedBlockingQueue<Order> orderQueue;
    private final ConcurrentHashMap<Long, Boolean> isOrderCanceled;
    private final HashMap<Integer, EventBus.OrderStatusListener> listeners;
    private final OrderData data;
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
        this.listeners = new HashMap<>();
        this.running = false;
        this.data = new OrderData(currentPrice);
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
    public void addListener(int accountId, EventBus.OrderStatusListener listener) {
        this.listeners.put(accountId, listener);
    }
    public void cancelOrder(long id) {
        this.isOrderCanceled.put(id, true);
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
        this.sendOrderAccepted(order);
        if (order.getType() == Order.orderType.LIMIT) this.handleLimitOrder(order);
        else if (order.getType() == Order.orderType.MARKET) this.handleMarketOrder(order);
    }
    private void handleMarketOrder(Order order) {
        if (order.getDirection() == Order.orderDirection.BUY) {
            while (!this.sellOrders.isEmpty()) {
                Order first = this.sellOrders.pollFirst();
                if (this.isOrderCanceled.containsKey(first.getId())) {
                    this.isOrderCanceled.remove(first.getId());
                    continue;
                }
                if (first.getAccountId() == order.getAccountId()) {
                    this.sendOrderCanceled(first.getId(), first.getAccountId());
                    this.sellOrders.add(first);
                    return;
                }
                if (first.getQuantity() >= order.getQuantity()) {
                    int filledQuantity = order.getQuantity();
                    BigDecimal filledPrice = Order.round(first.getPrice());
                    first.reduceQuantity(filledQuantity);
                    this.sendOrderFilled(first.getId(), first.getAccountId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), order.getAccountId(), filledQuantity, filledPrice);
                    this.updatePrice(filledPrice);
                    if (first.getQuantity() > 0) {
                        this.sellOrders.add(first);
                    }
                    return;
                }
                else {
                    int filledQuantity = first.getQuantity();
                    BigDecimal filledPrice = Order.round(first.getPrice());
                    order.reduceQuantity(filledQuantity);
                    this.sendOrderFilled(first.getId(), first.getAccountId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), order.getAccountId(), filledQuantity, filledPrice);
                    this.updatePrice(filledPrice);
                }
            }
            this.sendOrderCanceled(order.getId(), order.getAccountId());
        }
        else if (order.getDirection() == Order.orderDirection.SELL) {
            while (!this.buyOrders.isEmpty()) {
                Order first = this.buyOrders.pollFirst();
                if (this.isOrderCanceled.containsKey(first.getId())) {
                    this.isOrderCanceled.remove(first.getId());
                    continue;
                }
                if (first.getAccountId() == order.getAccountId()) {
                    this.sendOrderCanceled(first.getId(), first.getAccountId());
                    this.buyOrders.add(first);
                    return;
                }
                if (first.getQuantity() >= order.getQuantity()) {
                    int filledQuantity = order.getQuantity();
                    BigDecimal filledPrice = Order.round(first.getPrice());
                    first.reduceQuantity(filledQuantity);
                    this.sendOrderFilled(first.getId(), first.getAccountId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), order.getAccountId(), filledQuantity, filledPrice);
                    this.updatePrice(filledPrice);
                    if (first.getQuantity() > 0) {
                        this.buyOrders.add(first);
                    }
                    return;
                }
                else {
                    int filledQuantity = first.getQuantity();
                    BigDecimal filledPrice = Order.round(first.getPrice());
                    order.reduceQuantity(filledQuantity);
                    this.sendOrderFilled(first.getId(), first.getAccountId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), order.getAccountId(), filledQuantity, filledPrice);
                    this.updatePrice(filledPrice);
                }
            }
            this.sendOrderCanceled(order.getId(), order.getAccountId());
        }
    }
    private void handleLimitOrder(Order order) {
        if (order.getDirection() == Order.orderDirection.BUY) {
            while (!this.sellOrders.isEmpty()) {
                Order first = this.sellOrders.pollFirst();
                if (this.isOrderCanceled.containsKey(first.getId())) {
                    this.isOrderCanceled.remove(first.getId());
                    continue;
                }
                if (first.getAccountId() == order.getAccountId()) {
                    this.sendOrderCanceled(first.getId(), first.getAccountId());
                    this.sellOrders.add(first);
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
                    this.sendOrderFilled(first.getId(), first.getAccountId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), order.getAccountId(), filledQuantity, filledPrice);
                    this.updatePrice(filledPrice);
                    if (first.getQuantity() > 0) {
                        this.sellOrders.add(first);
                    }
                    return;
                }
                else {
                    int filledQuantity = first.getQuantity();
                    BigDecimal filledPrice = Order.round(first.getPrice());
                    order.reduceQuantity(filledQuantity);
                    this.sendOrderFilled(first.getId(), first.getAccountId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), order.getAccountId(), filledQuantity, filledPrice);
                }
            }
            this.buyOrders.add(order);
        }
        else if (order.getDirection() == Order.orderDirection.SELL) {
            while (!this.buyOrders.isEmpty()) {
                Order first = this.buyOrders.pollFirst();
                if (this.isOrderCanceled.containsKey(first.getId())) {
                    this.isOrderCanceled.remove(first.getId());
                    return;
                }
                if (first.getAccountId() == order.getAccountId()) {
                    this.sendOrderCanceled(first.getId(), first.getAccountId());
                    this.buyOrders.add(first);
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
                    this.sendOrderFilled(first.getId(), first.getAccountId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), order.getAccountId(), filledQuantity, filledPrice);
                    this.updatePrice(filledPrice);
                    if (first.getQuantity() > 0) {
                        this.buyOrders.add(first);
                    }
                    return;
                }
                else {
                    int filledQuantity = first.getQuantity();
                    BigDecimal filledPrice = Order.round(first.getPrice());
                    order.reduceQuantity(filledQuantity);
                    this.sendOrderFilled(first.getId(), first.getAccountId(), filledQuantity, filledPrice);
                    this.sendOrderFilled(order.getId(), order.getAccountId(), filledQuantity, filledPrice);
                    this.updatePrice(filledPrice);
                }
            }
            this.sellOrders.add(order);
        }
    }
    private void updatePrice(BigDecimal price) {
        data.updatePrice(price);
    }
    private void sendOrderAccepted(Order order) {
        this.listeners.get(order.getAccountId()).onOrderAccepted(order.copy());
    }
    private void sendOrderFilled(long id, int accountId, int filledQuantity, BigDecimal filledPrice) {
        this.listeners.get(accountId).onOrderFilled(id, filledQuantity, filledPrice);
    }
    private void sendOrderCanceled(long id, int accountId) {
        this.listeners.get(accountId).onOrderCanceled(id);
    }
    @Override
    public void onOrderSubmitted(int accountId, int quantity, Order.orderDirection direction, double price) {
        Order order = new Order(this.idGenerator.getAndIncrement(), accountId, quantity, direction, price);
        this.orderQueue.offer(order);
    }

    @Override
    public void onOrderSubmitted(int accountId, int quantity, Order.orderDirection direction, BigDecimal price) {
        Order order = new Order(this.idGenerator.getAndIncrement(), accountId, quantity, direction, price);
        this.orderQueue.offer(order);
    }

    @Override
    public void onOrderSubmitted(int accountId, int quantity, Order.orderDirection direction) {
        Order order = new Order(this.idGenerator.getAndIncrement(), accountId, quantity, direction);
        this.orderQueue.offer(order);
    }
}
