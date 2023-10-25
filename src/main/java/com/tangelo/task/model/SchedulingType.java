package com.tangelo.task.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
@Getter
public enum SchedulingType {
    IMMEDIATELY(Duration.ZERO),
    FIVE_SECONDS(Duration.ofSeconds(5)), //ONLY FOR TEST
    ONE_HOUR(Duration.ofHours(1)),
    TWO_HOURS(Duration.ofHours(2)),
    SIX_HOURS(Duration.ofHours(6)),
    TWELVE_HOURS(Duration.ofHours(12));

    private final Duration duration;
}
