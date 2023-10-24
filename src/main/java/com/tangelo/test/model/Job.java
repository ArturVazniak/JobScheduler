package com.tangelo.test.model;

import lombok.Data;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

@Data
public class Job {
    private final String id = UUID.randomUUID().toString();

    private final SchedulingType schedulingType;
    private final Runnable jobBody;

    private ScheduledFuture<?> scheduledJob;
}
