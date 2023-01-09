package org.example.artemis;

import java.util.Map;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.config.MetricsConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.ActiveMQServers;
import org.apache.activemq.artemis.core.server.metrics.ActiveMQMetricsPlugin;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class JmsBroker {

    private ActiveMQServer broker;
    @Autowired
    private MeterRegistry meterRegistry;
    @Value("${artemis.data.directory}")
    private String dataDir;


    @PostConstruct
    private void init() throws Exception {
        var metricsPlugin = new JmsMetricsPlugin(meterRegistry);
        var config = new ConfigurationImpl()
                .setPersistenceEnabled(true)
                .setCreateJournalDir(true)
                .setJournalDirectory("%s/journal".formatted(dataDir))
                .setCreateBindingsDir(true)
                .setBindingsDirectory("%s/bindings".formatted(dataDir))
                .setPagingDirectory("%s/paging".formatted(dataDir))
                .setLargeMessagesDirectory("%s/largemessages".formatted(dataDir))
                .setJournalCompactMinFiles(0)
                .setJournalPoolFiles(1)
                .setJournalCompactPercentage(1)
                .setSecurityEnabled(false)
                .addAcceptorConfiguration("in-vm", "vm://0");
        config.setMetricsConfiguration(new MetricsConfiguration().setPlugin(metricsPlugin));
        var settings = new AddressSettings()
                .setAutoCreateDeadLetterResources(true)
                .setAutoCreateExpiryResources(true)
                .setAutoDeleteAddresses(false)
                .setAutoDeleteCreatedQueues(false)
                .setAutoDeleteQueues(false)
                .setDeadLetterQueueSuffix(new SimpleString("dlq"))
                .setExpiryQueueSuffix(new SimpleString("expiry"));
        broker = ActiveMQServers.newActiveMQServer(config);
        broker.getAddressSettingsRepository().setDefault(settings);
        broker.start();
    }

    @PreDestroy
    private void terminate() throws Exception {
      broker.stop(true);
    }

    public static final class JmsMetricsPlugin implements ActiveMQMetricsPlugin {

       private static final long serialVersionUID = 1L;
       private transient MeterRegistry meterRegistry;
       private Map<String, String> options;

       public JmsMetricsPlugin(MeterRegistry meterRegistry) {
           this.meterRegistry = meterRegistry;
       }

       @Override
       public ActiveMQMetricsPlugin init(Map<String, String> options) {
          this.options = options;
          return this;
       }

       @Override
       public MeterRegistry getRegistry() {
          return meterRegistry;
       }

       public Map<String, String> getOptions() {
          return options;
       }
    }

    /**
     * For unit tests only.
     */
    protected ActiveMQServer broker() {
        return broker;
    }
}
