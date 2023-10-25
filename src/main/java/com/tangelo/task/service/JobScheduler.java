package com.tangelo.task.service;

import com.tangelo.task.model.Job;

public interface JobScheduler {
    Job scheduleJob(Job job);
    void cancelJob(Job job);
    long getNumberOfRunningJobs();

    long getScheduledJobsCount();
}
