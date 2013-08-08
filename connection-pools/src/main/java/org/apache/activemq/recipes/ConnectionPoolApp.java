package org.apache.activemq.recipes;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.pool.PooledConnectionFactory;

public class ConnectionPoolApp {

    private final String connectionUri = "tcp://localhost:61616";
    private ActiveMQConnectionFactory connectionFactory;
    private PooledConnectionFactory pooledFactory;
    private CountDownLatch done;
    private BrokerService broker;

    private final int NUM_SENDERS = 2000;
    private final int NUM_SENDS = 100;

    public void before() throws Exception {

        broker = new BrokerService();
        broker.setPersistent(false);
        broker.addConnector("tcp://0.0.0.0:61616");
        broker.start();

        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        pooledFactory = new PooledConnectionFactory(connectionFactory);
    }

    public void after() throws Exception {
        pooledFactory.clear();
        broker.stop();
    }

    public void run() throws Exception {
        System.out.print("Non-Pooled test took: ");
        long nonPooledTime = runPerformanceTest(connectionFactory);
        System.out.println(TimeUnit.MILLISECONDS.toSeconds(nonPooledTime) + " seconds");

        System.out.print("Pooled test took: ");
        long pooledTime = runPerformanceTest(pooledFactory);
        System.out.println(TimeUnit.MILLISECONDS.toSeconds(pooledTime) + " seconds");
    }

    class Sender implements Runnable {

        private final Connection connection;

        Sender(Connection connection) {
            this.connection = connection;
        }

        public void run() {
            try {
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                for (int i = 0; i < NUM_SENDS; i++) {
                    Destination destination = session.createTopic("TestTopic" + i);
                    MessageProducer producer = session.createProducer(destination);
                    producer.send(session.createMessage());
                    producer.close();
                }

                session.close();
                connection.close();
            } catch(Exception ex) {
                System.out.println("Run caught exception: " + ex.getMessage());
            } finally {
                done.countDown();
            }
        }
    }

    private long runPerformanceTest(ConnectionFactory factory) throws Exception {
        this.done = new CountDownLatch(NUM_SENDERS);
        ExecutorService service = Executors.newFixedThreadPool(10);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_SENDERS; ++i) {
            service.execute(new Sender(factory.createConnection()));
        }

        done.await();
        long endTime = System.currentTimeMillis();
        service.shutdown();

        return endTime - startTime;
    }

    public static void main(String[] args) {
        ConnectionPoolApp example = new ConnectionPoolApp();
        System.out.print("\n\n\n");
        System.out.println("Starting Pooled Connection example now...");
        try {
            example.before();
            example.run();
            example.after();
        } catch (Exception e) {
            System.out.println("Caught an exception during the example: " + e.getMessage());
        }
        System.out.println("Finished running the Pooled Connection example.");
        System.out.print("\n\n\n");
    }
}