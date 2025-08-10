package com.company.scheduler.exception.model.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorMessage {

    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "Unexpected internal server error occurs";
    public static final String JOB_NOT_FOUND_MESSAGE = "Job with name - {0} and group - {1} not found.";
    public static final String FAILED_TO_GET_ALL_JOBS_MESSAGE = "Failed to retrieve job list";
    public static final String FAILED_TO_GET_RUNNING_JOBS_MESSAGE = "Failed to retrieve running job list";
    public static final String FAILED_TO_SAVE_OR_UPDATE_JOB_MESSAGE =
            "Failed to save or update job: job with name - {0} and group - {1}";
    public static final String JOB_ACTION_FAILED_MESSAGE =
            "Job action- {0} failed: job with name - {1} and group - {2}";
    public static final String FAILED_TO_DELETE_JOB_MESSAGE =
            "Failed to delete job: job with name - {0} and group - {1}";
    public static final String FAILED_TO_CHECK_JOB_EXISTENCE_MESSAGE = "Error while checking for job existence";
    public static final String FAILED_TO_BUILD_JOB_DETAIL_MESSAGE = "Failed to build job detail";
    public static final String UNSUPPORTED_ACTION_TYPE_MESSAGE = "Unsupported action type";
}
