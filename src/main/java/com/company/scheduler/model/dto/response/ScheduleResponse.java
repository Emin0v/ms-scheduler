package com.company.scheduler.model.dto.response;

import com.company.scheduler.messaging.model.ResponseQueueInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {

    private String jobName;
    private String jobGroup;
    private String cronExpression;
    private String jobStatus;
    private ResponseQueueInfo responseQueueInfo;
}