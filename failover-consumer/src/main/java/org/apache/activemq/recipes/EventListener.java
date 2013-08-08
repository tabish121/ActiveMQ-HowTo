package org.apache.activemq.recipes;

import javax.jms.Message;
import javax.jms.MessageListener;

public class EventListener implements MessageListener {

    public void onMessage(Message message) {
        try {
            int num = message.getIntProperty("num");
            System.out.println("Failover Consumer received Message #" + num);
        } catch (Exception e) {
            System.out.println("Worker caught an Exception");
        }
    }
}