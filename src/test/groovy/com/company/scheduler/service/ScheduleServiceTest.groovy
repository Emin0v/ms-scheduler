package com.company.scheduler.service

import com.company.scheduler.common.ScheduleTestConstant
import com.company.scheduler.exception.DataNotFoundException
import com.company.scheduler.exception.JobOperationException
import com.company.scheduler.exception.model.constant.ErrorMessage
import com.company.scheduler.model.dto.request.JobActionRequest
import com.company.scheduler.model.dto.request.ScheduleRequest
import com.company.scheduler.model.enums.JobActionType
import com.company.scheduler.service.ScheduleService
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.quartz.*
import org.quartz.impl.matchers.GroupMatcher
import spock.lang.Specification
import spock.lang.Subject

import java.text.MessageFormat

import static com.company.scheduler.common.ScheduleTestConstant.JOB_GROUP
import static com.company.scheduler.common.ScheduleTestConstant.JOB_NAME

class ScheduleServiceTest extends Specification {

    @Subject
    ScheduleService scheduleService

    Scheduler scheduler = Mock()
    ObjectMapper objectMapper = new ObjectMapper()

    def setup() {
        scheduleService = new ScheduleService(scheduler, objectMapper)
    }

    def "getAllJobList should return a list of schedule responses"() {
        given:
        def trigger = Mock(CronTrigger)
        def jobDetail = Mock(JobDetail)

        scheduler.getJobKeys(GroupMatcher.anyJobGroup()) >> ([ScheduleTestConstant.JOB_KEY] as Set)
        scheduler.getTriggersOfJob(ScheduleTestConstant.JOB_KEY) >> [trigger]
        trigger.getKey() >> ScheduleTestConstant.TRIGGER_KEY
        scheduler.getTriggerState(ScheduleTestConstant.TRIGGER_KEY) >> Trigger.TriggerState.NORMAL
        scheduler.getJobDetail(ScheduleTestConstant.JOB_KEY) >> jobDetail
        jobDetail.getJobDataMap() >> new JobDataMap()
        trigger.getCronExpression() >> ScheduleTestConstant.CRON_EXPRESSION

        when:
        def result = scheduleService.getAllJobList()

        then:
        result.size() == 1
        result[0].jobName == JOB_NAME
        result[0].jobGroup == JOB_GROUP
    }

    def "getAllJobList should throw JobOperationException when SchedulerException occurs"() {
        given:
        scheduler.getJobKeys(GroupMatcher.anyJobGroup()) >> { throw new SchedulerException("some error") }

        when:
        scheduleService.getAllJobList()

        then:
        def e = thrown(JobOperationException)
        e.errorMessage == ErrorMessage.FAILED_TO_GET_ALL_JOBS_MESSAGE
    }

    def "getAllJobList should handle null values in JobDataMap gracefully"() {
        given:
        def trigger = Mock(CronTrigger)
        def jobDetail = Mock(JobDetail)

        scheduler.getJobKeys(GroupMatcher.anyJobGroup()) >> ([ScheduleTestConstant.JOB_KEY] as Set)
        scheduler.getTriggersOfJob(ScheduleTestConstant.JOB_KEY) >> [trigger]
        trigger.getKey() >> ScheduleTestConstant.TRIGGER_KEY
        scheduler.getTriggerState(ScheduleTestConstant.TRIGGER_KEY) >> Trigger.TriggerState.NORMAL
        scheduler.getJobDetail(ScheduleTestConstant.JOB_KEY) >> jobDetail
        jobDetail.getJobDataMap() >> new JobDataMap()
        trigger.getCronExpression() >> ScheduleTestConstant.CRON_EXPRESSION

        when:
        def result = scheduleService.getAllJobList()

        then:
        result.size() == 1
        result[0].responseQueueInfo.exchangeName == null
        result[0].responseQueueInfo.routingKey == null
    }

    def "getRunningJobList should return a list of currently running jobs"() {
        given:
        def jobExecutionContext = Mock(JobExecutionContext)
        def jobDetail = Mock(JobDetail)
        def trigger = Mock(CronTrigger)

        scheduler.getCurrentlyExecutingJobs() >> [jobExecutionContext]
        jobExecutionContext.getJobDetail() >> jobDetail
        jobExecutionContext.getTrigger() >> trigger
        jobDetail.getKey() >> ScheduleTestConstant.JOB_KEY

        trigger.getKey() >> ScheduleTestConstant.TRIGGER_KEY
        scheduler.getTriggerState(ScheduleTestConstant.TRIGGER_KEY) >> Trigger.TriggerState.NORMAL
        scheduler.getJobDetail(ScheduleTestConstant.JOB_KEY) >> jobDetail
        jobDetail.getJobDataMap() >> new JobDataMap()
        trigger.getCronExpression() >> ScheduleTestConstant.CRON_EXPRESSION

        when:
        def result = scheduleService.getRunningJobList()

        then:
        result.size() == 1
        result[0].jobName == JOB_NAME
        result[0].jobGroup == JOB_GROUP
    }

    def "getRunningJobList should return empty list when no jobs are running"() {
        given:
        scheduler.getCurrentlyExecutingJobs() >> []

        when:
        def result = scheduleService.getRunningJobList()

        then:
        result.isEmpty()
    }

    def "getRunningJobList should throw JobOperationException when SchedulerException occurs"() {
        given:
        scheduler.getCurrentlyExecutingJobs() >> { throw new SchedulerException("error") }

        when:
        scheduleService.getRunningJobList()

        then:
        def e = thrown(JobOperationException)
        e.errorMessage == ErrorMessage.FAILED_TO_GET_RUNNING_JOBS_MESSAGE
    }

    def "getRunningJobList should skip jobs that fail during mapping"() {
        given:
        def jobExecutionContext = Mock(JobExecutionContext)
        def jobDetail = Mock(JobDetail)
        def trigger = Mock(Trigger)

        scheduler.getCurrentlyExecutingJobs() >> [jobExecutionContext]
        jobExecutionContext.getJobDetail() >> jobDetail
        jobDetail.getKey() >> ScheduleTestConstant.JOB_KEY

        jobExecutionContext.getTrigger() >> trigger
        trigger.getKey() >> ScheduleTestConstant.TRIGGER_KEY

        scheduler.getTriggerState(ScheduleTestConstant.TRIGGER_KEY) >> { throw new SchedulerException("mapping failed") }

        when:
        def result = scheduleService.getRunningJobList()

        then:
        result.isEmpty()
    }

    def "saveOrUpdate should schedule a new job"() {
        given:
        scheduler.checkExists(new JobKey(JOB_NAME, JOB_GROUP)) >> false

        when:
        scheduleService.saveOrUpdate(ScheduleTestConstant.SCHEDULE_REQUEST)

        then:
        1 * scheduler.scheduleJob(_ as JobDetail, _ as Trigger)
    }

    def "saveOrUpdate should reschedule an existing job"() {
        given:
        scheduler.checkExists(ScheduleTestConstant.JOB_KEY) >> true

        when:
        scheduleService.saveOrUpdate(ScheduleTestConstant.SCHEDULE_REQUEST)

        then:
        1 * scheduler.addJob(_ as JobDetail, true)
        1 * scheduler.rescheduleJob(ScheduleTestConstant.TRIGGER_KEY, _ as Trigger)
    }

    def "saveOrUpdate should throw JobOperationException when SchedulerException occurs"() {
        given:
        scheduler.checkExists(_ as JobKey) >> { throw new SchedulerException("error") }

        when:
        scheduleService.saveOrUpdate(ScheduleTestConstant.SCHEDULE_REQUEST)

        then:
        def e = thrown(JobOperationException)
        def expectedMessage = MessageFormat.format(ErrorMessage.FAILED_TO_SAVE_OR_UPDATE_JOB_MESSAGE, JOB_NAME, JOB_GROUP)
        e.errorMessage == expectedMessage
    }

    def "saveOrUpdate should throw JobOperationException when building job detail fails"() {
        given:
        def request = new ScheduleRequest(
                jobName: "testJob",
                jobGroup: "testGroup",
                payload: Map.of(),
        )
        objectMapper.writeValueAsString(_) >> { throw new JsonProcessingException("Serialization failed") }

        when:
        scheduleService.saveOrUpdate(request)

        then:
        thrown(JobOperationException)
    }

    def "executeJobAction should trigger job for RUN_ONCE"() {
        given:
        def request = new JobActionRequest(JOB_NAME, JOB_GROUP, JobActionType.RUN_ONCE)
        scheduler.checkExists(ScheduleTestConstant.JOB_KEY) >> true

        when:
        scheduleService.executeJobAction(request)

        then:
        1 * scheduler.triggerJob(ScheduleTestConstant.JOB_KEY)
    }

    def "executeJobAction should pause job for PAUSE"() {
        given:
        def request = new JobActionRequest(JOB_NAME, JOB_GROUP, JobActionType.PAUSE)
        scheduler.checkExists(ScheduleTestConstant.JOB_KEY) >> true

        when:
        scheduleService.executeJobAction(request)

        then:
        1 * scheduler.pauseJob(ScheduleTestConstant.JOB_KEY)
    }

    def "executeJobAction should resume job for RESUME"() {
        given:
        def request = new JobActionRequest(JOB_NAME, JOB_GROUP, JobActionType.RESUME)
        scheduler.checkExists(ScheduleTestConstant.JOB_KEY) >> true

        when:
        scheduleService.executeJobAction(request)

        then:
        1 * scheduler.resumeJob(ScheduleTestConstant.JOB_KEY)
    }

    def "executeJobAction should throw JobOperationException when SchedulerException occurs during job existence check"() {
        given:
        def request = new JobActionRequest(JOB_NAME, JOB_GROUP, JobActionType.RUN_ONCE)
        scheduler.checkExists(ScheduleTestConstant.JOB_KEY) >> { throw new SchedulerException("scheduler error") }

        when:
        scheduleService.executeJobAction(request)

        then:
        def e = thrown(JobOperationException)
        e.errorMessage == ErrorMessage.FAILED_TO_CHECK_JOB_EXISTENCE_MESSAGE
    }

    def "executeJobAction should throw JobOperationException when SchedulerException occurs"() {
        given:
        def request = new JobActionRequest(JOB_NAME, JOB_GROUP, JobActionType.RUN_ONCE)
        scheduler.checkExists(ScheduleTestConstant.JOB_KEY) >> true
        scheduler.triggerJob(ScheduleTestConstant.JOB_KEY) >> { throw new SchedulerException("error") }

        when:
        scheduleService.executeJobAction(request)

        then:
        def e = thrown(JobOperationException)
        e.errorMessage == MessageFormat.format(ErrorMessage.JOB_ACTION_FAILED_MESSAGE,
                JobActionType.RUN_ONCE, JOB_NAME, JOB_GROUP)
    }

    def "executeJobAction should throw DataNotFoundException when job does not exist"() {
        given:
        def request = new JobActionRequest(JOB_NAME, JOB_GROUP, JobActionType.RUN_ONCE)
        scheduler.checkExists(ScheduleTestConstant.JOB_KEY) >> false

        when:
        scheduleService.executeJobAction(request)

        then:
        thrown(DataNotFoundException)
    }

    def "deleteJob should delete a job"() {
        given:
        scheduler.checkExists(ScheduleTestConstant.JOB_KEY) >> true

        when:
        scheduleService.deleteJob(JOB_NAME, JOB_GROUP)

        then:
        1 * scheduler.deleteJob(ScheduleTestConstant.JOB_KEY)
    }

    def "executeJobAction should throw IllegalArgumentException when actionType is null"() {
        given:
        def request = new JobActionRequest(JOB_NAME, JOB_GROUP, null)
        scheduler.checkExists(ScheduleTestConstant.JOB_KEY) >> true

        when:
        scheduleService.executeJobAction(request)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == ErrorMessage.UNSUPPORTED_ACTION_TYPE_MESSAGE
    }


    def "deleteJob should throw JobOperationException when SchedulerException occurs"() {
        given:
        scheduler.checkExists(ScheduleTestConstant.JOB_KEY) >> true
        scheduler.deleteJob(ScheduleTestConstant.JOB_KEY) >> { throw new SchedulerException("error") }

        when:
        scheduleService.deleteJob(JOB_NAME, JOB_GROUP)

        then:
        def e = thrown(JobOperationException)
        def expectedMessage = MessageFormat.format(ErrorMessage.FAILED_TO_DELETE_JOB_MESSAGE, JOB_NAME, JOB_GROUP)
        e.errorMessage == expectedMessage
    }
}
