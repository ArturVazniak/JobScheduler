package com.tangelo.task.service;

import com.tangelo.task.model.Job;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
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

    private final List<Job> scheduledJobs = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler;

    private int numberOfThreads = Runtime.getRuntime().availableProcessors();

    private final List<Future.State> jobFinishedStates = List.of(
            Future.State.SUCCESS,
            Future.State.CANCELLED
    );

    public JobSchedulerImpl() {
        scheduler = Executors.newScheduledThreadPool(numberOfThreads);
    }

    public JobSchedulerImpl(int threadsNumber) {
        int threadCount = threadsNumber < 1 ? numberOfThreads : threadsNumber;
        numberOfThreads = threadsNumber;
        scheduler = Executors.newScheduledThreadPool(threadCount);
    }

    @Override
    public synchronized Job scheduleJob(Job job) {

        if (scheduledJobs.size() >= numberOfThreads) {
            removeFinishedJob();
            if (scheduledJobs.size() >= numberOfThreads) {
                log.error("Can't register more jobs, job limit reached");
                return job;
            }
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
            if (scheduledJobs.stream().filter(j -> jobFinishedStates.contains(j.getScheduledJob().state())).count() == scheduledJobs.size()) {
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

    private void removeFinishedJob() {
        List<Job> finishedJob = scheduledJobs.stream().filter(j -> j.getScheduledJob().isDone()).toList();

        if (!finishedJob.isEmpty()) {
            scheduledJobs.removeAll(finishedJob);
            finishedJob.forEach(job -> log.info("Deleted finished job {}", job.getId()));
        }
    }

}
