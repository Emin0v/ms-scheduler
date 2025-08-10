package com.company.scheduler.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBasedHeadersDto {

    private String requestId;
    private String customerId;
    private String userId;
    private String userRole;
    private String email;
    private String username;
    private String deviceId;
    private String deviceModel;
    private String appVersion;
    private String originService;
    private String userAgent;
    private String clientIp;
    private String lang;
}
