package io.github.MoYuSOwO;

import io.github.MoYuSOwO.agent.AgentAccount;
import io.github.MoYuSOwO.stock.Order;
import io.github.MoYuSOwO.stock.OrderBook;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        OrderBook orderbook = new OrderBook(100);
        AgentAccount Mary = new AgentAccount(20000, 500, 100, orderbook);
        AgentAccount Alice = new AgentAccount(20000, 500, 100, orderbook);
        orderbook.addListener(Mary.getAccountId(), Mary);
        orderbook.addListener(Alice.getAccountId(), Alice);
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
            else if ((!name.equals("Mary")) && (!name.equals("Alice"))) {
                continue;
            }
            String s = input.next();
            if (s.equals("cash")) {
                if (name.equals("Mary")) {
                    System.out.println(Mary.getCash());
                    continue;
                }
                else if (name.equals("Alice")) {
                    System.out.println(Alice.getCash());
                    continue;
                }
            }
            else if (s.equals("position")) {
                if (name.equals("Mary")) {
                    System.out.println(Mary.getPosition());
                    continue;
                }
                else if (name.equals("Alice")) {
                    System.out.println(Alice.getPosition());
                    continue;
                }
            }
            else if (s.equals("cost")) {
                if (name.equals("Mary")) {
                    System.out.println(Mary.getCostPerPosition());
                    continue;
                }
                else if (name.equals("Alice")) {
                    System.out.println(Alice.getCostPerPosition());
                    continue;
                }
            }
            int qty = Integer.parseInt(s);
            double price = 0.0;
            Order.orderDirection direction = Order.toOrderDirection(input.next());
            Order.orderType type = Order.toOrderType(input.next());
            if (type == Order.orderType.LIMIT) price = input.nextDouble();
            if (name.equals("Mary")) {
                Mary.addOrder(qty, direction, price);
            }
            else if (name.equals("Alice")) {
                Alice.addOrder(qty, direction, price);
            }
        }
    }
}