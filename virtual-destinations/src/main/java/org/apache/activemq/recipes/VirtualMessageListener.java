package org.apache.activemq.recipes;

import java.util.concurrent.CountDownLatch;

import javax.jms.Message;
import javax.jms.MessageListener;

public class VirtualMessageListener implements MessageListener {

    private int numReceived;
    private final CountDownLatch done;

    public VirtualMessageListener(CountDownLatch done) {
        this.done = done;
    }

    public int getNumReceived() {
        return numReceived;
    }

    public void onMessage(Message message) {
        ++numReceived;
        done.countDown();
    }
}