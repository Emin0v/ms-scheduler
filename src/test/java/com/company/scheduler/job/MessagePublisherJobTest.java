package com.company.scheduler.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.company.scheduler.messaging.MessageProducer;
import com.company.scheduler.messaging.model.BaseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;

class MessagePublisherJobTest {

    private final StreamBridge bridge = mock(StreamBridge.class);
    private final MessagePublisherJob job = new MessagePublisherJob(new ObjectMapper(), new MessageProducer(bridge));

    @Test
    void rejectedPublicationFailsQuartzExecution() {
        when(bridge.send(eq("notificationEventProducer-out-0"), any())).thenReturn(false);
        assertThatThrownBy(() -> job.executeInternal(context("{\"message\":\"hello\"}")))
                .isInstanceOf(JobExecutionException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasStackTraceContaining("not accepted for delivery");
    }

    @Test
    void acceptedPublicationPreservesPayloadAndRoutingKey() throws JobExecutionException {
        when(bridge.send(eq("notificationEventProducer-out-0"), any())).thenReturn(true);
        job.executeInternal(context("{\"message\":\"hello\"}"));
        ArgumentCaptor<Message> message = ArgumentCaptor.forClass(Message.class);
        verify(bridge).send(eq("notificationEventProducer-out-0"), message.capture());
        BaseEvent event = (BaseEvent) message.getValue().getPayload();
        assertThat(event.getEventId()).isNotBlank();
        assertThat(event.getPayload()).isEqualTo(Map.of("message", "hello"));
        assertThat(message.getValue().getHeaders()).containsEntry("routingKey", "example.response");
    }

    @Test
    void invalidJsonFailsBeforePublication() {
        assertThatThrownBy(() -> job.executeInternal(context("not-json")))
                .isInstanceOf(JobExecutionException.class);
        verifyNoInteractions(bridge);
    }

    private JobExecutionContext context(String payload) {
        var details = JobBuilder.newJob(MessagePublisherJob.class)
                .withIdentity("test", "test")
                .usingJobData(new JobDataMap(Map.of("payload", payload,
                        "exchangeName", "example.responses", "routingKey", "example.response")))
                .build();
        JobExecutionContext context = mock(JobExecutionContext.class);
        when(context.getJobDetail()).thenReturn(details);
        return context;
    }
}
