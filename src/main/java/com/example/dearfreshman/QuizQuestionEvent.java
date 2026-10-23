package com.example.dearfreshman;

public class QuizQuestionEvent extends StoryEvent {

    private final String prompt;
    private final String[] choices;
    private final int correctIndex;
    private final String explanation;

    public QuizQuestionEvent(
            String title,
            String prompt,
            String[] choices,
            int correctIndex,
            String explanation) {

        super(
                title,
                "Programming 101 • Classroom quiz",
                "PROFESSOR",
                "/images/school/classrooms.jpg",
                "/char/professor/Professor.png"
        );

        if (choices == null || choices.length != 4) {
            throw new IllegalArgumentException(
                    "A quiz question requires four answers.");
        }

        if (correctIndex < 0 || correctIndex >= choices.length) {
            throw new IllegalArgumentException(
                    "The correct answer index is invalid.");
        }

        this.prompt = prompt;
        this.choices = choices.clone();
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }

    @Override
    public String getPrompt() {
        return prompt;
    }

    @Override
    public String[] getChoices() {
        return choices.clone();
    }

    @Override
    public String resolveChoice(
            Player player, int choiceIndex) {

        if (player == null) {
            throw new IllegalStateException(
                    "Start a new game before answering.");
        }

        if (choiceIndex < 0 || choiceIndex >= choices.length) {
            throw new IllegalArgumentException(
                    "Invalid quiz answer.");
        }

        boolean correct = choiceIndex == correctIndex;

        player.recordQuizAnswer(correct);

        if (correct) {
            player.applyChanges(
                    -1, 0, 2, 5, 0);
        } else {
            player.applyChanges(
                    -1, 0, -1, 0, 0);
        }

        String result = correct
                ? "Correct!"
                : "Not quite.";

        return result + "\n\n" + explanation;
    }

    @Override
    public boolean isChoiceEvent() {
        return true;
    }
}

