package com.company.scheduler.service;

import static com.company.scheduler.exception.model.constant.ErrorMessage.FAILED_TO_BUILD_JOB_DETAIL_MESSAGE;
import static com.company.scheduler.exception.model.constant.ErrorMessage.FAILED_TO_CHECK_JOB_EXISTENCE_MESSAGE;
import static com.company.scheduler.exception.model.constant.ErrorMessage.FAILED_TO_DELETE_JOB_MESSAGE;
import static com.company.scheduler.exception.model.constant.ErrorMessage.FAILED_TO_GET_ALL_JOBS_MESSAGE;
import static com.company.scheduler.exception.model.constant.ErrorMessage.FAILED_TO_GET_RUNNING_JOBS_MESSAGE;
import static com.company.scheduler.exception.model.constant.ErrorMessage.FAILED_TO_SAVE_OR_UPDATE_JOB_MESSAGE;
import static com.company.scheduler.exception.model.constant.ErrorMessage.JOB_ACTION_FAILED_MESSAGE;
import static com.company.scheduler.exception.model.constant.ErrorMessage.JOB_NOT_FOUND_MESSAGE;
import static com.company.scheduler.exception.model.constant.ErrorMessage.UNSUPPORTED_ACTION_TYPE_MESSAGE;
import static com.company.scheduler.model.constant.ApplicationConstants.RabbitMqConstants.EXCHANGE_NAME;
import static com.company.scheduler.model.constant.ApplicationConstants.RabbitMqConstants.PAYLOAD;
import static com.company.scheduler.model.constant.ApplicationConstants.RabbitMqConstants.ROUTING_KEY;
import static org.quartz.JobKey.jobKey;

import com.company.scheduler.exception.DataNotFoundException;
import com.company.scheduler.exception.JobOperationException;
import com.company.scheduler.job.MessagePublisherJob;
import com.company.scheduler.messaging.model.ResponseQueueInfo;
import com.company.scheduler.model.dto.request.JobActionRequest;
import com.company.scheduler.model.dto.request.ScheduleRequest;
import com.company.scheduler.model.dto.response.ScheduleResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final Scheduler scheduler;
    private final ObjectMapper objectMapper;

    public List<ScheduleResponse> getAllJobList() {
        List<ScheduleResponse> jobList = new ArrayList<>();
        try {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    jobList.add(mapToScheduleResponse(jobKey, trigger));
                }
            }
        } catch (SchedulerException e) {
            log.error("Error while getting all jobs", e);
            throw new JobOperationException(FAILED_TO_GET_ALL_JOBS_MESSAGE);
        }
        return jobList;
    }

    public List<ScheduleResponse> getRunningJobList() {
        try {
            return scheduler.getCurrentlyExecutingJobs().stream()
                    .map(context -> {
                        try {
                            return mapToScheduleResponse(context.getJobDetail().getKey(), context.getTrigger());
                        } catch (SchedulerException e) {
                            log.warn("Could not map running job context for jobKey: {}",
                                    context.getJobDetail().getKey(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (SchedulerException e) {
            log.error("Error while getting running jobs", e);
            throw new JobOperationException(FAILED_TO_GET_RUNNING_JOBS_MESSAGE);
        }
    }

    public void saveOrUpdate(ScheduleRequest request) {
        JobKey jobKey = JobKey.jobKey(request.getJobName(), request.getJobGroup());
        try {
            JobDetail jobDetail = buildJobDetail(request);
            Trigger trigger = buildTrigger(request);

            if (scheduler.checkExists(jobKey)) {
                scheduler.addJob(jobDetail, true);
                scheduler.rescheduleJob(trigger.getKey(), trigger);
                log.info("Successfully updated and rescheduled job: {}", jobKey);
            } else {
                scheduler.scheduleJob(jobDetail, trigger);
                log.info("Successfully scheduled new job: {}", jobKey);
            }
        } catch (SchedulerException e) {
            log.error("Failed to save or update job: {}", jobKey, e);
            throw JobOperationException.of(FAILED_TO_SAVE_OR_UPDATE_JOB_MESSAGE, request.getJobName(),
                    request.getJobGroup());
        }
    }

    public void executeJobAction(JobActionRequest request) {
        final var jobName = request.getJobName();
        final var jobGroup = request.getJobGroup();
        final var actionType = request.getActionType();

        JobKey key = jobKey(jobName, jobGroup);
        checkJobExists(key);
        try {
            switch (actionType) {
                case RUN_ONCE -> scheduler.triggerJob(key);
                case PAUSE -> scheduler.pauseJob(key);
                case RESUME -> scheduler.resumeJob(key);
                case null, default -> throw new IllegalArgumentException(UNSUPPORTED_ACTION_TYPE_MESSAGE);
            }
        } catch (SchedulerException e) {
            log.error("Failed to {} job: {}", actionType, key, e);
            throw JobOperationException.of(JOB_ACTION_FAILED_MESSAGE, actionType, jobName, jobGroup);
        }
    }

    public void deleteJob(String jobName, String jobGroup) {
        JobKey key = jobKey(jobName, jobGroup);
        checkJobExists(key);
        try {
            scheduler.deleteJob(key);
        } catch (SchedulerException e) {
            log.error("Failed to delete job: {}", key, e);
            throw JobOperationException.of(FAILED_TO_DELETE_JOB_MESSAGE, jobName, jobGroup);
        }
    }

    // ...::: private methods :::...
    private void checkJobExists(JobKey jobKey) {
        try {
            if (!scheduler.checkExists(jobKey)) {
                throw DataNotFoundException.of(JOB_NOT_FOUND_MESSAGE, jobKey.getName(), jobKey.getGroup());
            }
        } catch (SchedulerException e) {
            log.error("Error while checking for job existence with key: {}", jobKey, e);
            throw new JobOperationException(FAILED_TO_CHECK_JOB_EXISTENCE_MESSAGE);
        }
    }

    private JobDetail buildJobDetail(ScheduleRequest scheduleRequest) {
        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(EXCHANGE_NAME, scheduleRequest.getResponseQueueInfo().getExchangeName());
            jobDataMap.put(ROUTING_KEY, scheduleRequest.getResponseQueueInfo().getRoutingKey());
            jobDataMap.put(PAYLOAD, objectMapper.writeValueAsString(scheduleRequest.getPayload()));

            return JobBuilder.newJob(MessagePublisherJob.class)
                    .withIdentity(scheduleRequest.getJobName(), scheduleRequest.getJobGroup())
                    .usingJobData(jobDataMap)
                    .storeDurably()
                    .build();
        } catch (Exception e) {
            log.error("Failed to build job detail", e);
            throw new JobOperationException(FAILED_TO_BUILD_JOB_DETAIL_MESSAGE);
        }
    }

    private Trigger buildTrigger(ScheduleRequest scheduleRequest) {
        CronScheduleBuilder cronScheduleBuilder =
                CronScheduleBuilder.cronSchedule(scheduleRequest.getCronExpression());

        return TriggerBuilder.newTrigger()
                .withIdentity(scheduleRequest.getJobName(), scheduleRequest.getJobGroup())
                .withSchedule(cronScheduleBuilder)
                .build();
    }

    private ScheduleResponse mapToScheduleResponse(JobKey jobKey, Trigger trigger) throws SchedulerException {
        ScheduleResponse response = new ScheduleResponse();
        response.setJobName(jobKey.getName());
        response.setJobGroup(jobKey.getGroup());
        response.setJobStatus(scheduler.getTriggerState(trigger.getKey()).name());

        if (trigger instanceof CronTrigger cronTrigger) {
            response.setCronExpression(cronTrigger.getCronExpression());
        }

        JobDataMap jobDataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
        response.setResponseQueueInfo(ResponseQueueInfo.from(jobDataMap));
        return response;
    }
}
