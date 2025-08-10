package com.company.scheduler.controller;

import com.company.scheduler.model.dto.request.JobActionRequest;
import com.company.scheduler.model.dto.request.ScheduleRequest;
import com.company.scheduler.model.dto.response.ScheduleResponse;
import com.company.scheduler.service.ScheduleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/schedule-jobs")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getAllJobs() {
        return ResponseEntity.ok(scheduleService.getAllJobList());
    }

    @GetMapping("/running")
    public ResponseEntity<List<ScheduleResponse>> getRunningJobs() {
        return ResponseEntity.ok(scheduleService.getRunningJobList());
    }

    @PostMapping
    public ResponseEntity<Void> createOrUpdateJob(@Valid @RequestBody ScheduleRequest scheduleRequest) {
        scheduleService.saveOrUpdate(scheduleRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/action")
    public ResponseEntity<Void> handleJobAction(@Valid @RequestBody JobActionRequest request) {
        scheduleService.executeJobAction(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteJob(@RequestParam @NotBlank String jobName,
                                          @RequestParam @NotBlank String jobGroup) {
        scheduleService.deleteJob(jobName, jobGroup);
        return ResponseEntity.ok().build();
    }
}
