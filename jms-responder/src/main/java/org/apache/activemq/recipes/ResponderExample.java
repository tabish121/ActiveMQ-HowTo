package org.apache.activemq.recipes;

import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ResponderExample implements MessageListener {

    private final String connectionUri = "tcp://localhost:61616";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer requestListener;
    private MessageProducer responder;

    public void before() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = session.createQueue("REQUEST.QUEUE");
        responder = session.createProducer(null);
        requestListener = session.createConsumer(destination);
        requestListener.setMessageListener(this);
    }

    public void after() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public void run() throws Exception {
        TimeUnit.MINUTES.sleep(5);
    }

    public void onMessage(Message message) {
        try {
            Destination replyTo = message.getJMSReplyTo();
            if (replyTo != null) {
                TextMessage textMessage = (TextMessage) message;
                System.out.println(textMessage.getText());
                Message response = session.createTextMessage("Job Finished");
                response.setJMSCorrelationID(message.getJMSCorrelationID());
                responder.send(replyTo, response);
            }
        } catch (Exception e) {
            System.out.println("Encounted an error while responding: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ResponderExample example = new ResponderExample();
        System.out.print("\n\n\n");
        System.out.println("Starting Responder example now...");
        try {
            example.before();
            example.run();
            example.after();
        } catch (Exception e) {
            System.out.println("Caught an exception during the example: " + e.getMessage());
        }
        System.out.println("Finished running the Responder example.");
        System.out.print("\n\n\n");
    }

}