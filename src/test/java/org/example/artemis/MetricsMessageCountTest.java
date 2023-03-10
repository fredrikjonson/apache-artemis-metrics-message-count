package org.example.artemis;

import static org.testng.Assert.assertEquals;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.postoffice.impl.LocalQueueBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@SpringBootTest(classes = MetricsMessageCountApplication.class)
public class MetricsMessageCountTest extends AbstractTestNGSpringContextTests
{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private JmsBroker broker;
    @Autowired
    private JmsTemplate eventJmsTemplate;
    @Autowired
    private DefaultMessageListenerContainer eventListener;

    /**
     * As far as I can determine the journal is empty, therefore I expect the message count to be zero.
     *
     * @throws Exception
     */
    @Test(enabled=true)
    public void testMetricsCanCountMessagesEventQueue() throws Exception {
        var registry = broker.broker().getMetricsManager().getMeterRegistry();
        assertEquals(registry.get("artemis.message.count").tag("address", "disco.event.in").gauge().value(), 0.0);
    }

    /**
     * As far as I can determine the journal is empty, therefore I expect the message count to be zero.
     *
     * @throws Exception
     */
    @Test(enabled=true)
    public void testMetricsCanCountMessagesOrderQueue() throws Exception {
       var registry = broker.broker().getMetricsManager().getMeterRegistry();
       assertEquals(registry.get("artemis.message.count").tag("address", "disco.order.in").gauge().value(), 0.0);
    }

    @Test
    public void logMetricsMessageSize() throws Exception {
        var registry = broker.broker().getMetricsManager().getMeterRegistry();
        for (String queueName : new String[] {
                "disco.event.in",
                "disco.event.retry",
                "disco.order.in",
                "disco.order.retry",
                "disco.journal.in"
        }) {
            try {
                var metric = registry.get("artemis.message.count").tag("address", queueName).gauge();
                logger.info("Queue {} = {}", queueName, metric.value());
            } catch (Exception cause) {
                logger.error("Failed to get metric for queue {}", queueName, cause);
            }
        }
    }

    /**
     * Show that the metrics plugin is not the source of the error.
     *
     * @throws Exception
     */
    @Test(enabled=true)
    public void testCanGetSameValueFromMetricsAsPostOffice() throws Exception {
        var registry = broker.broker().getMetricsManager().getMeterRegistry();
        var metricsMessageCount = registry.get("artemis.message.count").tag("address", "disco.event.in").gauge().value();
        var queue = ((LocalQueueBinding) broker.broker().getPostOffice().getBinding(new SimpleString("disco.event.in"))).getQueue();
        // While both are unexpectedly wrong, i.e not zero, they do match at least. Hence the mismatch is not introduced by micrometer.io.
        logger.info("Metrics message count, expecting 0, found {}", metricsMessageCount);
        logger.info("Post office message count, expecting 0, found {}", queue.getMessageCount());
        assertEquals(metricsMessageCount, queue.getMessageCount());
    }

    /**
     * This test illustrates that the metric artemis_message_count is affected when more messages
     * are added, but that even after resuming consumption and waiting for all messages to be
     * consumed again, the baseline returns to the wrong count, and does not go to zero as expected.
     *
     * @throws Exception
     */
    @Test(enabled=true)
    public void testCanIncrementDecrementFromBaseline() throws Exception {
        var registry = broker.broker().getMetricsManager().getMeterRegistry();
        var queue = ((LocalQueueBinding) broker.broker().getPostOffice().getBinding(new SimpleString("disco.event.in"))).getQueue();
        // N.B starts with erroneous count. Perhaps page count instead of message count?
        logger.info("Metrics message count, expecting 0, found {}", queue.getMessageCount());
        eventListener.stop();
        eventListener.shutdown();
        eventJmsTemplate.send(s -> { return s.createTextMessage("Test 1"); });
        eventJmsTemplate.send(s -> { return s.createTextMessage("Test 2"); });
        eventJmsTemplate.send(s -> { return s.createTextMessage("Test 3"); });
        // Wait for messages to be accepted.
        Thread.sleep(15_000);
        assertEquals(registry.get("artemis.message.count").tag("address", "disco.event.in").gauge().value(), 3.0);
        assertEquals(queue.getMessageCount(), 3.0);
        // Durable message count seems to align the expected message count.
        assertEquals(queue.getDurableMessageCount(), 3.0);
        // Begin consuming messages.
        eventListener.initialize();
        eventListener.start();
        // Wait for messages to be consumed.
        Thread.sleep(15_000);
        // Unexpected message count, 1.
        logger.info("Metrics message count, expecting 0, found {}", queue.getMessageCount());
        // Expected result, all messages consumed.
        assertEquals(registry.get("artemis.message.count").tag("address", "disco.event.in").gauge().value(), 0.0);
    }
}
