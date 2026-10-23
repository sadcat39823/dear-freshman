package com.example.dearfreshman;

public class SaveSlot {
    private final int saveId;
    private final String playerName;
    private final int currentChapter;
    private final int currentEventIndex;
    private final int gameDay;
    private final int gameMinutes;
    private final String updatedAt;

    public SaveSlot(int saveId, String playerName, int currentChapter, int currentEventIndex, int gameDay, int gameMinutes, String updatedAt) {
        this.saveId = saveId;
        this.playerName = playerName;
        this.currentChapter = currentChapter;
        this.currentEventIndex = currentEventIndex;
        this.gameDay = gameDay;
        this.gameMinutes = gameMinutes;
        this.updatedAt = updatedAt;
    }

    public int getSaveId() {
        return saveId;
    }

    public String getDisplayLabel() {
        return String.format("Save #%d | %s | Chapter %d (Day %d) - Last Updated: %s",
                saveId, playerName, currentChapter, gameDay, updatedAt);
    }
}