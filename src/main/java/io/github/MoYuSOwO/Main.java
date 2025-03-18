package io.github.MoYuSOwO;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        OrderBook orderbook = new OrderBook(100);
        AgentAccount Mary = new AgentAccount(10000, 200, 100.0);
        AgentAccount Alice = new AgentAccount(10000, 250, 100.0);
        while (true) {
            Scanner input = new Scanner(System.in);
            String name = input.next();
            if (name.equals("exit")) {
                input.close();
                orderbook.stop();
                return;
            }
            else if (name.equals("print")) {
                orderbook._printOrderBook_();
                continue;
            }
            int qty = input.nextInt();
            double price = 0.0;
            Order.orderDirection direction = Order.toOrderDirection(input.next());
            Order.orderType type = Order.toOrderType(input.next());
            if (type == Order.orderType.LIMIT) price = input.nextDouble();
            if (name.equals("Mary")) {
                Mary.addOrder(orderbook.addOrder(qty, direction, type, price));
            }
            else if (name.equals("Alice")) {
                Alice.addOrder(orderbook.addOrder(qty, direction, type, price));
            }
        }
    }
}