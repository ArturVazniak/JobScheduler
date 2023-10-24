package com.tangelo.test.service;

import com.tangelo.test.model.Job;

public interface JobScheduler {
    Job scheduleJob(Job job);
    void cancelJob(Job job);

    long getNumberOfRunningJobs();

    long getScheduledJobsNumber();
}
