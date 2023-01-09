package org.example.artemis;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
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

	@Test
	void testMetricsCanCountMessagesEventQueue() throws Exception {
	    var registry = broker.broker().getMetricsManager().getMeterRegistry();
	    assertEquals(registry.get("artemis.message.count").tag("address", "disco.event.in").gauge().value(), 0.0);
	}


	@Test
	void testMetricsCanCountMessagesOrderQueue() throws Exception {
	   var registry = broker.broker().getMetricsManager().getMeterRegistry();
       assertEquals(registry.get("artemis.message.count").tag("address", "disco.order.in").gauge().value(), 0.0);
	}

	@Test
	void testCanInject() throws Exception {
        assertNotNull(broker);
        assertNotNull(eventJmsTemplate);
        //Thread.sleep(60_000); // consume any remaining messages
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
}
