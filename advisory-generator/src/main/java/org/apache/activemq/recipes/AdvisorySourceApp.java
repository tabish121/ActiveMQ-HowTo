package org.apache.activemq.recipes;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class AdvisorySourceApp implements Runnable {

    private final String connectionUri = "tcp://localhost:61616";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private final Random rand = new Random();

    public void run() {
        try {
            connectionFactory = new ActiveMQConnectionFactory(connectionUri);
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue("MyQueue");

            TimeUnit.SECONDS.sleep(rand.nextInt(10));

            MessageProducer producer = session.createProducer(destination);
            producer.send(session.createTextMessage());

            TimeUnit.SECONDS.sleep(rand.nextInt(10));

            MessageConsumer consumer = session.createConsumer(destination);
            consumer.receive();

            TimeUnit.SECONDS.sleep(rand.nextInt(30));

            System.out.print(".");

            if (connection != null) {
                connection.close();
            }

        } catch (Exception ex) {}
    }

    public static void main(String[] args) {

        System.out.print("\n\n\n");
        System.out.println("Starting Advisory Message Generation app now...");
        try {

            ExecutorService service = Executors.newFixedThreadPool(10);
            for (int i = 0; i < 20; ++i) {
                service.execute(new AdvisorySourceApp());
            }

            service.shutdown();
            service.awaitTermination(5, TimeUnit.MINUTES);
            System.out.println();

        } catch (Exception e) {
            System.out.println("Caught an exception during the example: " + e.getMessage());
        }
        System.out.println("Finished running the Advisory Message Generation application.");
        System.out.print("\n\n\n");
    }
}