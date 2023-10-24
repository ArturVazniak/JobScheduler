package com.tangelo.test.service;

import com.tangelo.test.model.Job;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JobSchedulerImpl implements JobScheduler {

    private final List<Job> scheduledJobs = new ArrayList<>();
    private final int numberOfAvailableCores = Runtime.getRuntime().availableProcessors();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(numberOfAvailableCores);

    private final List<Future.State> jobCompleteStates = List.of(
            Future.State.SUCCESS,
            Future.State.CANCELLED
    );

    @Override
    public Job scheduleJob(Job job) {
        if (scheduledJobs.size() >= numberOfAvailableCores) {
            log.error("Can't register more jobs, job limit reached");
            return job;
        }

        ScheduledFuture<?> scheduledJob = switch (job.getSchedulingType()) {
            case IMMEDIATELY ->
                    scheduler.schedule(job.getJobBody(), job.getSchedulingType().getDuration().getSeconds(), TimeUnit.SECONDS);
            default ->
                    scheduler.scheduleAtFixedRate(job.getJobBody(), job.getSchedulingType().getDuration().getSeconds(), job.getSchedulingType().getDuration().getSeconds(), TimeUnit.SECONDS);
        };

        job.setScheduledJob(scheduledJob);
        scheduledJobs.add(job);

        return job;
    }

    @Override
    public void cancelJob(Job job) {
        Optional<Job> jobOption = scheduledJobs.stream().filter(j -> job.getId().equals(j.getId())).findFirst();
        jobOption.map(j -> j.getScheduledJob().cancel(true));
        scheduledJobs.remove(job);
        log.info("Canceled job {}", job.getId());
    }

    public void waitAndShutdown() {
        while (true) {
            if (scheduledJobs.stream().filter(j -> jobCompleteStates.contains(j.getScheduledJob().state())).count() == scheduledJobs.size()) {
                scheduler.shutdown();
                break;
            }
            try {
                Thread.sleep(Duration.ofSeconds(5));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public long getNumberOfRunningJobs() {
        return scheduledJobs.stream().filter(j -> j.getScheduledJob().state() == Future.State.RUNNING).count();
    }

    @Override
    public long getScheduledJobsNumber() {
        return scheduledJobs.size();
    }
}
