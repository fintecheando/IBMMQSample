/*
* (c) Copyright IBM Corporation 2018
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.ibm.mq.samples.jms;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.TextMessage;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A minimal and simple application for Point-to-point messaging with reconnect for an HA Multi Instance WMQ Manager.
 *
 * Application makes use of fixed literals, any customisations will require
 * re-compilation of this source file. Application assumes that the named queue
 * is empty prior to a run.
 *
 * Notes:
 *
 * API type: JMS API (v2.0, simplified domain)
 *
 * Messaging domain: Point-to-point
 *
 * Provider type: IBM MQ
 *
 * Connection mode: Client connection
 *
 * JNDI in use: No
 *
 */
public class JmsPutGet {

	// System exit status value (assume unset value to be 1)
	private static int status = 1;

	// Create variables for the connection to MQ
	private static final String HOST = "192.168.49.140"; // Host name or IP address
	private static final int PORT = 10200; // Listener port for your queue manager
	private static final String CHANNEL = "CHANNEL1"; // Channel name
	private static final String QMGR = "HAQM1"; // Queue manager name
	private static final String APP_USER = ""; // User name that application uses to connect to MQ
	private static final String APP_PASSWORD = ""; // Password that the application uses to connect to MQ
	private static final String QUEUE_NAME = "SOURCE"; // Queue that the application uses to put and get messages to and from
	private static final int RECONNECT_TIMEOUT = 60; // 1 minute
    private static JMSContext context = null;
    private static 	Destination destination = null;
	/**
	 * Main method
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		// Variables
		JMSProducer producer = null;
		JMSConsumer consumer = null;		
		LocalDateTime now = null;

		try {
			
        	setupResources();

			long uniqueNumber = System.currentTimeMillis() % 1000;
			TextMessage message = context.createTextMessage("Your lucky number today is " + uniqueNumber);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
			
			for(int i=0; i>=0; i++){
				producer = context.createProducer();
				producer.send(destination, message);
				//System.out.println("Sent message:\n " + i + " " + message);
				System.out.println("\nMensaje enviado:\n " + i );
				now = LocalDateTime.now();
				System.out.println(dtf.format(now));
				consumer = context.createConsumer(destination); // autoclosable
				String receivedMessage = consumer.receiveBody(String.class, 15000); // in ms or 15 seconds
				//System.out.println("\nReceived message:\n " + i + " " + receivedMessage);
				System.out.println("\nMensaje recibido:\n " + i );
				now = LocalDateTime.now();
				System.out.println(dtf.format(now));
				Thread.sleep(1000);
			}
			context.close();

			recordSuccess();
		} catch (Exception ex) {
			recordFailure(ex);
			System.out.println("DETECTING ERROR... RECONNECTING");
            setupResources();
			
		}

	} // end main()
        
	/**
	 * Setup the connection to the Queue Manager
	 */
    private static void setupResources() { 

        boolean connected = false; 
        while (!connected) { 
            try { 
                // Create a connection factory
				JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
				JmsConnectionFactory cf = ff.createConnectionFactory();				
				
				// Set the properties
				cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, HOST);
				cf.setIntProperty(WMQConstants.WMQ_PORT, PORT);				
				//cf.setStringProperty(WMQConstants.WMQ_CONNECTION_NAME_LIST, "192.168.49.140(10200),192.168.49.131(10200),192.168.49.132(10200)");
				cf.setStringProperty(WMQConstants.WMQ_CHANNEL, CHANNEL);
				cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
				cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, QMGR);
				cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsPutGet (JMS)");
				cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
				cf.setStringProperty(WMQConstants.USERID, APP_USER);
				cf.setStringProperty(WMQConstants.PASSWORD, APP_PASSWORD);
				cf.setIntProperty(WMQConstants.WMQ_CLIENT_RECONNECT_TIMEOUT, RECONNECT_TIMEOUT);
				cf.setIntProperty(WMQConstants.WMQ_CLIENT_RECONNECT_OPTIONS, WMQConstants.WMQ_CLIENT_RECONNECT);
				//cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, "*TLS12");				

				// Create JMS objects
				context = cf.createContext();
				destination = context.createQueue("queue:///" + QUEUE_NAME);
				// no exception? then we connected ok 
				connected = true; 
				System.out.println("CONNECTED");
            } 
            catch (JMSException je) { 
                // sleep and then have another attempt 
				System.out.println("RECONNECTING");
                try {Thread.sleep(30*1000);} catch (InterruptedException ie) {} 
            } 
        } 
    }

	/**
	 * Record this run as successful.
	 */
	private static void recordSuccess() {
		System.out.println("SUCCESS");
		status = 0;
		return;
	}

	/**
	 * Record this run as failure.
	 *
	 * @param ex
	 */
	private static void recordFailure(Exception ex) {
		if (ex != null) {
			if (ex instanceof JMSException) {
				processJMSException((JMSException) ex);
			} else {
				System.out.println(ex);
			}
		}
		System.out.println("FAILURE");
		status = -1;
		return;
	}

	/**
	 * Process a JMSException and any associated inner exceptions.
	 *
	 * @param jmsex
	 */
	private static void processJMSException(JMSException jmsex) {
		System.out.println(jmsex);
		Throwable innerException = jmsex.getLinkedException();
		if (innerException != null) {
			System.out.println("Inner exception(s):");
		}
		while (innerException != null) {
			System.out.println(innerException);
			innerException = innerException.getCause();
		}
		return;
	}
        
        

}
