package com.company.scheduler.model.constant;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class ApplicationConstants {

    @NoArgsConstructor(access = PRIVATE)
    public static final class HttpAttribute {
        public static final String REQUEST_START_TIME = "request_start_time";
        public static final String BEARER = "Bearer ";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static final class HttpHeader {
        public static final String X_FORWARDED_FOR = "X-Forwarded-For";
        public static final String X_REAL_IP = "X-Real-IP";
        public static final String USER_AGENT = "User-Agent";
        public static final String AUTHORIZATION = "Authorization";

        public static final String AC_REQUEST_ID = "ac-request-id";
        public static final String AC_USER_AGENT = "ac-user-agent";
        public static final String AC_CLIENT_IP = "ac-client-ip";
        public static final String AC_LANGUAGE = "ac-lang";
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class RabbitMqConstants {
        public static final String BINDING_NAME = "notificationEventProducer-out-0";
        public static final String EXCHANGE_NAME = "exchangeName";
        public static final String ROUTING_KEY = "routingKey";
        public static final String PAYLOAD = "payload";

    }
}
