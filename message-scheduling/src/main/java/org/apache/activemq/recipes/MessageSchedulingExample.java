package org.apache.activemq.recipes;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ScheduledMessage;

public class MessageSchedulingExample implements MessageListener {

    private final String connectionUri = "tcp://localhost:61616";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageProducer producer;
    private MessageConsumer consumer;

    private final long delay = TimeUnit.SECONDS.toMillis(10);
    private final long period = TimeUnit.SECONDS.toMillis(5);
    private final int repeat = 9;

    private final CountDownLatch done = new CountDownLatch(10);

    public void before() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = session.createTopic("Alarm.Clock");
        producer = session.createProducer(destination);
        consumer = session.createConsumer(destination);
        consumer.setMessageListener(this);
        connection.start();
    }

    public void after() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public void onMessage(Message message) {
        try {
            TextMessage text = (TextMessage) message;
            System.out.println(text.getText());
        } catch (JMSException e) {
        }
        done.countDown();
    }

    public void run() throws Exception {
        TextMessage message = session.createTextMessage("Wake Up!");
        message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
        message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, period);
        message.setIntProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, repeat);
        producer.send(message);
        System.out.println("Wakeup call set, going to sleep now.");
        done.await();
        System.out.println("Fine! I'm awake!");
    }

    public static void main(String[] args) {
        MessageSchedulingExample example = new MessageSchedulingExample();
        System.out.print("\n\n\n");
        System.out.println("Starting Scheduled Message example now...");
        try {
            example.before();
            example.run();
            example.after();
        } catch (Exception e) {
            System.out.println("Caught an exception during the example: " + e.getMessage());
        }
        System.out.println("Finished running the Scheduled Message example.");
        System.out.print("\n\n\n");
    }
}