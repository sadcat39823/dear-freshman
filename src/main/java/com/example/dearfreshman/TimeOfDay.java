package com.example.dearfreshman;

public enum TimeOfDay {

    MORNING("Morning", "6:00 AM – 10:59 AM"),
    DAY("Day", "11:00 AM – 1:59 PM"),
    AFTERNOON("Afternoon", "2:00 PM – 5:59 PM"),
    NIGHT("Night", "6:00 PM – 5:59 AM");

    private final String displayName;
    private final String timeRange;

    TimeOfDay(String displayName, String timeRange) {
        this.displayName = displayName;
        this.timeRange = timeRange;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTimeRange() {
        return timeRange;
    }
}
