package org.apache.activemq.recipes;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class JobQConsumer {

    private final String connectionUri = "tcp://localhost:61616?jms.prefetchPolicy.queuePrefetch=1";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private Destination control;

    public void before() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = session.createQueue("JOBQ.Work");
        control = session.createTopic("Worker.Control");
    }

    public void after() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public void run() throws Exception {
        MessageConsumer consumer = session.createConsumer(destination);
        consumer.setMessageListener(new JobQListener());

        Session controlSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer controlConsumer = controlSession.createConsumer(control);
        controlConsumer.receive();

        connection.stop();

        consumer.close();
        controlSession.close();
    }

    public static void main(String[] args) {
        JobQConsumer producer = new JobQConsumer();
        System.out.print("\n\n\n");
        System.out.println("Starting example Consumer now...");
        try {
            producer.before();
            producer.run();
            producer.after();
        } catch (Exception e) {
            System.out.println("Caught an exception during the example: " + e.getMessage());
        }
        System.out.println("Finished running the sample Consumer app.");
        System.out.print("\n\n\n");
    }

}