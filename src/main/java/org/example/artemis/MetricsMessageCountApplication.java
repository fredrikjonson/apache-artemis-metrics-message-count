package org.example.artemis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

@SpringBootApplication
public class MetricsMessageCountApplication {

    @Autowired
    JmsBroker jmsBroker;
    @Autowired
    DefaultMessageListenerContainer eventListener;
    @Autowired
    DefaultMessageListenerContainer eventRetryListener;
    @Autowired
    DefaultMessageListenerContainer orderListener;
    @Autowired
    DefaultMessageListenerContainer orderRetryListener;
    @Autowired
    DefaultMessageListenerContainer journalListener;

    public static void main(String[] args) {
        SpringApplication.run(MetricsMessageCountApplication.class, args);
    }
}
