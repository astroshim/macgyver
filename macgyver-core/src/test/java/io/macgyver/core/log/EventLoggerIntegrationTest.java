package io.macgyver.core.log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.Test;
import org.slf4j.event.EventRecodingLogger;
import org.springframework.beans.factory.annotation.Autowired;

import io.macgyver.core.log.EventLogger;
import io.macgyver.core.log.EventLogger.LogMessage;
import io.macgyver.test.MacGyverIntegrationTest;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.selector.Selectors;

public class EventLoggerIntegrationTest extends MacGyverIntegrationTest {

	@Autowired
	EventBus eventBus;

	@Autowired
	EventLogger eventLogger;

	@Test
	public void testIt() throws InterruptedException {
	
		Assertions.assertThat(eventBus).isNotNull();
		
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<Event<LogMessage>> ref = new AtomicReference<Event<LogMessage>>(null);
		eventBus.on(Selectors.T(LogMessage.class),(Event<LogMessage> x)->{
			ref.set(x);
			latch.countDown();
		});
		
		eventLogger.event().withProperty("foo", "bar").log();

		Assertions.assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
		

		Assertions.assertThat(ref.get().getData().getJsonNode().path("foo").asText()).isEqualTo("bar");
	
	}
}
