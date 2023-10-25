package com.tangelo.task.model;

import lombok.*;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

@Getter
@RequiredArgsConstructor
public class Job {
    private final String id = UUID.randomUUID().toString();
    private final SchedulingType schedulingType;
    private final Runnable jobBody;
    @Setter
    private ScheduledFuture<?> scheduledJob;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return Objects.equals(id, job.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Future.State getCurrentState(){
        return scheduledJob.state();
    }
}
