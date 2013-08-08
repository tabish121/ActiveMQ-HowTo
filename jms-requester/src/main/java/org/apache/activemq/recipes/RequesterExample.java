package org.apache.activemq.recipes;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class RequesterExample implements MessageListener {

    private final String connectionUri = "tcp://localhost:61616";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private static final int NUM_REQUESTS = 10;
    private final CountDownLatch done = new CountDownLatch(NUM_REQUESTS);

    public void before() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = session.createQueue("REQUEST.QUEUE");
    }

    public void after() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public void run() throws Exception {
        TemporaryQueue responseQ = session.createTemporaryQueue();
        MessageProducer requester = session.createProducer(destination);
        MessageConsumer responseListener = session.createConsumer(responseQ);
        responseListener.setMessageListener(this);

        for (int i = 0; i < NUM_REQUESTS; i++) {
            TextMessage request = session.createTextMessage("Job Request");
            request.setJMSReplyTo(responseQ);
            request.setJMSCorrelationID("request: " + i);
            System.out.println(request.getText());
            requester.send(request);
        }

        if (done.await(10, TimeUnit.MINUTES)) {
            System.out.println("Woohoo! Work's all done!");
        } else {
            System.out.println("Doh!! Work didn't get done.");
        }
    }

    public void onMessage(Message message) {
        try {
            String jmsCorrelation = message.getJMSCorrelationID();
            if (!jmsCorrelation.startsWith("request")) {
                System.out.println("Received an unexpected response: " + jmsCorrelation);
            }
            TextMessage txtResponse = (TextMessage) message;
            System.out.println(txtResponse.getText());
            done.countDown();
        } catch (Exception ex) {
        }
    }

    public static void main(String[] args) {
        RequesterExample example = new RequesterExample();
        System.out.print("\n\n\n");
        System.out.println("Starting Requester example now...");
        try {
            example.before();
            example.run();
            example.after();
        } catch (Exception e) {
            System.out.println("Caught an exception during the example: " + e.getMessage());
        }
        System.out.println("Finished running the Requester example.");
        System.out.print("\n\n\n");
    }

}