package org.apache.activemq.recipes;

import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class FailoverProducer {

    private final String connectionUri = "failover:tcp://localhost:61616";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;

    public void before() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = session.createQueue("MyQueue");
    }

    public void after() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public void run() throws Exception {
        MessageProducer producer = session.createProducer(destination);
        for (int i = 1; i <= 10000; ++i) {
            Message message = session.createMessage();
            message.setIntProperty("num", i);
            producer.send(message);
            System.out.println("Failover Producer sent Message #" + i);
            TimeUnit.SECONDS.sleep(1);
        }
        producer.close();
    }

    public static void main(String[] args) {
        FailoverProducer producer = new FailoverProducer();
        System.out.print("\n\n\n");
        System.out.println("Starting example Failover Producer now...");
        try {
            producer.before();
            producer.run();
            producer.after();
        } catch (Exception e) {
            System.out.println("Caught an exception during the example: " + e.getMessage());
        }
        System.out.println("Finished running the sample Failover Producer app.");
        System.out.print("\n\n\n");
    }
}