package com.tangelo.test.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;

@AllArgsConstructor
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
