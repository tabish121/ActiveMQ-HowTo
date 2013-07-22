package org.apache.activemq.recipes;

import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class RequesterExample {

    private final String connectionUri = "tcp://localhost:61616";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;

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

        while (true) {
            TextMessage request = session.createTextMessage("Are we there yet?");
            request.setJMSReplyTo(responseQ);
            request.setJMSCorrelationID("request");
            System.out.println(request.getText());
            requester.send(request);

            Message response = responseListener.receive(TimeUnit.MINUTES.toMillis(5));

            if (response != null) {
                String jmsCorrelation = response.getJMSCorrelationID();
                if (!jmsCorrelation.equals("request")) {
                    System.out.println("Received an unexpected response: " + jmsCorrelation);
                }
                TextMessage txtResponse = (TextMessage) response;
                System.out.println(txtResponse.getText());

                if (txtResponse.getBooleanProperty("quit")) {
                    break;
                }
            } else {
                System.out.println("Never got a response for our request!");
            }
        }

        System.out.println("Woohoo!");
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