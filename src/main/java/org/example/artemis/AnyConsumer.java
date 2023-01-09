package org.example.artemis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

@Component
public class AnyConsumer implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onMessage(Message message) {
        try {
            logger.info("Consumed {} on {}", ((TextMessage) message).getText(), message.getJMSDestination());
        } catch (Exception cause) {
            logger.warn("Failed to consume message.", cause);
        }
    }
}
