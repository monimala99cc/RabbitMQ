package com.javapingpong.rabitmq;

import com.rabbitmq.client.*;

public class PingPong {

    private final static String QUEUE_NAME_IN = "ping";
    private final static String QUEUE_NAME_OUT = "pong";

    // Messages
    static String ping = "PING";
    static String pong = "PONG";
    static String ask = "PING";
    static String ack = "ACK";

    // Booleans
    static boolean initialized = false;
    static int ID = 1;

    // Channels
    static Channel channelPing;
    static Channel channelPong;

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        channelPing = connection.createChannel();
        channelPing.queueDeclare(QUEUE_NAME_IN, false, false, false, null);

        channelPong = connection.createChannel();
        channelPong.queueDeclare(QUEUE_NAME_OUT, false, false, false, null);

        send(ask, QUEUE_NAME_OUT); // Start the Ping-Pong process

        receive(); // Start listening on both queues
    }

   
    public static void send(String msg, String queueName) {
        try {
            Channel channel = queueName.equals(QUEUE_NAME_IN) ? channelPing : channelPong;
            channel.basicPublish("", queueName, null, msg.getBytes());
            System.out.println("Sent: '" + msg + "' to " + queueName);

            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void receive() throws Exception {
        System.out.println("Listening for messages...");

        // Listen on `ping` queue
        DeliverCallback deliverCallbackPing = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("[x] Received from ping: '" + message + "'");
            handleReceivedMessage(message, QUEUE_NAME_OUT);
        };
        channelPing.basicConsume(QUEUE_NAME_IN, true, deliverCallbackPing, consumerTag -> {});

        // Listen on `pong` queue
        DeliverCallback deliverCallbackPong = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("[x] Received from pong: '" + message + "'");
            handleReceivedMessage(message, QUEUE_NAME_IN);
        };
        channelPong.basicConsume(QUEUE_NAME_OUT, true, deliverCallbackPong, consumerTag -> {});
    }

    private static void handleReceivedMessage(String message, String responseQueue) {
    	
    	System.out.println("calling handleReceivedMessage:  "+message);
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
    	switch (message) {
            case "ACK":
                send(ping, responseQueue);
                break;
            case "PING":
                send(pong, responseQueue);
                break;
            case "PONG":
                send(ping, responseQueue);
                break;
            case "WANT2PING":
                if (!initialized) {
                    send(ack, responseQueue);
                    initialized = true;
                } 
                break;
            default:
                System.out.println("Received an unrecognized message: " + message);
        }
    }
}

