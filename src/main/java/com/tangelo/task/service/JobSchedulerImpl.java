package com.tangelo.task.service;

import com.tangelo.task.model.Job;
import com.tangelo.task.model.SchedulingType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JobSchedulerImpl implements JobScheduler {

    private static final int ONE = 1;
    private final List<Job> scheduledJobs = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler;

    private int threadsNumber = Runtime.getRuntime().availableProcessors();

    public JobSchedulerImpl() {
        scheduler = Executors.newScheduledThreadPool(threadsNumber);
    }

    public JobSchedulerImpl(int threadsNumber) {
        this.threadsNumber = threadsNumber < ONE ? this.threadsNumber : threadsNumber;
        this.scheduler = Executors.newScheduledThreadPool(threadsNumber);
    }

    @Override
    public Job scheduleJob(Job job) {

        if (scheduledJobs.size() >= threadsNumber) {
            removeFinishedJob();
        }
        ScheduledFuture<?> scheduledJob = startJob(job);

        job.setScheduledJob(scheduledJob);
        scheduledJobs.add(job);

        return job;
    }

    @Override
    public void cancelJob(Job job) {
        Optional<Job> jobOption = scheduledJobs.stream().filter(job::equals).findFirst();
        jobOption.map(j -> j.getScheduledJob().cancel(true));
        scheduledJobs.remove(job);
        log.info("Canceled job {}", job.getId());
    }

    @Override
    public long getNumberOfRunningJobs() {
        return scheduledJobs.stream().filter(j -> j.getCurrentState() == Future.State.RUNNING).count();
    }

    public long getScheduledJobsCount() {
        return scheduledJobs.size();
    }

    private void removeFinishedJob() {
        List<Job> finishedJob = scheduledJobs.stream().filter(j -> j.getScheduledJob().isDone()).toList();

        if (!finishedJob.isEmpty()) {
            scheduledJobs.removeAll(finishedJob);
            finishedJob.forEach(job -> log.info("Deleted finished job {}", job.getId()));
        }
    }

    private ScheduledFuture<?> startJob(Job job) {
        if (job.getSchedulingType() == SchedulingType.IMMEDIATELY) {
            return scheduler.schedule(job.getJobBody(), job.getSchedulingType().getDuration().getSeconds(), TimeUnit.SECONDS);
        }
        return scheduler.scheduleAtFixedRate(job.getJobBody(), job.getSchedulingType().getDuration().getSeconds(),
                job.getSchedulingType().getDuration().getSeconds(), TimeUnit.SECONDS);
    }

}
