package com.company.scheduler.model.dto.request;

import com.company.scheduler.model.enums.JobActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobActionRequest {

    @NotBlank
    private String jobName;

    @NotBlank
    private String jobGroup;

    @NotNull
    private JobActionType actionType;
}