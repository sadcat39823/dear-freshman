package com.example.dearfreshman;

public class Player {

    private final String name;

    private int energy = 100;
    private int friendship = 0;
    private int reputation = 50;
    private int academics = 25;
    private int suspicion = 0;

    // Dynamic NPC Trust & Suspicion Matrix (Hidden from Stats UI)
    private int ginoTrust = 50;
    private int zTrust = 50;
    private int obetTrust = 50;
    private int williamSuspicion = 0;

    private int programmingScore = 0;
    private int databaseScore = 0;
    private int historyScore = 0;
    private int quizAnswered = 0;

    public Player(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter a name."
            );
        }

        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    public int getEnergy() {
        return energy;
    }

    public int getFriendship() {
        return friendship;
    }

    public int getReputation() {
        return reputation;
    }

    public int getAcademics() {
        return academics;
    }

    public int getSuspicion() {
        return suspicion;
    }

    // NPC Trust Matrix Getters & Adjusters
    public int getGinoTrust() {
        return ginoTrust;
    }

    public int getZTrust() {
        return zTrust;
    }

    public int getObetTrust() {
        return obetTrust;
    }

    public int getWilliamSuspicion() {
        return williamSuspicion;
    }

    public void adjustNpcTrust(int ginoDelta, int zDelta, int obetDelta, int williamDelta) {
        this.ginoTrust = clamp(this.ginoTrust + ginoDelta);
        this.zTrust = clamp(this.zTrust + zDelta);
        this.obetTrust = clamp(this.obetTrust + obetDelta);
        this.williamSuspicion = clamp(this.williamSuspicion + williamDelta);
    }

    public int getProgrammingScore() {
        return programmingScore;
    }

    public int getDatabaseScore() {
        return databaseScore;
    }

    public int getHistoryScore() {
        return historyScore;
    }

    public int getQuizAnswered() {
        return quizAnswered;
    }

    /**
     * Checks if the player meets minimum stat requirements for a gated choice.
     * Array format: {minEnergy, minFriendship, minReputation, minAcademics}
     */
    public boolean hasEnoughStats(int[] reqs) {
        if (reqs == null || reqs.length < 4) return true;
        return this.energy >= reqs[0]
                && this.friendship >= reqs[1]
                && this.reputation >= reqs[2]
                && this.academics >= reqs[3];
    }

    public void recordQuizAnswer(boolean correct) {
        if (quizAnswered >= 15) {
            return;
        }

        quizAnswered++;

        if (quizAnswered <= 5) {
            if (correct) {
                programmingScore++;
                applyChanges(0, 0, 0, 3, 0);
                restoreEnergy(5); // Rewards +5 Energy for correct Programming quiz answer
            }
        } else if (quizAnswered <= 10) {
            if (correct) {
                databaseScore++;
                applyChanges(0, 0, 0, 3, 0);
                restoreEnergy(5); // Rewards +5 Energy for correct Database quiz answer
            }
        } else {
            if (correct) {
                historyScore++;
                applyChanges(0, 0, 0, 3, 0);
                restoreEnergy(5); // Rewards +5 Energy for correct Tech History quiz answer
            }
        }
    }

    public void applyChanges(
            int energyChange,
            int friendshipChange,
            int reputationChange,
            int academicsChange,
            int suspicionChange) {

        energy = clamp(energy + energyChange);
        friendship = clamp(friendship + friendshipChange);
        reputation = clamp(reputation + reputationChange);
        academics = clamp(academics + academicsChange);
        suspicion = clamp(suspicion + suspicionChange);
    }

    public void consumeTimeEnergy(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Energy cost cannot be negative."
            );
        }

        energy = clamp(energy - amount);
    }

    public void restoreEnergy(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Energy restoration cannot be negative."
            );
        }

        energy = clamp(energy + amount);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    public void restoreProgress(
            int energy,
            int friendship,
            int reputation,
            int academics,
            int suspicion,
            int programmingScore,
            int quizAnswered) {

        this.energy = clamp(energy);
        this.friendship = clamp(friendship);
        this.reputation = clamp(reputation);
        this.academics = clamp(academics);
        this.suspicion = clamp(suspicion);

        this.programmingScore =
                Math.max(0, Math.min(5, programmingScore));

        this.quizAnswered =
                Math.max(0, Math.min(15, quizAnswered));
    }
}