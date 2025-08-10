package com.company.scheduler.messaging.model;

import static com.company.scheduler.model.constant.ApplicationConstants.RabbitMqConstants.EXCHANGE_NAME;
import static com.company.scheduler.model.constant.ApplicationConstants.RabbitMqConstants.ROUTING_KEY;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.quartz.JobDataMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseQueueInfo {

    @NotBlank
    private String exchangeName;

    @NotBlank
    private String routingKey;

    public static ResponseQueueInfo from(JobDataMap jobDataMap) {
        return ResponseQueueInfo.builder()
                .exchangeName(jobDataMap.getString(EXCHANGE_NAME))
                .routingKey(jobDataMap.getString(ROUTING_KEY))
                .build();
    }
}
