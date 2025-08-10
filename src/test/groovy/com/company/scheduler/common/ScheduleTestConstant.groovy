package com.company.scheduler.common

import com.company.scheduler.model.dto.request.ScheduleRequest
import com.company.scheduler.model.dto.response.ScheduleResponse
import org.quartz.JobKey
import org.quartz.TriggerKey

class ScheduleTestConstant {

    static final String JOB_NAME = "testJob"
    static final String JOB_GROUP = "testGroup"
    static final String CRON_EXPRESSION = "0/5 * * * * ?"
    static final String EXCHANGE_NAME = "testExchange"
    static final String ROUTING_KEY = "testRoutingKey"
    static final Map PAYLOAD = [:]
    static final JobKey JOB_KEY = new JobKey(JOB_NAME, JOB_GROUP)
    static final TriggerKey TRIGGER_KEY = new TriggerKey(JOB_NAME, JOB_GROUP)

    static final ScheduleRequest SCHEDULE_REQUEST = new ScheduleRequest(
            jobName: JOB_NAME,
            jobGroup: JOB_GROUP,
            cronExpression: CRON_EXPRESSION,
            "responseQueueInfo": [
                    "exchangeName": EXCHANGE_NAME,
                    "routingKey"  : ROUTING_KEY
            ],
            payload: PAYLOAD)

    static final ScheduleResponse SCHEDULE_RESPONSE = new ScheduleResponse(
            jobName: JOB_NAME,
            jobGroup: JOB_GROUP,
            cronExpression: CRON_EXPRESSION,
            jobStatus: "NOT_STARTED",
            responseQueueInfo: [
                    exchangeName: EXCHANGE_NAME,
                    routingKey  : ROUTING_KEY
            ]
    )
}
