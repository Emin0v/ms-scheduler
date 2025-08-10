package com.company.scheduler.job;

import static com.company.scheduler.model.constant.ApplicationConstants.RabbitMqConstants.PAYLOAD;

import com.company.scheduler.messaging.MessageProducer;
import com.company.scheduler.messaging.model.BaseEvent;
import com.company.scheduler.messaging.model.ResponseQueueInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class MessagePublisherJob extends QuartzJobBean {

    private final ObjectMapper objectMapper;
    private final MessageProducer messageProducer;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        final String payloadJson = jobDataMap.getString(PAYLOAD);

        try {
            final var payload = objectMapper.readValue(payloadJson, new TypeReference<Map<String, String>>() {
            });

            var event = BaseEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .payload(payload)
                    .responseQueueInfo(ResponseQueueInfo.from(jobDataMap))
                    .build();

            messageProducer.send(event);
        } catch (JsonProcessingException e) {
            log.error("Invalid payload JSON: {}", payloadJson, e);
            throw new JobExecutionException("Payload parsing failed", e);
        } catch (Exception e) {
            log.error("Failed to execute RabbitMQ job", e);
            throw new JobExecutionException("Unexpected error", e);
        }
    }
}
