package com.company.scheduler.model.dto.request;

import com.company.scheduler.messaging.model.ResponseQueueInfo;
import com.company.scheduler.util.annotation.ValidCron;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {

    @NotBlank
    private String jobName;

    @NotBlank
    private String jobGroup;

    @NotBlank
    @ValidCron
    private String cronExpression;

    @Valid
    @NotNull
    private ResponseQueueInfo responseQueueInfo;

    @NotNull
    private Map<@NotBlank String, @NotBlank String> payload;
}
