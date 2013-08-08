package org.apache.activemq.recipes;

import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class FailoverConsumer {

    private final String connectionUri = "failover:tcp://localhost:61616";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;

    public void before() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = session.createQueue("MyQueue");
    }

    public void after() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public void run() throws Exception {
        MessageConsumer consumer = session.createConsumer(destination);
        consumer.setMessageListener(new EventListener());
        TimeUnit.MINUTES.sleep(10);
        consumer.close();
    }

    public static void main(String[] args) {
        FailoverConsumer producer = new FailoverConsumer();
        System.out.print("\n\n\n");
        System.out.println("Starting example Failover Consumer now...");
        try {
            producer.before();
            producer.run();
            producer.after();
        } catch (Exception e) {
        }
        System.out.println("Finished running the Failover Consumer example.");
        System.out.print("\n\n\n");
    }
}