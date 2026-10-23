package com.example.dearfreshman;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeManager {

    public enum Period {
        MORNING,
        DAY,
        AFTERNOON,
        NIGHT
    }

    private int day = 1;
    private int minutesOfDay = 8 * 60;

    private static final int MINUTES_PER_DAY = 24 * 60;
    private static final int START_MINUTES = 8 * 60;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("hh:mm a");

    public int getDay() {
        return day;
    }

    public int getMinutesOfDay() {
        return minutesOfDay;
    }

    public LocalTime getCurrentTime() {
        return LocalTime.of(
                minutesOfDay / 60,
                minutesOfDay % 60
        );
    }

    public int getTotalElapsedMinutes() {
        return (day - 1) * MINUTES_PER_DAY
                + minutesOfDay - START_MINUTES;
    }

    public Period getPeriod() {
        int hour = minutesOfDay / 60;

        if (hour >= 6 && hour < 12) {
            return Period.MORNING;
        }

        if (hour >= 12 && hour < 15) {
            return Period.DAY;
        }

        if (hour >= 15 && hour < 18) {
            return Period.AFTERNOON;
        }

        return Period.NIGHT;
    }

    public boolean isNight() {
        return getPeriod() == Period.NIGHT;
    }

    public String getPeriodName() {
        switch (getPeriod()) {
            case MORNING:
                return "MORNING";

            case DAY:
                return "DAYTIME";

            case AFTERNOON:
                return "AFTERNOON";

            case NIGHT:
                return "NIGHT";

            default:
                return "UNKNOWN";
        }
    }

    public String getFormattedTime() {
        return getCurrentTime().format(TIME_FORMAT);
    }

    public String getFormattedDay() {
        return "DAY " + day;
    }

    public String getTimeDisplay() {
        return getFormattedDay()
                + "  •  "
                + getFormattedTime()
                + "  •  "
                + getPeriodName();
    }

    public void advanceMinutes(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException(
                    "Time cannot move backward."
            );
        }

        long totalMinutes = (long) minutesOfDay + minutes;

        day += (int) (totalMinutes / MINUTES_PER_DAY);

        minutesOfDay =
                (int) (totalMinutes % MINUTES_PER_DAY);
    }

    public void advanceToNextPeriod() {
        int targetHour;

        switch (getPeriod()) {
            case MORNING:
                targetHour = 12;
                break;

            case DAY:
                targetHour = 15;
                break;

            case AFTERNOON:
                targetHour = 18;
                break;

            case NIGHT:
            default:
                targetHour = 6;
                break;
        }

        int targetMinutes = targetHour * 60;

        if (targetMinutes <= minutesOfDay) {
            targetMinutes += MINUTES_PER_DAY;
        }

        advanceMinutes(targetMinutes - minutesOfDay);
    }

    public void reset() {
        day = 1;
        minutesOfDay = START_MINUTES;
    }
}
