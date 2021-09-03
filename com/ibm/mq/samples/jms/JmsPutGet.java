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
import javax.jms.Message;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Random;

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
		TextMessage message = null;
		//long uniqueNumber;

		try {
			
        	setupResources();
			
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
			
			for(int i=0; i>=0; i++){
				//uniqueNumber = System.currentTimeMillis() % 1000;				
				String correlationId = RandomString();
				//System.out.println("Mensaje de valor aleatorio " + correlationId + " con CorrelationId "+correlationId);
			    message = context.createTextMessage("Mensaje de valor aleatorio " + correlationId);
				message.setJMSCorrelationID(correlationId);
				producer = context.createProducer();
				producer.send(destination, message);
				//System.out.println("Mensaje enviado: "+message);
				now = LocalDateTime.now();
				System.out.println(dtf.format(now) + " ID de Mensaje enviado: " + i + " con contenido " + message.getText() );
				String jmsCorrelationID = " JMSCorrelationID = '" + message.getJMSCorrelationID() + "'";
				//System.out.println("Selector "+jmsCorrelationID);
				consumer = context.createConsumer(destination,jmsCorrelationID); // autoclosable								
				Message receivedMessage1 = consumer.receive(15000);// in ms or 15 seconds		
				//System.out.println("Mensaje recibido: "+receivedMessage1);
				now = LocalDateTime.now();
				System.out.println(dtf.format(now) + " ID de Mensaje recibido: " + i + " con contenido " + receivedMessage1.getBody(String.class)); 								
				recordSuccess();
				Thread.sleep(2000);
			}			
		} catch (Exception ex) {
			context.close();
			recordFailure(ex);
			System.out.println("CONNECTION CLOSED");
            //setupResources();			
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

	public static String RandomString() {
 
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();
		StringBuilder buffer = new StringBuilder(targetStringLength);
		for (int i = 0; i < targetStringLength; i++) {
			int randomLimitedInt = leftLimit + (int) 
			  (random.nextFloat() * (rightLimit - leftLimit + 1));
			buffer.append((char) randomLimitedInt);
		}
		String generatedString = buffer.toString();	
		//System.out.println(generatedString);
		return generatedString;
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
