package org.apache.activemq.recipes;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.jms.Message;
import javax.jms.MessageListener;

public class JobQListener implements MessageListener {

    private final Random jobDelay = new Random();

    public void onMessage(Message message) {
        try {
            int jobId = message.getIntProperty("JobID");
            System.out.println("Worker processing job: " + jobId);
            TimeUnit.MILLISECONDS.sleep(jobDelay.nextInt(100));
        } catch (Exception e) {
            System.out.println("Worker caught an Exception");
        }
    }
}