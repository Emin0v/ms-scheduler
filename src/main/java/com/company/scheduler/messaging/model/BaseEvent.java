package com.company.scheduler.messaging.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseEvent implements Serializable {

    private String eventId;
    private ResponseQueueInfo responseQueueInfo;
    private Map<String, String> headers;
    private Map<String, String> payload;
}
