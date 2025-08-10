package com.company.scheduler.messaging.model;

import com.company.scheduler.model.enums.Result;
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
public class BaseResultEvent<T extends Serializable> implements Serializable {

    private String eventId;
    private Result result;
    private String error;
    private Map<String, String> headers;
    private T payload;
}