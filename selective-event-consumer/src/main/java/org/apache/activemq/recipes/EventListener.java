package org.apache.activemq.recipes;

import javax.jms.Message;
import javax.jms.MessageListener;

public class EventListener implements MessageListener {

    public void onMessage(Message message) {
        try {
            float price = message.getFloatProperty("price");
            String symbol = message.getStringProperty("symbol");
            System.out.println("Price Update: " + symbol + "[$" + price + "]");
        } catch (Exception e) {
            System.out.println("Worker caught an Exception");
        }
    }
}