package com.example.dearfreshman;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class HelloController {

    @FXML private StackPane root;
    @FXML private StackPane mainMenuPane;
    @FXML private ImageView menuBackground;
    @FXML private BorderPane gamePane;
    @FXML private StackPane storyArea;

    @FXML private Label statsLabel;
    @FXML private Label sceneNumberLabel;
    @FXML private Label sceneTitleLabel;
    @FXML private Label sceneDescriptionLabel;
    @FXML private Label speakerLabel;
    @FXML private Label dialogueLabel;
    @FXML private Label footerLabel;
    @FXML private Label timeLabel;

    @FXML private VBox choicesBox;
    @FXML private ScrollPane dialogueScroll;

    @FXML private ImageView sceneBackground;
    @FXML private ImageView characterImage;
    @FXML private ImageView dialogueSkin;
    @FXML private ImageView nameSkin;
    @FXML private ImageView statusSkin;

    @FXML private Button newGameButton;
    @FXML private Button continueButton;
    @FXML private Button exitButton;
    @FXML private ImageView gameBackground;

    private final GameState gameState = new GameState();
    private final TimeManager timeManager = new TimeManager();

    private boolean showingResult;

    @FXML
    public void initialize() {
        loadImageInto(menuBackground, "/images/main_menu.png");
        loadImageInto(gameBackground, "/images/school/campus.jpg");
        loadImageInto(nameSkin, "/gui/namebox_pink.png");
        loadImageInto(statusSkin, "/gui/statusbox_light_pink.png");

        if (menuBackground != null && root != null) {
            menuBackground.fitWidthProperty().bind(root.widthProperty());
            menuBackground.fitHeightProperty().bind(root.heightProperty());
            menuBackground.setPreserveRatio(false);
        }

        if (gameBackground != null && root != null) {
            gameBackground.fitWidthProperty().bind(root.widthProperty());
            gameBackground.fitHeightProperty().bind(root.heightProperty());
            gameBackground.setPreserveRatio(false);
        }

        if (sceneBackground != null && storyArea != null) {
            sceneBackground.fitWidthProperty().bind(storyArea.widthProperty());
            sceneBackground.fitHeightProperty().bind(storyArea.heightProperty());
            sceneBackground.setPreserveRatio(false);
            sceneBackground.setSmooth(true);
        }

        if (characterImage != null) {
            characterImage.setPreserveRatio(true);
            characterImage.setSmooth(true);
            characterImage.setFitWidth(360);
            characterImage.setFitHeight(440);

            StackPane.setAlignment(characterImage, Pos.BOTTOM_RIGHT);
            characterImage.setTranslateX(-20);
            characterImage.setTranslateY(0);
        }

        configureDialogueLayout();

        updateTimeDisplay();
        showMainMenu();
        updateStats();
    }

    private void configureDialogueLayout() {
        if (dialogueLabel != null) {
            dialogueLabel.setWrapText(true);
            dialogueLabel.setMaxWidth(Double.MAX_VALUE);
        }

        if (dialogueScroll != null) {
            dialogueScroll.setFitToWidth(true);
            dialogueScroll.setFitToHeight(false);
            dialogueScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            dialogueScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            dialogueScroll.setPannable(true);
            dialogueScroll.setVvalue(0);
        }

        if (choicesBox != null) {
            choicesBox.setFillWidth(true);
            VBox.setVgrow(choicesBox, Priority.NEVER);
        }
    }

    private String personalizeText(String text) {
        if (text == null) {
            return "";
        }

        Player player = gameState.getPlayer();

        if (player == null || player.getName() == null || player.getName().isBlank()) {
            return text;
        }

        String playerName = player.getName();

        return text.replace("Y/N", playerName)
                .replace("y/n", playerName)
                .replace("Y/n", playerName)
                .replace("y/N", playerName);
    }

    @FXML
    private void handleNewGame() {
        timeManager.reset();
        showingResult = false;

        mainMenuPane.setVisible(false);
        mainMenuPane.setManaged(false);

        gamePane.setVisible(true);
        gamePane.setManaged(true);

        showWelcome();
    }

    @FXML
    private void handleContinue() {
        try {
            List<SaveSlot> slots = new GameRepository().getAllSaveSlots();

            if (slots.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Save Data");
                alert.setHeaderText("No saved games found.");
                alert.setContentText("Please start a NEW GAME to create a new story.");
                alert.showAndWait();
                return;
            }

            ChoiceDialog<SaveSlot> dialog = new ChoiceDialog<>(slots.get(0), slots);
            dialog.setTitle("Continue Progress");
            dialog.setHeaderText("Select a Save Slot");
            dialog.setContentText("Choose your saved session:");

            dialog.getDialogPane().setPrefWidth(450);

            dialog.showAndWait().ifPresent(selectedSlot -> {
                try {
                    boolean loaded = gameState.loadSaveById(selectedSlot.getSaveId());
                    if (loaded) {
                        mainMenuPane.setVisible(false);
                        mainMenuPane.setManaged(false);

                        gamePane.setVisible(true);
                        gamePane.setManaged(true);

                        displayCurrentEvent();
                        updateStats();
                        updateTimeDisplay();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Progress Loaded");
                        alert.setHeaderText("Welcome back, " + gameState.getPlayer().getName() + "!");
                        alert.setContentText("Successfully restored save slot #" + selectedSlot.getSaveId());
                        alert.showAndWait();
                    }
                } catch (SQLException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Database Error");
                    alert.setHeaderText("Failed to load selected save.");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            });

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Failed to retrieve save slots.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleAdvanceTime() {
        int previousHours = timeManager.getTotalElapsedMinutes() / 60;
        timeManager.advanceMinutes(60);
        int currentHours = timeManager.getTotalElapsedMinutes() / 60;

        int energyCost = currentHours - previousHours;
        Player player = gameState.getPlayer();

        if (player != null && energyCost > 0) {
            player.consumeTimeEnergy(energyCost);
        }

        updateTimeDisplay();
        updateStats();
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    private void updateTimeDisplay() {
        if (timeLabel == null) return;
        timeLabel.setText(timeManager.getTimeDisplay());
    }

    private void advanceStoryTime(int minutes) {
        int previousHours = timeManager.getTotalElapsedMinutes() / 60;
        timeManager.advanceMinutes(minutes);
        int currentHours = timeManager.getTotalElapsedMinutes() / 60;

        int energyCost = currentHours - previousHours;
        Player player = gameState.getPlayer();

        if (player != null && energyCost > 0) {
            player.consumeTimeEnergy(energyCost);
        }

        updateTimeDisplay();
        updateStats();
    }

    private void showWelcome() {
        sceneNumberLabel.setText("CHARACTER SETUP");
        sceneTitleLabel.setText("A Twin's Secret");
        sceneDescriptionLabel.setText("Set up your identity before entering NCST.");
        speakerLabel.setText("YOUR JOURNAL");
        footerLabel.setText("EVERY CHOICE BECOMES A MEMORY");

        dialogueLabel.setText("Welcome to NCST. What should we call you?");

        if (characterImage != null) {
            characterImage.setImage(null);
            characterImage.setVisible(false);
        }

        choicesBox.getChildren().clear();

        TextField nameField = new TextField();
        nameField.setPromptText("Write your name here...");
        nameField.getStyleClass().add("journal-input");
        nameField.setMaxWidth(Double.MAX_VALUE);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-message");

        Button startButton = createButton("START MY STORY   →");

        startButton.setOnAction(event -> {
            String name = nameField.getText();
            if (name == null || name.isBlank()) {
                errorLabel.setText("Please enter a name before continuing.");
                nameField.requestFocus();
                return;
            }

            try {
                gameState.startNewGame(name.trim());

                Alert tutorialAlert = new Alert(Alert.AlertType.CONFIRMATION);
                tutorialAlert.setTitle("Tutorial Option");
                tutorialAlert.setHeaderText("Would you like to play the interactive tutorial?");
                tutorialAlert.setContentText("Learn game mechanics, time management, and stat tracking before starting Chapter 1.");

                ButtonType yesTutorial = new ButtonType("Play Tutorial");
                ButtonType skipTutorial = new ButtonType("Skip to Story", ButtonBar.ButtonData.CANCEL_CLOSE);
                tutorialAlert.getButtonTypes().setAll(yesTutorial, skipTutorial);

                tutorialAlert.showAndWait().ifPresent(response -> {
                    if (response == yesTutorial) {
                        gameState.initTutorial();
                    } else {
                        gameState.completeTutorial();
                    }
                    showingResult = false;
                    updateStats();
                    updateTimeDisplay();
                    displayCurrentEvent();
                });

            } catch (IllegalArgumentException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        nameField.setOnAction(event -> startButton.fire());
        choicesBox.getChildren().addAll(nameField, errorLabel, startButton);
    }

    private void displayCurrentEvent() {
        StoryEvent story = gameState.getCurrentEvent();

        if (story == null) {
            if (gameState.isTutorialActive()) {
                gameState.completeTutorial();
                displayCurrentEvent();
                return;
            }
            showStoryComplete();
            return;
        }

        showingResult = false;
        loadImageInto(sceneBackground, story.getBackgroundPath());

        String characterOrEvidence = null;
        if (story instanceof DialogueEvent) {
            characterOrEvidence = ((DialogueEvent) story).getEvidencePath();
        }
        if (characterOrEvidence == null || characterOrEvidence.isBlank()) {
            characterOrEvidence = story.getCharacterPath();
        }

        if (characterImage != null) {
            if (characterOrEvidence == null || characterOrEvidence.isBlank()) {
                characterImage.setImage(null);
                characterImage.setVisible(false);
            } else {
                characterImage.setImage(null);
                loadImageInto(characterImage, characterOrEvidence);
                characterImage.setVisible(true);
            }
        }

        if (gameState.isTutorialActive()) {
            sceneNumberLabel.setText(
                    String.format("TUTORIAL  /  STEP %02d OF %02d",
                            gameState.getCurrentEventNumber() + 1,
                            gameState.getEventCount())
            );
        } else {
            sceneNumberLabel.setText(
                    String.format("CHAPTER %02d  /  MEMORY %02d OF %02d",
                            gameState.getCurrentChapter(),
                            gameState.getCurrentChapterEventNumber() + 1,
                            gameState.getCurrentChapterEventCount())
            );
        }

        sceneTitleLabel.setText(personalizeText(story.getTitle()));
        sceneDescriptionLabel.setText(personalizeText(story.getDescription()));
        speakerLabel.setText(personalizeText(story.getSpeaker()));

        footerLabel.setText(story.isChoiceEvent() ? "A CHOICE IS WAITING" : "A MOMENT TO REMEMBER");
        dialogueLabel.setText(personalizeText(story.getPrompt()));

        if (dialogueScroll != null) {
            dialogueScroll.setVvalue(0);
        }

        choicesBox.getChildren().clear();
        String[] choices = story.getChoices();

        if (choices.length == 0) {
            Button next = createButton(gameState.isLastEventOfChapter() && !gameState.isTutorialActive() ? "COMPLETE CHAPTER   →" : "CONTINUE   →");
            next.setOnAction(event -> advanceToNextStoryEvent());
            choicesBox.getChildren().add(next);
        } else {
            for (int i = 0; i < choices.length; i++) {
                final int choiceIndex = i;
                Button button = createButton(String.format("%02d   %s", i + 1, personalizeText(choices[i])));

                // Enforce stat gating & display requirements if unmet
                if (story instanceof ChoiceEvent choiceEvent) {
                    if (!choiceEvent.canPlayerSelect(gameState.getPlayer(), choiceIndex)) {
                        int[] reqs = choiceEvent.getRequirements(choiceIndex);
                        StringBuilder reqStr = new StringBuilder(" (Req: ");
                        boolean hasReq = false;
                        if (reqs[0] > 0) { reqStr.append(reqs[0]).append(" Energy "); hasReq = true; }
                        if (reqs[1] > 0) { reqStr.append(reqs[1]).append(" Friendship "); hasReq = true; }
                        if (reqs[2] > 0) { reqStr.append(reqs[2]).append(" Reputation "); hasReq = true; }
                        if (reqs[3] > 0) { reqStr.append(reqs[3]).append(" Academics "); hasReq = true; }
                        reqStr.append(")");

                        if (hasReq) {
                            button.setText(String.format("%02d   %s%s", i + 1, personalizeText(choices[i]), reqStr.toString()));
                        }
                        button.setDisable(true);
                    }
                }

                button.setOnAction(event -> handleChoice(choiceIndex));
                choicesBox.getChildren().add(button);
            }
        }

        updateStats();
        updateTimeDisplay();
    }

    private void handleChoice(int choiceIndex) {
        StoryEvent story = gameState.getCurrentEvent();
        if (story == null) return;

        if (gameState.isTutorialActive()) {
            story.resolveChoice(gameState.getPlayer(), choiceIndex);
            advanceStoryTime(10);
            advanceToNextStoryEvent();
            return;
        }

        if (story instanceof DialogueEvent) {
            story.resolveChoice(gameState.getPlayer(), choiceIndex);
            advanceStoryTime(15);
            advanceToNextStoryEvent();
            return;
        }

        story.resolveChoice(gameState.getPlayer(), choiceIndex);

        if ("Choosing Your Final Path".equals(story.getTitle())) {
            evaluateEndingBranch(choiceIndex);
            advanceStoryTime(30);
            displayCurrentEvent();
            return;
        }

        advanceStoryTime(30);
        showResult(personalizeText(story.resolveChoice(gameState.getPlayer(), choiceIndex)));
    }

    private void evaluateEndingBranch(int choiceIndex) {
        Player player = gameState.getPlayer();

        if (player.getSuspicion() >= 85) {
            gameState.jumpToEvent(gameState.getEventCount() - 2);
        } else if (choiceIndex == 0 && player.getGinoTrust() >= 45) {
            gameState.jumpToEvent(gameState.getEventCount() - 5);
        } else if (player.getAcademics() < 40) {
            gameState.jumpToEvent(gameState.getEventCount() - 4);
        } else {
            gameState.jumpToEvent(gameState.getEventCount() - 3);
        }
    }

    private void showResult(String result) {
        StoryEvent story = gameState.getCurrentEvent();
        if (story == null) return;

        showingResult = true;
        sceneNumberLabel.setText("YOUR JOURNAL  /  REFLECTION");
        sceneTitleLabel.setText("And so, it happened...");
        sceneDescriptionLabel.setText(personalizeText(result));

        choicesBox.getChildren().clear();
        Button next = createButton("CONTINUE TO CONCLUSION   →");
        next.setOnAction(event -> advanceToNextStoryEvent());

        choicesBox.getChildren().add(next);
    }

    private void advanceToNextStoryEvent() {
        if (gameState.isTutorialActive()) {
            if (gameState.getCurrentEventNumber() >= gameState.getEventCount() - 1) {
                gameState.completeTutorial();
                displayCurrentEvent();
                return;
            }
            gameState.advanceEvent();
            displayCurrentEvent();
            return;
        }

        if (gameState.isLastEventOfChapter()) {
            if (gameState.getCurrentChapter() >= 6) {
                showStoryComplete();
            } else {
                showChapterComplete();
            }
            return;
        }

        gameState.advanceEvent();
        if (gameState.getCurrentEvent() == null) {
            showStoryComplete();
            return;
        }
        displayCurrentEvent();
    }

    private void showChapterComplete() {
        int completedChapter = gameState.getCurrentChapter();
        if (completedChapter >= 6) {
            showStoryComplete();
            return;
        }

        sceneNumberLabel.setText(String.format("CHAPTER %02d  /  COMPLETE", completedChapter));
        sceneTitleLabel.setText("Chapter Complete");
        dialogueLabel.setText("Would you like to save your progress and proceed to the next chapter?");

        choicesBox.getChildren().clear();

        Button saveButton = createButton("💾 SAVE PROGRESS");
        saveButton.setOnAction(event -> handleSaveProgress());

        Button proceedButton = createButton("PROCEED TO NEXT CHAPTER   →");
        proceedButton.setOnAction(event -> {
            gameState.advanceEvent();
            displayCurrentEvent();
        });

        choicesBox.getChildren().addAll(saveButton, proceedButton);
    }

    private void handleSaveProgress() {
        try {
            gameState.createDatabaseSave();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Progress Saved");
            alert.setHeaderText("Save Successful!");
            alert.setContentText("Your chapter progress and save slot have been recorded.");
            alert.showAndWait();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Save Failed");
            alert.setHeaderText("Unable to save progress.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void showStoryComplete() {
        sceneNumberLabel.setText("DEAR FRESHMAN  /  STORY COMPLETE");
        sceneTitleLabel.setText("The Journey Concludes");
        dialogueLabel.setText("Thank you for playing Dear Freshman.");

        choicesBox.getChildren().clear();
        Button menuButton = createButton("RETURN TO MAIN MENU");
        menuButton.setOnAction(event -> showMainMenu());
        choicesBox.getChildren().add(menuButton);
    }

    private void showMainMenu() {
        mainMenuPane.setVisible(true);
        mainMenuPane.setManaged(true);
        gamePane.setVisible(false);
        gamePane.setManaged(false);
    }

    private void updateStats() {
        if (statsLabel == null) return;
        Player player = gameState.getPlayer();

        if (player == null) {
            statsLabel.setText("ENERGY 100\nFRIENDSHIP 0\nREPUTATION 50\nACADEMICS 25\nSUSPICION 0");
            return;
        }

        statsLabel.setText(
                "ENERGY  " + player.getEnergy()
                        + "\nFRIENDSHIP  " + player.getFriendship()
                        + "\nREPUTATION  " + player.getReputation()
                        + "\nACADEMICS  " + player.getAcademics()
                        + "\nSUSPICION  " + player.getSuspicion()
                        + "\nPROGRAMMING  " + player.getProgrammingScore() + "/5"
                        + "\nDATABASE  " + player.getDatabaseScore() + "/5"
                        + "\nTECH HISTORY  " + player.getHistoryScore() + "/5"
        );
    }

    private Button createButton(String text) {
        Button button = new Button(personalizeText(text));
        button.getStyleClass().addAll("learn-more", "story-choice");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setWrapText(true);
        return button;
    }

    private void loadImageInto(ImageView view, String path) {
        if (view == null || path == null || path.isBlank()) return;
        var resource = getClass().getResource(path);
        if (resource == null) return;
        Image image = new Image(resource.toExternalForm());
        if (!image.isError()) view.setImage(image);
    }
}