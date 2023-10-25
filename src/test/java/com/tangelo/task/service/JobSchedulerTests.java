package com.tangelo.task.service;

import com.tangelo.task.model.Job;
import com.tangelo.task.model.SchedulingType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class JobSchedulerTests {

    private final static int NUMBER_OF_THREADS = 5;

    private final JobScheduler service = new JobSchedulerImpl(NUMBER_OF_THREADS);


    @Test
    void testJobRegistration() {
        service.scheduleJob(new Job(SchedulingType.FIVE_SECONDS, () -> System.out.println("Something"))); //1
        Assertions.assertEquals(1, service.getScheduledJobsNumber());

        service.scheduleJob(new Job(SchedulingType.FIVE_SECONDS, () -> System.out.println("Something"))); //2
        service.scheduleJob(new Job(SchedulingType.FIVE_SECONDS, () -> System.out.println("Something"))); //3
        service.scheduleJob(new Job(SchedulingType.FIVE_SECONDS, () -> System.out.println("Something"))); //4
        service.scheduleJob(new Job(SchedulingType.FIVE_SECONDS, () -> System.out.println("Something"))); //5
        Assertions.assertEquals(5, service.getScheduledJobsNumber());
    }

    @Test
    @SneakyThrows
    void testRemoveJobIsDane() {
        AtomicInteger counter = new AtomicInteger();
        service.scheduleJob(new Job(SchedulingType.IMMEDIATELY, counter::getAndIncrement)); //1
        service.scheduleJob(new Job(SchedulingType.IMMEDIATELY, counter::getAndIncrement)); //2
        service.scheduleJob(new Job(SchedulingType.IMMEDIATELY, counter::getAndIncrement)); //3
        service.scheduleJob(new Job(SchedulingType.IMMEDIATELY, counter::getAndIncrement)); //4
        service.scheduleJob(new Job(SchedulingType.ONE_HOUR, counter::getAndIncrement)); //5 not deleted

        Thread.sleep(Duration.ofSeconds(5));
        service.scheduleJob(new Job(SchedulingType.IMMEDIATELY, counter::getAndIncrement)); //6
        Assertions.assertEquals(2, service.getScheduledJobsNumber());

        Thread.sleep(Duration.ofSeconds(5));
        Assertions.assertEquals(5, counter.get());
    }

    @Test
    @SneakyThrows
    void testRemoveJob() {
        AtomicInteger counter = new AtomicInteger();
        service.scheduleJob(new Job(SchedulingType.IMMEDIATELY, counter::getAndIncrement)); //1
        service.scheduleJob(new Job(SchedulingType.IMMEDIATELY, counter::getAndIncrement)); //2
        service.scheduleJob(new Job(SchedulingType.IMMEDIATELY, counter::getAndIncrement)); //3
        service.scheduleJob(new Job(SchedulingType.IMMEDIATELY, counter::getAndIncrement)); //4
        service.scheduleJob(new Job(SchedulingType.IMMEDIATELY, counter::getAndIncrement)); //5

        Thread.sleep(Duration.ofSeconds(5));
        service.scheduleJob(new Job(SchedulingType.IMMEDIATELY, counter::getAndIncrement)); //6
        Assertions.assertEquals(1, service.getScheduledJobsNumber());

        Thread.sleep(Duration.ofSeconds(5));
        Assertions.assertEquals(6, counter.get());
    }

    @Test
    @SneakyThrows
    void testJobExecution() {
        AtomicInteger counter = new AtomicInteger();
        service.scheduleJob(new Job(SchedulingType.IMMEDIATELY, counter::getAndIncrement));
        service.scheduleJob(new Job(SchedulingType.IMMEDIATELY, counter::getAndIncrement));
        Thread.sleep(Duration.ofSeconds(5));
        Assertions.assertEquals(2, counter.get());
    }

    @Test
    @SneakyThrows
    void testPeriodicJobExecution() {
        AtomicInteger counter = new AtomicInteger();
        service.scheduleJob(new Job(SchedulingType.FIVE_SECONDS, counter::getAndIncrement));
        Thread.sleep(Duration.ofSeconds(17));
        Assertions.assertEquals(3, counter.get());
    }

    @Test
    @SneakyThrows
    void testPeriodicJobCancelExecution() {
        AtomicInteger counter = new AtomicInteger();
        Job job = new Job(SchedulingType.FIVE_SECONDS, counter::getAndIncrement);
        service.scheduleJob(job);

        Thread.sleep(Duration.ofSeconds(7));
        Assertions.assertEquals(1, counter.get());

        Thread.sleep(Duration.ofSeconds(7));
        Assertions.assertEquals(2, counter.get());

        service.cancelJob(job);
        Thread.sleep(Duration.ofSeconds(10));
        Assertions.assertEquals(2, counter.get());
    }

    @Test
    @SneakyThrows
    void testNumberOfRunningJob() {
        Job job1 = new Job(SchedulingType.FIVE_SECONDS, () -> System.out.println("Something"));
        Job job2 = new Job(SchedulingType.FIVE_SECONDS, () -> System.out.println("Something"));
        Job job3 = new Job(SchedulingType.FIVE_SECONDS, () -> System.out.println("Something"));

        service.scheduleJob(job1);
        service.scheduleJob(job2);
        Assertions.assertEquals(2, service.getNumberOfRunningJobs());

        service.scheduleJob(job3);
        Assertions.assertEquals(3, service.getNumberOfRunningJobs());
    }
}
