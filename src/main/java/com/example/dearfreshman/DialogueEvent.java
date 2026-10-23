package com.example.dearfreshman;

public class DialogueEvent extends StoryEvent {

    private final String dialogue;
    private final String evidencePath; // Optional evidence image path

    public DialogueEvent(
            String title,
            String description,
            String speaker,
            String dialogue,
            String backgroundPath,
            String characterPath) {
        super(title, description, speaker, backgroundPath, characterPath);
        this.dialogue = dialogue;
        this.evidencePath = null;
    }

    public DialogueEvent(
            String title,
            String description,
            String speaker,
            String dialogue,
            String backgroundPath,
            String characterPath,
            String evidencePath) {
        super(title, description, speaker, backgroundPath, characterPath);
        this.dialogue = dialogue;
        this.evidencePath = evidencePath;
    }

    public String getEvidencePath() {
        return evidencePath;
    }

    @Override
    public String getPrompt() {
        return dialogue;
    }

    @Override
    public String[] getChoices() {
        return new String[]{"Continue   →"};
    }

    @Override
    public String resolveChoice(Player player, int choiceIndex) {
        if (choiceIndex != 0) {
            throw new IllegalArgumentException("Invalid dialogue option.");
        }
        return "You continue your story.";
    }

    @Override
    public boolean isChoiceEvent() {
        return false;
    }
}