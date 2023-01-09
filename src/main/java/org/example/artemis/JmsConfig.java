package org.example.artemis;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;

@Configuration
@EnableJms
public class JmsConfig
{
    @Bean
    public Queue eventQueue()
    {
        return new ActiveMQQueue("disco.event.in");
    }

    @Bean
    public Queue eventRetryQueue()
    {
        return new ActiveMQQueue("disco.event.retry");
    }
       @Bean
    public Queue orderQueue()
    {
        return new ActiveMQQueue("disco.order.in");
    }

    @Bean
    public Queue orderRetryQueue()
    {
        return new ActiveMQQueue("disco.order.retry");
    }

    @Bean
    public Queue journalQueue()
    {
        return new ActiveMQQueue("disco.journal.in");
    }

    @Bean
    public ConnectionFactory connectionFactory() throws JMSException
    {
        var artemisFactory = new ActiveMQConnectionFactory();
        artemisFactory.setBrokerURL("vm://0");
        artemisFactory.setConsumerWindowSize(0);
        artemisFactory.setBlockOnAcknowledge(false);
        artemisFactory.setBlockOnDurableSend(false);
        var factory = new JmsPoolConnectionFactory();
        factory.setConnectionFactory(artemisFactory);
        factory.setMaxConnections(240);
        return factory;
    }

    @Bean
    public JmsTemplate eventTemplate(ConnectionFactory connectionFactory, Queue eventQueue) {
        var jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setDefaultDestination(eventQueue);
        return jmsTemplate;
    }

    @Bean
    public DefaultMessageListenerContainer eventListener(ConnectionFactory connectionFactory, Queue eventQueue, AnyConsumer anyConsumer)
    {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setDestination(eventQueue);
        container.setMessageListener(anyConsumer);
        container.setMaxConcurrentConsumers(32);
        return container;
    }

    @Bean
    public DefaultMessageListenerContainer eventRetryListener(ConnectionFactory connectionFactory, Queue eventRetryQueue, AnyConsumer anyConsumer)
    {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setDestination(eventRetryQueue);
        container.setMessageListener(anyConsumer);
        container.setMaxConcurrentConsumers(32);
        return container;
    }

    @Bean
    public DefaultMessageListenerContainer orderListener(ConnectionFactory connectionFactory, Queue orderQueue, AnyConsumer anyConsumer)
    {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setDestination(orderQueue);
        container.setMessageListener(anyConsumer);
        container.setMaxConcurrentConsumers(32);
        return container;
    }

    @Bean
    public DefaultMessageListenerContainer orderRetryListener(ConnectionFactory connectionFactory, Queue orderRetryQueue, AnyConsumer anyConsumer)
    {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setDestination(orderRetryQueue);
        container.setMessageListener(anyConsumer);
        container.setMaxConcurrentConsumers(32);
        return container;
    }

    @Bean
    public DefaultMessageListenerContainer journalListener(ConnectionFactory connectionFactory, Queue journalQueue, AnyConsumer anyConsumer)
    {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setDestination(journalQueue);
        container.setMessageListener(anyConsumer);
        container.setMaxConcurrentConsumers(32);
        return container;
    }
}
