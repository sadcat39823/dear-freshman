package com.example.dearfreshman;

public class ChoiceEvent extends StoryEvent {

    private final String prompt;
    private final String[] choices;
    private final String[] outcomes;
    private final int[][] statChanges;
    private final int[][] minRequirements; // {minEnergy, minFriendship, minReputation, minAcademics}

    public ChoiceEvent(
            String title,
            String description,
            String speaker,
            String prompt,
            String[] choices,
            String[] outcomes,
            int[][] statChanges,
            int[][] minRequirements,
            String backgroundPath,
            String characterPath, String s) {

        super(
                title,
                description,
                speaker,
                backgroundPath,
                characterPath
        );

        if (choices == null || choices.length != 2
                || outcomes == null || outcomes.length != 2
                || statChanges == null || statChanges.length != 2) {

            throw new IllegalArgumentException(
                    "A choice event requires two choices, "
                            + "two outcomes, and two stat-change arrays.");
        }

        for (int[] changes : statChanges) {
            if (changes == null || changes.length != 5) {
                throw new IllegalArgumentException(
                        "Each choice must contain five stat changes.");
            }
        }

        this.prompt = prompt;
        this.choices = choices.clone();
        this.outcomes = outcomes.clone();

        this.statChanges = new int[][]{
                statChanges[0].clone(),
                statChanges[1].clone()
        };

        if (minRequirements != null && minRequirements.length == 2) {
            this.minRequirements = new int[][]{
                    minRequirements[0] != null ? minRequirements[0].clone() : new int[4],
                    minRequirements[1] != null ? minRequirements[1].clone() : new int[4]
            };
        } else {
            this.minRequirements = new int[][]{new int[4], new int[4]};
        }
    }

    // Overloaded constructor for choices without strict entry requirements
    public ChoiceEvent(
            String title,
            String description,
            String speaker,
            String prompt,
            String[] choices,
            String[] outcomes,
            int[][] statChanges,
            String backgroundPath,
            String characterPath, String s) {
        this(title, description, speaker, prompt, choices, outcomes, statChanges, null, backgroundPath, characterPath, s);
    }

    public boolean canPlayerSelect(Player player, int choiceIndex) {
        if (player == null) return false;
        if (choiceIndex < 0 || choiceIndex >= minRequirements.length) return true;
        return player.hasEnoughStats(minRequirements[choiceIndex]);
    }

    public int[] getRequirements(int choiceIndex) {
        if (choiceIndex < 0 || choiceIndex >= minRequirements.length) return new int[4];
        return minRequirements[choiceIndex];
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
                    "Start a new game before making a choice.");
        }

        if (choiceIndex < 0 || choiceIndex >= choices.length) {
            throw new IllegalArgumentException(
                    "Invalid choice index.");
        }

        if (!canPlayerSelect(player, choiceIndex)) {
            throw new IllegalStateException("You do not meet the stat requirements for this choice.");
        }

        int[] changes = statChanges[choiceIndex];

        player.applyChanges(
                changes[0], // Energy (-5 energy, etc.)
                changes[1], // Friendship (-5 friendship, etc.)
                changes[2], // Reputation (+reputation, etc.)
                changes[3], // Academics (+academics, etc.)
                changes[4]  // Suspicion
        );

        if (changes[0] > 0) {
            player.restoreEnergy(changes[0]);
        }

        return outcomes[choiceIndex];
    }

    @Override
    public boolean isChoiceEvent() {
        return true;
    }
}