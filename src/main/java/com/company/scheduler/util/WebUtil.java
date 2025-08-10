package com.company.scheduler.util;

import static com.company.scheduler.model.constant.ApplicationConstants.HttpAttribute.BEARER;
import static com.company.scheduler.model.constant.ApplicationConstants.HttpAttribute.REQUEST_START_TIME;
import static com.company.scheduler.model.constant.ApplicationConstants.HttpHeader.AC_CLIENT_IP;
import static com.company.scheduler.model.constant.ApplicationConstants.HttpHeader.AC_REQUEST_ID;
import static com.company.scheduler.model.constant.ApplicationConstants.HttpHeader.AC_USER_AGENT;
import static com.company.scheduler.model.constant.ApplicationConstants.HttpHeader.AUTHORIZATION;
import static com.company.scheduler.model.constant.ApplicationConstants.HttpHeader.USER_AGENT;
import static com.company.scheduler.model.constant.ApplicationConstants.HttpHeader.X_REAL_IP;

import com.company.scheduler.model.dto.ProjectBasedHeadersDto;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Slf4j
@Component
public class WebUtil {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private Tracer tracer;

    public String getRequestId() {
        return Optional.ofNullable(getHttpServletRequest())
                .map(req -> req.getHeader(AC_REQUEST_ID))
                .filter(StringUtils::isNotBlank)
                .orElse(null);
    }

    public String getProjectBasedHeader(String headerKey) {
        return Optional.ofNullable(getHttpServletRequest())
                .map(req -> req.getHeader(headerKey))
                .orElse("");
    }

    public String getRequestUri() {
        return Optional.ofNullable(getHttpServletRequest())
                .map(req -> req.getMethod() + " " + req.getRequestURI())
                .orElse("");
    }

    public Map<String, String> getAllRequestHeaders() {
        Map<String, String> headers = new HashMap<>();
        final HttpServletRequest request = getHttpServletRequest();
        if (request == null || request.getHeaderNames() == null) {
            return headers;
        }
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            headers.put(key, value);
        }
        return headers;
    }

    public Map<String, String> getAcHeaders() {
        Map<String, String> headers = new HashMap<>();
        HttpServletRequest request = getHttpServletRequest();
        if (request == null || request.getHeaderNames() == null) {
            return headers;
        }
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            if (headerName.toLowerCase().startsWith("ac")) {
                headers.put(headerName.toLowerCase(), request.getHeader(headerName));
            }
        });
        return headers;
    }

    public Long getElapsedTime() {
        return Optional.ofNullable(getHttpServletRequest())
                .map(req -> String.valueOf(req.getAttribute(REQUEST_START_TIME)))
                .filter(StringUtils::isNumeric)
                .map(t -> System.currentTimeMillis() - Long.parseLong(t))
                .orElse(-1L);
    }

    public String getRequestBody() {
        try {
            return Optional.ofNullable(getHttpServletRequest())
                    .filter(ContentCachingRequestWrapper.class::isInstance)
                    .map(ContentCachingRequestWrapper.class::cast)
                    .map(ContentCachingRequestWrapper::getContentAsByteArray)
                    .map(String::new)
                    .orElse("[null]");
        } catch (Exception e) {
            log.error("{} error occurs while getting request body for uri {}", e.getMessage(), getRequestUri(), e);
            return "[failed]";
        }
    }

    private HttpServletRequest getHttpServletRequest() {
        try {
            if (httpServletRequest != null && httpServletRequest.getHeaderNames() != null) {
                return httpServletRequest;
            }
        } catch (Exception ex) {
            log.debug("[WebUtil.getHttpServletRequest] : {}", ex.getMessage());
        }

        return null;
    }

}