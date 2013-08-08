package org.apache.activemq.recipes;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

public class EmbeddedSimpleJMS {

    private final String connectionUri = "tcp://localhost:61616";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private BrokerService service;

    public void before() throws Exception {

        service = BrokerFactory.createBroker("xbean:activemq.xml");
        service.start();

        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = session.createQueue("MyQueue");
    }

    public void after() throws Exception {

        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ex) {}
        }

        if (service != null) {
            try {
                service.stop();
            } catch (Exception ex) {}
        }
    }

    public void run() throws Exception {

        MessageProducer producer = session.createProducer(destination);
        try {
            TextMessage message = session.createTextMessage();
            message.setText("We sent a Message!");
            producer.send(message);
        } finally {
            producer.close();
        }

        MessageConsumer consumer = session.createConsumer(destination);
        try {
            TextMessage message = (TextMessage) consumer.receive();
            System.out.println(message.getText());
        } finally {
            consumer.close();
        }
    }

    public static void main(String[] args) {
        EmbeddedSimpleJMS example = new EmbeddedSimpleJMS();
        System.out.print("\n\n\n");
        System.out.println("Starting the Embedded Broker example now...");
        try {
            example.before();
            example.run();
            example.after();
        } catch (Exception e) {
            System.out.println("Caught an exception during the example: " + e.getMessage());
        }
        System.out.println("Finished running the Embedded Broker example.");
        System.out.print("\n\n\n");
    }
}