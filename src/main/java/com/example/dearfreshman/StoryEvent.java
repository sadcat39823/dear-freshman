package com.example.dearfreshman;

public abstract class StoryEvent {

    private final String title;
    private final String description;
    private final String speaker;
    private final String backgroundPath;
    private final String characterPath;

    protected StoryEvent(
            String title,
            String description,
            String speaker,
            String backgroundPath,
            String characterPath) {

        this.title = title;
        this.description = description;
        this.speaker = speaker;
        this.backgroundPath = backgroundPath;
        this.characterPath = characterPath;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public String getCharacterPath() {
        return characterPath;
    }

    public abstract String getPrompt();

    public abstract String[] getChoices();

    public abstract String resolveChoice(
            Player player, int choiceIndex);

    public abstract boolean isChoiceEvent();
}

