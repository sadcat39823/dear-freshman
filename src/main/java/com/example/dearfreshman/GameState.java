package com.example.dearfreshman;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameState {

    private Player player;

    private final List<StoryEvent> events = new ArrayList<>();
    private final List<StoryEvent> tutorialEvents = new ArrayList<>();
    private boolean isTutorialActive = false;

    private int currentEventIndex = 0;

    private int chapterTwoStartIndex;
    private int chapterThreeStartIndex;
    private int chapterFourStartIndex;
    private int chapterFiveStartIndex;
    private int chapterSixStartIndex;

    private final GameRepository repository = new GameRepository();

    private int currentSaveId = -1;

    public GameState() {
        buildChapterOne();

        chapterTwoStartIndex = events.size();
        buildChapterTwo();

        chapterThreeStartIndex = events.size();
        buildChapterThree();

        chapterFourStartIndex = events.size();
        buildChapterFour();

        chapterFiveStartIndex = events.size();
        buildChapterFive();

        chapterSixStartIndex = events.size();
        buildChapterSix();
    }

    public int getCurrentSaveId() {
        return currentSaveId;
    }

    public void createDatabaseSave() throws SQLException {
        if (player == null) {
            throw new IllegalStateException("Start a new game before creating a save.");
        }
        currentSaveId = repository.createNewSave(player, getCurrentChapter(), currentEventIndex);
    }

    public boolean loadLatestSave() throws SQLException {
        int latestSaveId = repository.getLatestSaveId();
        if (latestSaveId == -1) {
            return false;
        }
        return loadSaveById(latestSaveId);
    }

    public boolean loadSaveById(int saveId) throws SQLException {
        GameRepository.LoadedSaveData loadedData = repository.loadSaveData(saveId);
        if (loadedData == null || loadedData.player() == null) {
            return false;
        }

        this.player = loadedData.player();
        this.currentSaveId = saveId;
        this.currentEventIndex = loadedData.currentEventIndex();
        this.isTutorialActive = false;
        return true;
    }

    public void startNewGame(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Please enter a valid name.");
        }

        player = new Player(playerName.trim());
        currentEventIndex = 0;
        currentSaveId = -1;
    }

    // --- Tutorial Sequence Management ---
    public void initTutorial() {
        isTutorialActive = true;
        tutorialEvents.clear();

        tutorialEvents.add(new DialogueEvent(
                "Tutorial • Welcome", "How to Play Dear Freshman", "YOUR JOURNAL",
                "Welcome to NCST! Before your journey begins, let's review how choices and survival mechanics work.\n\nEvery choice you make consumes time and affects your attributes (Energy, Academics, Reputation, Suspicion).",
                "/images/school/front.jpg", null
        ));

        tutorialEvents.add(new DialogueEvent(
                "Tutorial • Time & Energy", "Managing Your Resources", "YOUR JOURNAL",
                "As time advances, your energy decreases. Keep an eye on your status sidebar.\n\nSome advanced choices will require minimum stats like Academics or Energy to unlock.",
                "/images/school/hallway.jpg", null
        ));

        tutorialEvents.add(new ChoiceEvent(
                "Tutorial • Practice Choice", "Try your first sample choice.", "TUTORIAL GUIDE",
                "How would you like to handle your orientation study materials?",
                new String[]{"Study intensively (-5 Energy, +10 Academics)", "Take a break and rest (+5 Energy, -5 Friendship)"},
                new String[]{"Your academic focus improves.", "You feel refreshed and ready."},
                new int[][]{{-5, 0, 0, 10, 0}, {5, -5, 0, 0, 0}},
                new int[][]{
                        {5, 0, 0, 0}, // Requires at least 5 Energy for Choice A
                        {0, 0, 0, 0}  // No requirements for Choice B
                },
                "/images/school/classrooms.jpg", null, null
        ));
    }

    public boolean isTutorialActive() {
        return isTutorialActive;
    }

    public List<StoryEvent> getTutorialEvents() {
        return tutorialEvents;
    }

    public void completeTutorial() {
        isTutorialActive = false;
        currentEventIndex = 0;
    }

    public Player getPlayer() {
        return player;
    }

    public StoryEvent getCurrentEvent() {
        if (isTutorialActive) {
            if (tutorialEvents.isEmpty() || currentEventIndex < 0 || currentEventIndex >= tutorialEvents.size()) {
                return null;
            }
            return tutorialEvents.get(currentEventIndex);
        }

        if (events.isEmpty() || currentEventIndex < 0 || currentEventIndex >= events.size()) {
            return null;
        }
        return events.get(currentEventIndex);
    }

    public int getCurrentEventNumber() {
        return currentEventIndex;
    }

    public int getEventCount() {
        return isTutorialActive ? tutorialEvents.size() : events.size();
    }

    public boolean isLastEvent() {
        int size = isTutorialActive ? tutorialEvents.size() : events.size();
        return !events.isEmpty() && currentEventIndex == size - 1;
    }

    public void advanceEvent() {
        int size = isTutorialActive ? tutorialEvents.size() : events.size();
        if (currentEventIndex >= 0 && currentEventIndex < size - 1) {
            currentEventIndex++;
        }
    }

    public void jumpToEvent(int index) {
        int size = isTutorialActive ? tutorialEvents.size() : events.size();
        if (index >= 0 && index < size) {
            currentEventIndex = index;
        }
    }

    public int getCurrentChapter() {
        if (isTutorialActive) return 1;
        if (currentEventIndex < chapterTwoStartIndex) return 1;
        if (currentEventIndex < chapterThreeStartIndex) return 2;
        if (currentEventIndex < chapterFourStartIndex) return 3;
        if (currentEventIndex < chapterFiveStartIndex) return 4;
        if (currentEventIndex < chapterSixStartIndex) return 5;
        return 6;
    }

    public int getCurrentChapterEventNumber() {
        if (isTutorialActive) return currentEventIndex;
        return switch (getCurrentChapter()) {
            case 1 -> currentEventIndex;
            case 2 -> currentEventIndex - chapterTwoStartIndex;
            case 3 -> currentEventIndex - chapterThreeStartIndex;
            case 4 -> currentEventIndex - chapterFourStartIndex;
            case 5 -> currentEventIndex - chapterFiveStartIndex;
            default -> currentEventIndex - chapterSixStartIndex;
        };
    }

    public int getCurrentChapterEventCount() {
        if (isTutorialActive) return tutorialEvents.size();
        return switch (getCurrentChapter()) {
            case 1 -> chapterTwoStartIndex;
            case 2 -> chapterThreeStartIndex - chapterTwoStartIndex;
            case 3 -> chapterFourStartIndex - chapterThreeStartIndex;
            case 4 -> chapterFiveStartIndex - chapterFourStartIndex;
            case 5 -> chapterSixStartIndex - chapterFiveStartIndex;
            default -> events.size() - chapterSixStartIndex;
        };
    }

    public boolean isLastEventOfChapter() {
        if (getCurrentEvent() == null) return false;
        int chapterEventNumber = getCurrentChapterEventNumber();
        int chapterEventCount = getCurrentChapterEventCount();
        return chapterEventCount > 0 && chapterEventNumber == chapterEventCount - 1;
    }

    private void buildChapterOne() {
        String front = "/images/school/front.jpg";
        String campus = "/images/school/campus.jpg";
        String hallway = "/images/school/hallway.jpg";
        String classroom = "/images/school/classrooms.jpg";

        events.add(new DialogueEvent(
                "A Twin's Secret", "Chapter 1 • A new beginning", "YOUR JOURNAL",
                "The National College of Science and Technology looks perfect: enormous buildings, spotless halls, and students who seem to know where they belong. You don't.\n\nAfter your grandmother's death, your mother enrolled you here. But this isn't just a new school. Your twin sibling, Luna, has lived in this world without you.",
                front, null
        ));

        events.add(new DialogueEvent(
                "A Familiar Face", "A student mistakes you for someone else.", "RANDOM GIRL",
                "A girl stops in the hallway, eyes wide.\n\n\"Wait... are you Luna?\"\n\nYou explain that you're Luna's twin sibling.",
                hallway, "/char/random girl.png"
        ));

        events.add(new ChoiceEvent(
                "Gino and Z", "A group of students notices the commotion.", "GINO & Z",
                "Z studies you from head to toe while Gino watches quietly. Z says, \"So you're the twin. Luna never mentioned you.\"\n\nHow do you respond?",
                new String[]{"\"Maybe she didn't have a reason to.\" (-5 Energy, +10 Reputation)", "Ignore the comment and ask Gino where class is. (+5 Energy, -5 Friendship)"},
                new String[]{
                        "Your bold reply makes Z curious and boosts your reputation.",
                        "Gino points you toward the classroom, but you lose some social rapport."
                },
                new int[][]{{-5, 0, 10, 0, 2}, {5, -5, 0, 0, 0}},
                new int[][]{
                        {10, 0, 0, 0}, // Requires 10 Energy for choice 0
                        {0, 0, 0, 0}   // No requirement for choice 1
                },
                campus, "/char/gino/Gino.png", "/evidences/paper.png"
        ));

        events.add(new DialogueEvent(
                "Face to Face", "After years apart, your twin finally appears.", "LUNA",
                "\"Y/N?\"\n\nLuna approaches, looking exactly like you and somehow completely different.",
                hallway, "/char/luna/Luna.png"
        ));

        events.add(new ChoiceEvent(
                "A Warning in the Hallway", "Luna's friendliness falters.", "LUNA",
                "\"Why is everyone acting weird?\" you ask.\n\nLuna stops walking. \"Just don't get involved in things that don't concern you.\"\n\nDo you press Luna for an explanation?",
                new String[]{"Ask Luna to tell you the truth. (-5 Energy, +10 Academics)", "Respect Luna's boundary for now. (+5 Energy)"},
                new String[]{"Luna looks frightened rather than annoyed.", "Luna seems relieved, then guilty."},
                new int[][]{{-5, 0, 0, 10, 8}, {5, 2, 1, 0, 2}},
                new int[][]{
                        {5, 0, 0, 0},
                        {0, 0, 0, 0}
                },
                hallway, "/char/luna/Luna.png", "/evidences/paper.png"
        ));

        events.add(new DialogueEvent(
                "First Class: Programming", "Your first lesson begins.", "PROFESSOR",
                "The professor welcomes the class and writes \"Programming Basics\" on the board.",
                classroom, "/char/professor/Professor.png"
        ));

        events.add(new QuizQuestionEvent("Programming Quiz • Question 1", "What is programming?", new String[]{"Writing instructions a computer can execute", "Designing hardware circuits", "Connecting to internet", "Typing text"}, 0, "Programming means writing instructions that a computer can execute."));
        events.add(new QuizQuestionEvent("Programming Quiz • Question 2", "What is an algorithm?", new String[]{"A programming language", "A step-by-step procedure for solving a problem", "Physical memory", "A syntax error"}, 1, "An algorithm is a step-by-step procedure for solving a problem."));
        events.add(new QuizQuestionEvent("Programming Quiz • Question 3", "In programming, what is a variable commonly used for?", new String[]{"To turn off a program", "To decorate UI", "To store a value that can be used by the program", "To eliminate bugs"}, 2, "A variable stores a value that a program can use."));
        events.add(new QuizQuestionEvent("Programming Quiz • Question 4", "What does a loop generally do?", new String[]{"Deletes variables", "Repeats a block of instructions", "Converts code", "Prevents input"}, 1, "A loop repeats a block of instructions."));
        events.add(new QuizQuestionEvent("Programming Quiz • Question 5", "What does debugging mean?", new String[]{"Adding features", "Translating to English", "Running without instructions", "Finding and fixing errors in a program"}, 3, "Debugging means finding and fixing errors in a program."));

        events.add(new DialogueEvent("A Quiet Classmate", "Someone waits until other students leave.", "OBET", "A classmate named Obet catches up with you outside the lab.", "/images/school/lab.jpg", "/char/obet/Obet.png"));
        events.add(new ChoiceEvent("After-Class Conversation", "You have a chance to build a connection.", "OBET", "Obet asks if you want to review the lesson together.", new String[]{"Study together (-5 Energy, +10 Academics)", "Take a break alone (+5 Energy)"}, new String[]{"You review the basics together and boost academics.", "You get a little time to breathe alone and recover energy."}, new int[][]{{-5, 8, 1, 10, 0}, {5, 0, 0, 0, 0}}, hallway, "/char/obet/Obet.png", "/evidences/paper.png"));
        events.add(new DialogueEvent("Z Is Watching", "Someone has noticed your growing curiosity.", "Z", "You catch Z watching from the far end of the corridor.", campus, "/char/z/Z.png"));
        events.add(new DialogueEvent("The Phone Call", "That night, your mother calls.", "MOTHER", "\"There's been an accident. Come home... It's Luna.\"", front, "/char/mother/mother.png"));
        events.add(new DialogueEvent("The Next Morning", "The school no longer feels like a place for new beginnings.", "YOUR JOURNAL", "Luna is gone. Everyone keeps telling you what happened, but nobody answers your questions.", campus, "/char/professor/Professor.png"));
        events.add(new ChoiceEvent("A Promise to Luna", "You decide what kind of investigation begins.", "YOUR JOURNAL", "How will you begin?", new String[]{"Write down every detail (-5 Energy, +10 Academics)", "Confront Z and Gino immediately (-10 Energy)"}, new String[]{"You start a careful timeline.", "You demand answers immediately."}, new int[][]{{-5, 0, 3, 10, 8}, {-10, -2, -5, 0, 18}}, campus, "/char/people.png", "/evidences/paper.png"));
        events.add(new DialogueEvent("Chapter 1: A Twin's Secret", "To be continued...", "YOUR JOURNAL", "Your first day has shaped your path forward.", campus, null));
    }

    private void buildChapterTwo() {
        String campus = "/images/school/campus.jpg";
        String hallway = "/images/school/hallway.jpg";
        String lunaDesk = "/images/luna_place/luna_desk.jpg";
        String investigationDesk = "/images/investigation_desk.png";
        String front = "/images/school/front.jpg";
        String classroom = "/images/school/classrooms.jpg";

        events.add(new DialogueEvent("The Things Luna Left Behind", "Chapter 2 • Three days later", "YOUR JOURNAL", "Three days have passed since Luna's death. At lunch, you sit at Luna's usual desk.", campus, null));
        events.add(new DialogueEvent("Database Management Class", "Your afternoon class puts your tech skills to the test.", "PROFESSOR", "Let's see how well you understand structured data, keys, and queries.", classroom, "/char/professor/Professor.png"));

        events.add(new QuizQuestionEvent("Database Quiz • Question 1", "What does SQL stand for?", new String[]{"Structured Query Language", "Simple Question Logic", "System Quality Link", "Sequential Query Layer"}, 0, "SQL stands for Structured Query Language."));
        events.add(new QuizQuestionEvent("Database Quiz • Question 2", "What is a primary key used for in a database table?", new String[]{"To encrypt the entire database", "To uniquely identify each record in a table", "To connect to an external server", "To sort rows"}, 1, "A primary key uniquely identifies each record in a database table."));
        events.add(new QuizQuestionEvent("Database Quiz • Question 3", "Which SQL command is used to retrieve data from a database?", new String[]{"GET", "PULL", "SELECT", "FETCH"}, 2, "The SELECT statement is used to query and retrieve data from a database."));
        events.add(new QuizQuestionEvent("Database Quiz • Question 4", "What type of database stores data in tables with rows and columns?", new String[]{"Relational database", "Non-relational database", "Hierarchical database", "Distributed cache"}, 0, "A relational database stores data in structured tables."));
        events.add(new QuizQuestionEvent("Database Quiz • Question 5", "What is a foreign key?", new String[]{"A password", "A key that links two tables together", "An unused column", "A cryptographic token"}, 1, "A foreign key links two tables together."));

        events.add(new DialogueEvent("Luna's Usual Seat", "A quiet classmate has something to tell you.", "OBET", "Obet sits across from you and warns that Luna was scared of someone.", lunaDesk, "/char/obet/Obet.png"));
        events.add(new ChoiceEvent("What Did Luna Tell You?", "Obet is reluctant to say more.", "OBET", "How do you respond?", new String[]{"\"Please, tell me.\" (-5 Energy, +10 Academics)", "\"I won't force you.\" (+5 Energy)"}, new String[]{"Obet admits Luna had seen something she wasn't supposed to see.", "Obet promises to tell you later."}, new int[][]{{-5, 0, -1, 10, 5}, {5, 3, 1, 0, 2}}, lunaDesk, "/char/obet/Obet.png", "/evidences/paper.png"));
        events.add(new ChoiceEvent("Gino's Interruption", "Gino arrives before Obet can explain further.", "GINO", "Gino grips Obet's shoulder and tells him to come along.", new String[]{"Stand your ground (-5 Energy, +10 Reputation)", "Let Obet go (+5 Energy)"}, new String[]{"Gino looks away instead of answering.", "You step aside."}, new int[][]{{-5, 0, 10, 0, 10}, {5, 0, 2, 0, 4}}, hallway, "/char/gino/Gino.png", "/evidences/paper.png"));
        events.add(new DialogueEvent("A Room Full of Memories", "Luna's belongings may hold the answers.", "YOUR JOURNAL", "Beneath a stack of notebooks in Luna's room, you discover an old black phone.", lunaDesk, null, "/evidences/luna_phone.png"));
        events.add(new ChoiceEvent("The Hidden Messages", "An unknown number appears in Luna's messages.", "YOUR JOURNAL", "What do you do first?", new String[]{"Save screenshots (-5 Energy, +10 Academics)", "Call mother (+5 Energy)"}, new String[]{"You preserve the threatening messages.", "Your mother grows nervous."}, new int[][]{{-5, 0, 1, 10, 8}, {5, 2, 0, 0, 4}}, investigationDesk, null, "/evidences/paper.png"));
        events.add(new ChoiceEvent("Mother's Warning", "Your mother seems frightened by the messages.", "MOTHER", "How do you answer her demand to hand over the phone?", new String[]{"\"I'm going to find out.\" (-5 Energy, +10 Reputation)", "\"Tell me what you know.\" (-2 Energy)"}, new String[]{"You keep the phone despite her warnings.", "Your mother turns away."}, new int[][]{{-5, 0, 10, 0, 8}, {-2, -1, 0, 0, 6}}, lunaDesk, "/char/mother/mother.png", "/evidences/paper.png"));
        events.add(new DialogueEvent("Z Wants to Talk", "Z approaches you in the hallway.", "Z", "Z leads you into an empty classroom.", hallway, "/char/z/Z.png"));
        events.add(new ChoiceEvent("Z's Secret", "Z knows more than she is willing to say.", "Z", "How do you respond to her warning?", new String[]{"\"Start talking.\" (-5 Energy, +10 Reputation)", "\"I'm listening.\" (+5 Energy)"}, new String[]{"Z refuses to name the person.", "Z says Luna found out something important."}, new int[][]{{-5, -2, 10, 0, 8}, {5, 2, 1, 0, 5}}, hallway, "/char/z/Z.png", "/evidences/paper.png"));
        events.add(new DialogueEvent("Stay Away from This", "Gino interrupts again.", "GINO", "Gino warns you to stay away from this investigation.", hallway, "/char/gino/Gino.png"));
        events.add(new DialogueEvent("The Last Video", "Luna's old phone contains one more clue.", "YOUR JOURNAL", "You find a video of Luna in a dark room. An unidentified person stands in the reflection.", investigationDesk, "/char/luna/Luna.png", "/evidences/luna_phone.png"));
        events.add(new ChoiceEvent("A New Threat", "A message arrives from the unknown number.", "YOUR JOURNAL", "The lights go out and someone knocks at the front door.", new String[]{"Check who is at the door (-5 Energy, +10 Reputation)", "Stay inside (+5 Energy)"}, new String[]{"You approach the door cautiously.", "You keep your distance."}, new int[][]{{-5, 0, 10, 0, 8}, {5, 2, 1, 0, 3}}, front, null, "/evidences/paper.png"));
        events.add(new DialogueEvent("Obet at the Door", "Obet arrives with vital information.", "OBET", "\"The person you're looking for isn't a student,\" Obet reveals.", front, "/char/obet/Obet.png"));
        events.add(new DialogueEvent("Chapter 2: The Things Luna Left Behind", "To be continued...", "YOUR JOURNAL", "You have uncovered threatening messages.", investigationDesk, null));
    }

    private void buildChapterThree() {
        String campus = "/images/school/campus.jpg";
        String hallway = "/images/school/hallway.jpg";
        String front = "/images/school/front.jpg";
        String investigationDesk = "/images/investigation_desk.png";
        String classroom = "/images/school/classrooms.jpg";

        events.add(new DialogueEvent("The Person Behind the Door", "Chapter 3 • An unexpected revelation", "OBET", "Obet gives you a small flash drive he found in Luna's locker.", front, "/char/obet/Obet.png", "/evidences/flashdrive.png"));
        events.add(new DialogueEvent("History of Technology Lecture", "An elective class exploring milestones of the digital age.", "PROFESSOR", "Let's see how well you know your tech history.", classroom, "/char/professor/Professor.png"));

        events.add(new QuizQuestionEvent("Tech History Quiz • Question 1", "Who is considered the first computer programmer?", new String[]{"Ada Lovelace", "Grace Hopper", "Alan Turing", "Margaret Hamilton"}, 0, "Ada Lovelace wrote notes detailing an algorithm."));
        events.add(new QuizQuestionEvent("Tech History Quiz • Question 2", "Which invention replaced vacuum tubes?", new String[]{"The transistor", "The optical fiber", "The floppy disk", "The quantum processor"}, 0, "The transistor replaced bulky vacuum tubes."));
        events.add(new QuizQuestionEvent("Tech History Quiz • Question 3", "In what decade was ARPANET created?", new String[]{"1950s", "1960s", "1970s", "1980s"}, 1, "ARPANET was developed in the late 1960s."));
        events.add(new QuizQuestionEvent("Tech History Quiz • Question 4", "Who invented the World Wide Web in 1989?", new String[]{"Steve Jobs", "Bill Gates", "Tim Berners-Lee", "Vint Cerf"}, 2, "Tim Berners-Lee invented the World Wide Web."));
        events.add(new QuizQuestionEvent("Tech History Quiz • Question 5", "What major hardware component was introduced in 1971 by Intel?", new String[]{"Hard Disk", "Microprocessor (CPU)", "RAM", "Motherboard"}, 1, "Intel released the world's first microprocessor."));

        events.add(new ChoiceEvent("The Flash Drive", "Obet has kept this evidence hidden.", "OBET", "What do you do with the flash drive?", new String[]{"Open the files (-5 Energy, +10 Academics)", "Ask Obet questions (+5 Energy)"}, new String[]{"You connect the flash drive and view its contents.", "Obet explains."}, new int[][]{{-5, 1, 1, 10, 5}, {5, 2, 1, 0, 3}}, investigationDesk, "/char/obet/Obet.png", "/evidences/flashdrive.png"));
        events.add(new DialogueEvent("The Photographs", "Two images reveal a familiar face.", "YOUR JOURNAL", "You find a photo of William Acosta alongside school officials.", investigationDesk, null, "/evidences/flashdrive.png"));
        events.add(new DialogueEvent("The Recording", "Luna's final recording contains a warning.", "LUNA", "The video shows Luna arguing with a man.", investigationDesk, "/char/luna/Luna.png", "/evidences/flashdrive.png"));
        events.add(new ChoiceEvent("Back to NCST", "The recording raises more questions.", "YOUR JOURNAL", "What do you decide to do?", new String[]{"Go to administration (-5 Energy, +10 Reputation)", "Save evidence (+5 Energy)"}, new String[]{"You and Obet investigate the administration building.", "You preserve the recording carefully."}, new int[][]{{-5, 0, 10, -3, 8}, {5, 2, 2, 0, 4}}, front, "/char/obet/Obet.png", "/evidences/paper.png"));
        events.add(new DialogueEvent("NCST — Late at Night", "The familiar campus feels different after dark.", "YOUR JOURNAL", "You and Obet reach the administration office, and Obet pulls out a key.", hallway, "/char/obet/Obet.png"));
        events.add(new ChoiceEvent("A Key to the Office", "Obet has another secret.", "OBET", "How do you react to the key?", new String[]{"Let Obet unlock it (-5 Energy, +10 Reputation)", "Ask why (-2 Energy)"}, new String[]{"Obet unlocks the door.", "Obet hesitates."}, new int[][]{{-5, 1, 10, -2, 5}, {-2, 2, 1, 0, 3}}, hallway, "/char/obet/Obet.png", "/evidences/paper.png"));
        events.add(new DialogueEvent("L. CRUZ", "A folder contains a report.", "YOUR JOURNAL", "You find a folder labeled 'L. CRUZ' noting Luna reported being followed.", investigationDesk, null, "/evidences/confidential_file.png"));
        events.add(new DialogueEvent("Caught in the Office", "Someone is standing in the doorway.", "GINO", "Gino stands in the doorway, telling you to stop investigating.", investigationDesk, "/char/gino/Gino.png", "/evidences/file.png"));
        events.add(new ChoiceEvent("What Did You Know?", "Gino's expression changes.", "GINO", "How do you confront Gino?", new String[]{"\"Why didn't you tell me?\" (-5 Energy, +10 Reputation)", "\"I believe you tried to help.\" (+5 Energy)"}, new String[]{"Gino breaks down.", "Gino admits he tried to help Luna."}, new int[][]{{-5, -2, 10, 0, 10}, {5, 3, 2, 0, 6}}, investigationDesk, "/char/gino/Gino.png", "/evidences/file.png"));
        events.add(new DialogueEvent("Something She Found", "Gino finally reveals the connection.", "GINO", "Gino whispers that Luna was killed because of his father, William.", investigationDesk, "/char/gino/Gino.png"));
        events.add(new DialogueEvent("Footsteps in the Hallway", "The person from the recording has returned.", "YOUR JOURNAL", "You hide as the unidentified man enters the office.", investigationDesk, null));
        events.add(new DialogueEvent("The Man Behind the Door", "You remain hidden.", "YOUR JOURNAL", "You realize someone high up is trying to erase Luna's discovery.", hallway, "/char/gino/Gino.png"));
        events.add(new DialogueEvent("The Next Morning", "A message arrives.", "YOUR JOURNAL / UNKNOWN", "Obet and Gino are missing. An unknown number sends a photo of the balcony.", campus, null));
        events.add(new ChoiceEvent("Come Alone", "The unknown sender has given you an address.", "YOUR JOURNAL", "What do you do?", new String[]{"Save evidence first (+5 Energy, +10 Academics)", "Head out immediately (-5 Energy)"}, new String[]{"You save the evidence before proceeding.", "You head out immediately."}, new int[][]{{5, 2, 2, 10, 5}, {-5, 0, -1, -2, 8}}, campus, null, "/evidences/paper.png"));
        events.add(new DialogueEvent("The Last Place", "Gino receives the same photograph.", "GINO", "Gino sees the photo on his phone and runs after you.", front, "/char/gino/Gino.png"));
        events.add(new DialogueEvent("Chapter 3: The Person Behind the Door", "To be continued...", "YOUR JOURNAL", "The trail leads to the last place Luna visited.", campus, null));
    }

    private void buildChapterFour() {
        String front = "/images/school/front.jpg";
        String hallway = "/images/abandoned_building/building_hallway.jpg";
        String basement = "/images/abandoned_building/basement.jpg";
        String evidenceRoom = "/images/abandoned_building/evidence_room.jpg";
        String buildingExterior = "/images/abandoned_building/building_exterior.jpg";

        events.add(new DialogueEvent("The Abandoned Building", "Chapter 4 • The Last Place Luna Went", "YOUR JOURNAL", "The address leads to an old abandoned building.", buildingExterior, null));
        events.add(new DialogueEvent("Footsteps in the Dark", "Someone is waiting in the shadowed hallway.", "UNIDENTIFIED MAN", "The man from Luna's recording steps out.", hallway, "/char/unidentified_man.png"));
        events.add(new ChoiceEvent("Facing the Mystery Man", "You demand to know what happened.", "UNIDENTIFIED MAN", "How do you confront him?", new String[]{"Press him directly (-5 Energy, +10 Reputation)", "Ask why Luna was targeted (+5 Energy)"}, new String[]{"He warns you about William.", "He admits the plan was only to scare her."}, new int[][]{{-5, 0, 10, 0, 10}, {5, 1, 1, 0, 8}}, hallway, "/char/unidentified_man.png", "/evidences/paper.png"));
        events.add(new DialogueEvent("Trapped Inside", "A sudden crash cuts your conversation short.", "YOUR JOURNAL", "The heavy door slams shut and locks.", hallway, null));
        events.add(new DialogueEvent("The Box with Your Name", "A room deep inside holds an unsettling discovery.", "YOUR JOURNAL", "You find a box with your name containing surveillance photos.", evidenceRoom, null, "/evidences/photos.png"));
        events.add(new ChoiceEvent("The Note and the Warning", "An envelope inside reveals a secret.", "YOUR JOURNAL", "What do you do?", new String[]{"Search carefully (-5 Energy, +10 Academics)", "Think back (+5 Energy)"}, new String[]{"You find a photo.", "Guilt washes over you."}, new int[][]{{-5, 0, 2, 10, 6}, {5, 0, 0, 0, 4}}, evidenceRoom, null, "/evidences/photos.png"));
        events.add(new DialogueEvent("Z Appears", "A familiar student steps out of the darkness.", "Z", "Z arrives, explaining that Luna discovered William's corruption.", evidenceRoom, "/char/z/Z.png"));
        events.add(new ChoiceEvent("The Escape", "Footsteps approach.", "YOUR JOURNAL", "How do you escape?", new String[]{"Follow Z (-5 Energy, +10 Reputation)", "Demand Z explain (-8 Energy)"}, new String[]{"You rush to the basement.", "You scramble out."}, new int[][]{{-5, 3, 10, 0, 5}, {-8, 1, 0, 0, 8}}, basement, "/char/z/Z.png", "/evidences/paper.png"));
        events.add(new DialogueEvent("Gino's Arrival", "A car screeches to a halt beside you.", "GINO", "Gino pulls up and tells you to get in.", "/images/gino_place/gino_car.jpg", "/char/gino/Gino.png"));
        events.add(new DialogueEvent("Chapter 4: The Last Place Luna Went", "To be continued...", "YOUR JOURNAL", "You must figure out who is telling the truth.", buildingExterior, null));
    }

    private void buildChapterFive() {
        String carNight = "/images/gino_place/gino_car_night.jpg";
        String deskNight = "/images/investigation_desk_night.png";
        String schoolBack = "/images/school/school_back.jpg";
        String hallway = "/images/abandoned_building/building_hallway.jpg";
        String acostaResidence = "/images/acosta_residence.png";

        events.add(new DialogueEvent("The Photograph In the Dark", "Chapter 5 • The silent ride", "YOUR JOURNAL", "The car remained silent as Z and Gino drove you back.", carNight, null));
        events.add(new ChoiceEvent("The Next Morning", "The photographs from the building are spread across your desk.", "YOUR JOURNAL", "You notice a single letter 'C' on a photo of Luna and Z.", new String[]{"Call Obet (-5 Energy, +10 Academics)", "Keep studying quietly (+5 Energy)"}, new String[]{"You call Obet.", "You keep the photos close."}, new int[][]{{-5, 0, 0, 10, 6}, {5, 0, 1, 0, 2}}, deskNight, null, "/evidences/photos.png"));
        events.add(new DialogueEvent("Behind the Older Buildings", "Obet looks nervous when you meet him after classes.", "OBET", "Obet hands you a folded note from Luna warning you not to trust anyone.", schoolBack, "/char/obet/Obet.png", "/evidences/paper.png"));
        events.add(new ChoiceEvent("Z's Secret", "You confront Z near the school lockers.", "Z", "Z admits she knew Luna was investigating William.", new String[]{"Accuse Z (-5 Energy, +10 Reputation)", "Accept her warning (+5 Energy)"}, new String[]{"Z walks away.", "You head to the Acosta residence."}, new int[][]{{-5, -1, 10, 0, 8}, {5, 2, 1, 0, 3}}, hallway, "/char/z/Z.png", "/evidences/paper.png"));
        events.add(new DialogueEvent("Gino's House", "Evening settles outside the Acosta residence.", "GINO", "An unknown number texts a photo of Luna and Gino together the night before her death.", acostaResidence, "/char/gino/Gino.png", "/evidences/photos.png"));
        events.add(new DialogueEvent("Chapter 5: The Photograph In the Dark", "To be continued...", "YOUR JOURNAL", "The mystery deepens.", acostaResidence, null));
    }

    private void buildChapterSix() {
        String acostaResidence = "/images/acosta_residence.png";
        String rooftop = "/images/rooftop.png";
        String lunaRoom = "/images/luna_place/luna_room.jpg";
        String auditorium = "/images/school/auditorium.jpg";
        String courtroom = "/images/courtroom.png";
        String darkHallway = "/images/dark_hallway.png";
        String interrogationRoom = "/images/interrogation_room.png";
        String alienAuditorium = "/images/school/auditorium_alien.jpg";

        events.add(new DialogueEvent("The Photograph in Your Hand", "Chapter 6 • The Truth", "YOUR JOURNAL", "The photograph remained in your hand. Someone had been there that night. Inside the Acosta residence, you find Luna's phone.", acostaResidence, "/char/gino/Gino.png", "/evidences/luna_phone.png"));
        events.add(new ChoiceEvent("Luna's Video Message", "Luna's phone screen turns on.", "LUNA", "Luna looks tired: \"Y/N... if you're watching this, you found the photograph. I wasn't alone that night.\"\n\nText message received: 'COME TO NCST. THE ROOFTOP.'", new String[]{"Head straight to rooftop (-5 Energy, +10 Academics)", "Ask Gino (+5 Energy)"}, new String[]{"You rush to the rooftop.", "Gino insists you go."}, new int[][]{{-5, 1, 0, 10, 5}, {5, 0, 0, 0, 8}}, new int[][]{{10, 0, 0, 0}, {0, 0, 0, 0}}, acostaResidence, "/char/luna/Luna.png", "/evidences/luna_phone.png"));
        events.add(new DialogueEvent("Gathering at the Rooftop", "Z and Obet arrive at the rooftop.", "OBET & Z", "Obet hands you an envelope: \"Luna gave this to me.\" William Acosta's name is circled: 'HE ISN'T WORKING ALONE.'", rooftop, "/char/obet/Obet.png", "/evidences/paper.png"));
        events.add(new ChoiceEvent("Gino's Confession", "Gino steps forward, crying.", "GINO", "\"Luna was supposed to meet William that night. I promised to keep you away to protect you.\"", new String[]{"\"You should have trusted me.\" (-5 Energy, +10 Reputation)", "\"I understand.\" (+5 Energy)"}, new String[]{"Gino lowers his head.", "Gino nods softly."}, new int[][]{{-5, -2, 10, 0, 5}, {5, 3, 1, 0, 2}}, rooftop, "/char/gino/Gino.png", "/evidences/photos.png"));
        events.add(new DialogueEvent("Behind the Mirror", "You return to Luna's room.", "YOUR JOURNAL", "Behind a crack in the mirror, a memory card falls. A video plays: \"Someone inside NCST is helping them.\"", lunaRoom, null, "/evidences/memory_card.png"));
        events.add(new DialogueEvent("The Auditorium Secret", "You gather at the auditorium.", "OBET", "Obet uncovers a hidden box under the stage. Luna's recording echoes: \"The person I trusted most helped William.\"", auditorium, "/char/obet/Obet.png", "/evidences/recorder.png"));
        events.add(new ChoiceEvent("William & The Trusted Accomplice", "William Acosta walks inside.", "WILLIAM ACOSTA", "\"Your sister should have stopped searching.\" The trusted accomplice steps out from the shadows.", new String[]{"Hold up evidence (+10 Academics, +5 Reputation)", "Demand to know why (+5 Energy)"}, new String[]{"William's face turns pale.", "The accomplice breaks down."}, new int[][]{{0, 5, 5, 10, -10}, {5, 2, 2, 0, 0}}, auditorium, "/char/trusted_person.png", "/evidences/recorder.png"));
        events.add(new ChoiceEvent("Choosing Your Final Path", "The confrontation reaches its peak.", "YOUR JOURNAL", "William threatens that powerful people will bury you. How do you finish this?", new String[]{"Publish all evidence publicly (Requires 30+ Academics)", "Confront William directly in a reckless standoff"}, new String[]{"You broadcast the truth to the world.", "Your aggressive approach triggers a chaotic struggle."}, new int[][]{{0, 5, 5, 0, -5}, {-5, -3, -5, 0, 20}}, new int[][]{{0, 0, 0, 30}, {0, 0, 0, 0}}, auditorium, "/char/william/william.png", "/evidences/file.png"));

        events.add(new DialogueEvent("Ending 1: The True Ending", "Justice and Remembrance", "YOUR JOURNAL", "Weeks later, the evidence Luna collected is published across all major newspapers. William Acosta and his accomplices are taken to court and convicted.\n\nYou visit Luna's grave, placing her photograph beside the flowers. \"I finally found the truth, Luna. I'll never let anyone forget your story.\"\n\nTHE END (True Ending)", courtroom, "/char/luna/Luna.png", "/evidences/newspaper.png"));
        events.add(new DialogueEvent("Ending 2: A Dangerous Silence", "Compromised Truth", "YOUR JOURNAL", "Without enough academic foundation or public evidence, William's powerful associates easily suppress the story. The dark hallway of NCST remains quiet, and the truth stays buried.\n\nTHE END (Silent Ending)", darkHallway, "/char/william/william.png", "/evidences/file.png"));
        events.add(new DialogueEvent("Ending 3: An Unresolved Mystery", "Lost in the Shadows", "YOUR JOURNAL", "Your reckless confrontation backfired. Suspicion overwhelmed the investigation, and you found yourself questioned in an interrogation room while the real culprits erased all tracks.\n\nTHE END (Unresolved Ending)", interrogationRoom, "/char/unidentified_man.png"));
        events.add(new DialogueEvent("SECRET ENDING: The Extraterrestrial Cover-Up", "Blooper Ending • Operation: Area NCST", "DIRECTOR'S CUT", "Suddenly, the auditorium roof blows wide open with neon-green light! A giant alien spacecraft hovers above.\n\nWilliam pulls out a plasma blaster and shouts: \"Luna didn't discover a school conspiracy—she walked into our intergalactic television set!\"\n\nAn alien tractor beam pulls William away. Gino turns to you: 'Well, that explains the bad Wi-Fi on campus.'\n\nTHE END?! (Alien Blooper Ending)", alienAuditorium, "/char/william/Expressions.william_alien/Angry.png", "/evidences/alien_gun.png"));
        events.add(new DialogueEvent("Credits Roll", "Thank you for playing", "BYEEEEE!!!!", "Made By\nAbuda Marvin\nCkisha Palco\nChaztin Pachejo\nHarvey Paquerra\n\nCharacter Designs by:\nSutemo\nSraye\nIF THEN ELSE - INTERACTIVE FICTION\n© All Rights Reserved 2026", courtroom, null));
    }
}