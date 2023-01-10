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

Note that the tests can be executed repeatedly, starting and stopping the
broker does not correct the value.

== Background ==

See activemq users mailing list thread:

 "Artemis 2.27.1, metrics reports wrong message count on persisted queues"
 https://lists.apache.org/thread/08q2jx7fc28sx7ps470syhg1o4h4bj3t
