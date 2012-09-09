package ar.cxf;

import java.net.URI;
import java.util.Date;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.store.jdbc.JDBCPersistenceAdapter;
import org.apache.activemq.store.jdbc.Statements;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class EmbeddedBrokers {

	static DefaultMessageListenerContainer listener;
	
	public static void main(String[] args) throws Exception {
		BrokerService  centralBroker = createEmbeddedBroker("Central", "61616", null);
		centralBroker.start();
		BrokerService  broker1 = createEmbeddedBroker("Broker1", "61617", "61616");
		broker1.start();
		BrokerService  broker2 = createEmbeddedBroker("Broker2", "61618", "61616");
		broker2.start();
		BrokerService  broker3 = createEmbeddedBroker("Broker3", "61619", "61616");
		broker3.start();
		
		SingleConnectionFactory con0 = new SingleConnectionFactory(new ActiveMQConnectionFactory("tcp://localhost:61616"));
		startListener(con0);
		SingleConnectionFactory con1 = new SingleConnectionFactory(new ActiveMQConnectionFactory("tcp://localhost:61617"));
		SingleConnectionFactory con2 = new SingleConnectionFactory(new ActiveMQConnectionFactory("tcp://localhost:61618"));
		SingleConnectionFactory con3 = new SingleConnectionFactory(new ActiveMQConnectionFactory("tcp://localhost:61619"));
		
		sendDelete(con1);
		sendDelete(con1);
		sendDelete(con1);
		sendDelete(con2);
		sendDelete(con3);
		sendDelete(con1);
		sendDelete(con2);
		sendDelete(con3);

	}

	static BrokerService createEmbeddedBroker(String brokername, String port, String centralBrokerPort){
		BrokerService broker = new BrokerService();
		try {
			broker.setBrokerName(brokername);
			broker.setUseJmx(false);
			broker.addConnector("tcp://localhost:" + port);

			
			JDBCPersistenceAdapter jdbc = new JDBCPersistenceAdapter();
			broker.setPersistenceAdapter(jdbc);
			
			BasicDataSource datasource = new BasicDataSource();
			jdbc.setDataSource(datasource);
			jdbc.setUseDatabaseLock(false);
			
			datasource.setUsername("ikon");
			datasource.setPassword("ikon");
			datasource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
			datasource.setUrl("jdbc:oracle:thin:@127.0.0.1:1521:xe");
			Statements statement = new Statements();
			statement.setTablePrefix(brokername);
			jdbc.setStatements(statement);
			jdbc.deleteAllMessages();
			
			if(centralBrokerPort != null)
				broker.addNetworkConnector(new URI("static:(tcp://localhost:" + centralBrokerPort +")"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return broker;
	}


	
	
	static void sendDelete(SingleConnectionFactory connectionFactory) {
		
		JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
		jmsTemplate.setSessionTransacted(true);
		jmsTemplate.setDeliveryMode(DeliveryMode.PERSISTENT);
		jmsTemplate.setTimeToLive(0);
		jmsTemplate.send("Test.QUEUE1",  new MessageCreator() {
		           public Message createMessage(Session session) throws JMSException {
		               return session.createTextMessage(new Date().toString());
		             }
		         });
	}

	static void startListener(SingleConnectionFactory con) {
		
		listener = new DefaultMessageListenerContainer();		
		listener.setConnectionFactory(con);
		listener.setDestinationName("Test.QUEUE1");
		listener.setPubSubDomain(false);
		listener.setConnectionFactory(con);
		listener.setSessionTransacted(true);
		
		MessageListener msgListener = new MessageListener(){

			public void onMessage(Message arg0) {
				System.out.println("Receive: " + arg0);
			}
			
		}; 
		listener.setMessageListener(msgListener);
		listener.initialize();
		listener.start();
	}
}

