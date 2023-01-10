= Journal data for unexpected metric artemis_message_count =

Test case and journal data to reproduce an unexpected value for artemis_message_count.

* Test cases src/test/java/org/example/artemis/MetricsMessageCountTest.java
* journal data in the 'data' directory.

== Expection and outcome ==

I expect that the 'artemis.message.count' / 'artemis_message_count' for the
queues represents the number of messages stored in the queue.

Unexpectedly to me it appears that the baseline for the message count given
this journal data is the queue page size. Verifiable with the section 'Page
Count' exposed by the 'artemis data print' command.

Running 'artemis data recover' and replacing the journal files fixes the
problem.

== Usage ==

 mvn clean test

 > [ERROR] Failures:
 > [ERROR]   MetricsMessageCountTest>AbstractTestNGSpringContextTests.run:154
 >           ->testCanIncrementDecrementFromBaseline:103 expected [3.0] but found [4.0]
 > [ERROR]   MetricsMessageCountTest>AbstractTestNGSpringContextTests.run:154
 >           ->testMetricsCanCountMessagesEventQueue:34 expected [0.0] but found [4.0]
 > [ERROR]   MetricsMessageCountTest>AbstractTestNGSpringContextTests.run:154
 >           ->testMetricsCanCountMessagesOrderQueue:45 expected [0.0] but found [146.0]
 > [INFO]
 > [ERROR] Tests run: 5, Failures: 3, Errors: 0, Skipped: 0

Note that the tests can be executed repeatedly, starting and stopping the
broker does not correct the value.

== Workaround ==

Using artemis data recover appears to resolve the issue:

 export ARTEMIS_HOME=/your/installation/path/apache-artemis-2.27.1
 ./artemis.sh data recover --broker etc/broker.xml --target recover
 rm data/journal/*.amq
 mv -i recover/* data/journal/

 mvn clean test

 > Tests run: 5, Failures: 0, Errors: 0, Skipped: 0

== Background ==

See activemq users mailing list thread:

 "Artemis 2.27.1, metrics reports wrong message count on persisted queues"
 https://lists.apache.org/thread/08q2jx7fc28sx7ps470syhg1o4h4bj3t

The embedded broker version has been updated a few times during 2022, from
2.24, through 2.26, 2.27 to 2.27.1. We have had a few unclean shutdowns
unrelated to artemis, which might have introduced the issue.
