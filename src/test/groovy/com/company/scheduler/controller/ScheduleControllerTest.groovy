package com.company.scheduler.controller

import com.company.scheduler.common.ScheduleTestConstant
import com.company.scheduler.service.ScheduleService
import com.company.scheduler.util.MessageSourceUtil
import com.company.scheduler.util.WebUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static com.company.scheduler.common.JsonFiles.CREATE_JOB_REQUEST_JSON
import static com.company.scheduler.common.JsonFiles.GET_ALL_JOBS_RESPONSE_JSON
import static com.company.scheduler.common.JsonFiles.JOB_ACTION_REQUEST_JSON
import static com.company.scheduler.common.ScheduleTestConstant.JOB_GROUP
import static com.company.scheduler.common.ScheduleTestConstant.JOB_NAME
import static com.company.scheduler.common.ScheduleTestConstant.SCHEDULE_RESPONSE
import static com.company.scheduler.common.TestUtil.json
import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest extends Specification {

    private static final String BASE_PATH = "/api/schedule-jobs"

    @Autowired
    private MockMvc mockMvc

    @MockitoBean
    private WebUtil webUtil

    @MockitoBean
    private MessageSourceUtil messageSourceUtil

    @MockitoBean
    private ScheduleService scheduleService

    def "getAllJobs should return all jobs"() {
        given:
        when(scheduleService.getAllJobList()).thenReturn([SCHEDULE_RESPONSE])

        when:
        def result = mockMvc.perform(get(BASE_PATH))

        then:
        result.andExpect(status().isOk())
                .andExpect(content().json(json(GET_ALL_JOBS_RESPONSE_JSON)))
    }

    def "getRunningJobs should return running jobs"() {
        given:
        when(scheduleService.getRunningJobList()).thenReturn([SCHEDULE_RESPONSE])

        when:
        def result = mockMvc.perform(get("${BASE_PATH}/running"))

        then:
        result.andExpect(status().isOk())
                .andExpect(content().json(json(GET_ALL_JOBS_RESPONSE_JSON)))
    }

    def "createOrUpdateJob should create or update a job"() {
        when:
        def result = mockMvc.perform(post(BASE_PATH)
                .contentType("application/json")
                .content(json(CREATE_JOB_REQUEST_JSON)))

        then:
        result.andExpect(status().isOk())
    }

    def "handleJobAction should perform a job action"() {
        when:
        def result = mockMvc.perform(post("${BASE_PATH}/action")
                .contentType("application/json")
                .content(json(JOB_ACTION_REQUEST_JSON)))

        then:
        result.andExpect(status().isOk())
    }

    def "deleteJob should delete a job"() {
        when:
        def result = mockMvc.perform(delete(BASE_PATH)
                .param("jobName", JOB_NAME)
                .param("jobGroup", JOB_GROUP))

        then:
        result.andExpect(status().isOk())
    }
}
