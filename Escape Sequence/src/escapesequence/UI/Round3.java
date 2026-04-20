package escapesequence.UI;

/**
 * @author Akera Griffith & Kaitlyn Morris 
 * Escape Sequence Round3 Interface
 */

import escapesequence.*;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Round3 extends javax.swing.JFrame {

    // ── Card Display Tracking ────────────────────────────────
    private int index = 0;              // current player card slot index
    private int aiIndex = 0;           // current AI card slot index
    private int aiHiddenCardIndex = 0; // slot index of AI's hidden card
    private int aiCardsRevealed = 0;   // how many AI face-up cards have been shown
    private JLabel[] cardSlots;        // player card label array
    private JLabel[] aiCardSlots;      // AI card label array

    // ── Multiplayer Tracking ─────────────────────────────────
    private boolean isMultiplayer = false;      // true if multiplayer mode
    private int currentPlayerIndex = 0;         // whose turn it is
    private ArrayList<Player> allPlayers;       // all players in the game
    private boolean[] playerStayed;             // tracks which players have stayed

    // ── Game Logic ───────────────────────────────────────────
    private GameController gameController;
    private Player player; // current active player

    // ── Single Player Constructor ────────────────────────────
    public Round3(String playerName) {
        initComponents();
        initCardSlots();
        initAICardSlots();
        setSize(1200, 700);
        hideAllCardSlots();
        hideSummaryLabels();
        setupUI();
        setupGame(playerName);
        startRound();
        wireButtons();
    }

    // ── Multiplayer Constructor ──────────────────────────────
    public Round3(GameController gameController) {
        isMultiplayer = true;
        this.gameController = gameController;
        initComponents();
        initCardSlots();
        initAICardSlots();
        setSize(1200, 700);
        hideAllCardSlots();
        hideSummaryLabels();
        setupUI();

        // Initialize multiplayer state
        allPlayers = gameController.getPlayers();
        playerStayed = new boolean[allPlayers.size()];
        currentPlayerIndex = 0;
        player = allPlayers.get(0);

        startRound();
        wireButtons();
    }

    // ── Setup Methods ────────────────────────────────────────
    // Loads all background images and icons
    private void setupUI() {
        backgroundLabel.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/Round3.png", 1200, 700));
        pauseButton.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/Pause.png", 40, 40));
        deckLabel.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/BackOfCard.jpg", 50, 50));
    }

    // Creates single player game — builds player list and GameController
    private void setupGame(String playerName) {
        player = new Player(playerName);
        ArrayList<Player> players = new ArrayList<>();
        players.add(player);
        gameController = new GameController(players);
        playerLabel.setText(player.getName());
    }

    // Wires hit and stay buttons to their handlers
    private void wireButtons() {
        hitButton.addActionListener(e -> onHit());
        stayButton.addActionListener(e -> onStay());
    }

    // Hides all card slots at the start before any cards are dealt
    private void hideAllCardSlots() {
        for (JLabel slot : cardSlots) {
            slot.setVisible(false);
        }
        for (JLabel slot : aiCardSlots) {
            slot.setVisible(false);
        }
    }

    // Hides all summary labels — shown only in multiplayer
    private void hideSummaryLabels() {
        for (JLabel l : new JLabel[]{p1Name, p2Name, p3Name, p4Name, p5Name, p6Name}) {
            l.setVisible(false);
        }
        for (JTextField t : new JTextField[]{p1Total, p2Total, p3Total, p4Total, p5Total, p6Total}) {
            t.setVisible(false);
        }
    }

    // ── Round Flow ───────────────────────────────────────────
    // Starts a new round — deals opening cards and triggers AI turn
    private void startRound() {
        resetCardDisplay();
        gameController.startRound();

        // In multiplayer, update current player reference
        player = isMultiplayer ? allPlayers.get(currentPlayerIndex) : player;
        playerLabel.setText(player.getName());

        // Display player's opening card(s)
        for (int val : gameController.getPlayerOpeningCards(player)) {
            addCardToDisplay(val);
        }

        // Show AI's hidden card face-down
        dealAIOpeningCards();

        // AI plays its full turn now — cards revealed gradually as player hits
        gameController.playAITurn();

        hitButton.setEnabled(gameController.playerCanHit(player));
        updateTotals();
        updateSummaryLabels();
    }

    // ── Player Actions ───────────────────────────────────────
    // Called when player clicks Hit
    private void onHit() {
        gameController.playerHit(player);
        addCardToDisplay(gameController.getLastPlayerCard(player));

        // Disable buttons during timer delay
        hitButton.setEnabled(false);
        stayButton.setEnabled(false);
        updateTotals();

        // After 1 second, reveal one AI card
        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
            ArrayList<Card> aiHand = gameController.getAI().getHand();
            int nextRevealIndex = aiCardsRevealed + 1; // skip index 0 (hidden card)

            if (nextRevealIndex < aiHand.size()) {
                // Reveal next AI card
                addAICardToDisplay(aiHand.get(nextRevealIndex).getValue());
                aiCardsRevealed++;
                updateTotals();
            } else {
                // No more AI cards to reveal — AI has stayed
                GameDialog.showTimed(Round3.this, "The System stayed.", 1500);
            }

            hitButton.setEnabled(gameController.playerCanHit(player));
            stayButton.setEnabled(true);
        });
        timer.setRepeats(false); // fire once only
        timer.start();
    }

    // Called when player clicks Stay
    private void onStay() {
        hitButton.setEnabled(false);
        stayButton.setEnabled(false);

        if (isMultiplayer) {
            playerStayed[currentPlayerIndex] = true;

            // Reveal one AI card when a player stays
            ArrayList<Card> aiHand = gameController.getAI().getHand();
            int nextRevealIndex = aiCardsRevealed + 1;

            if (nextRevealIndex < aiHand.size()) {
                javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
                    addAICardToDisplay(aiHand.get(nextRevealIndex).getValue());
                    aiCardsRevealed++;
                    updateTotals();
                    advanceToNextPlayer();
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                advanceToNextPlayer();
            }
        } else {
            // Single player — reveal all remaining AI cards then resolve
            ArrayList<Card> aiHand = gameController.getAI().getHand();
            int remaining = aiHand.size() - 1 - aiCardsRevealed;

            if (remaining <= 0) {
                revealAndResolve();
            } else {
                // Use array to track index inside timer lambda
                int[] cardIndex = {aiCardsRevealed + 1};
                javax.swing.Timer timer = new javax.swing.Timer(1000, null);
                timer.addActionListener(e -> {
                    if (cardIndex[0] < aiHand.size()) {
                        addAICardToDisplay(aiHand.get(cardIndex[0]).getValue());
                        cardIndex[0]++;
                        aiCardsRevealed++;
                        updateTotals();
                    } else {
                        timer.stop();
                        revealAndResolve();
                    }
                });
                timer.start();
            }
        }
    }

    // ── Multiplayer Turn Rotation ────────────────────────────
    // Finds the next alive player who hasn't stayed, or resolves if all have stayed
    private void advanceToNextPlayer() {
        int next = -1;

        // Search for next eligible player starting after current
        for (int i = 1; i <= allPlayers.size(); i++) {
            int idx = (currentPlayerIndex + i) % allPlayers.size();
            if (!playerStayed[idx] && allPlayers.get(idx).isAlive()) {
                next = idx;
                break;
            }
        }

        if (next == -1) {
            // All alive players have stayed — resolve the round
            revealAndResolve();
        } else {
            // Switch to next player
            currentPlayerIndex = next;
            player = allPlayers.get(currentPlayerIndex);
            playerLabel.setText(player.getName());

            // Clear player cards for next player's turn
            resetPlayerCardDisplay();

            // Show next player's opening cards
            for (int val : gameController.getPlayerOpeningCards(player)) {
                addCardToDisplay(val);
            }

            updateTotals();
            updateSummaryLabels();
            hitButton.setEnabled(gameController.playerCanHit(player));
            stayButton.setEnabled(true);

            // Brief notification of whose turn it is
            GameDialog.showTimed(this, player.getName() + "'s turn!", 1500);
        }
    }

    // ── Outcome Resolution ───────────────────────────────────
    // Reveals AI hidden card, updates total, resolves round and shows outcome
    private void revealAndResolve() {
        revealAIHiddenCard();
        aiTotal.setText(String.valueOf(gameController.getAI().getHandTotal()));
        GameController.RoundOutcome outcome = gameController.resolveOutcome(player);
        gameController.resolveRound();
        showOutcome(outcome);
    }

    // Shows outcome dialog then navigates to Game Over
     private void showOutcome(GameController.RoundOutcome outcome) {
        SwingUtilities.invokeLater(() -> {
            GameDialog.show(this, gameController.getOutcomeMessage(outcome));
            gameController.advanceRound();
            new GameOver (gameController).setVisible(true); 
            dispose();
        });
    }

    // ── Card Slot Initialization ─────────────────────────────
    // Maps player card labels into an ordered array for sequential display
    private void initCardSlots() {
        cardSlots = new JLabel[]{
            p1Card1, p1Card2, p1Card3, p1Card4, p1Card5,
            p1Card6, p1Card7, p1Card8, p1Card9
        };
    }

    // Maps AI card labels into an ordered array for sequential display
    private void initAICardSlots() {
        aiCardSlots = new JLabel[]{
            aiCard1, aiCard2, aiCard3, aiCard4, aiCard5,
            aiCard6, aiCard7, aiCard8, aiCard9
        };
    }

    // ── Card Display Methods ─────────────────────────────────
    // Reveals a player card image at the current index then advances the index
    private void addCardToDisplay(int cardValue) {
        cardSlots[index].setVisible(true);
        cardSlots[index].setIcon(ResourceLoader.loadCardImage(cardValue));
        if (index < 8) {
            index++;
        }
    }

    // Shows AI's first card face-down at slot 0 — revealed at round end
    private void dealAIOpeningCards() {
        aiHiddenCardIndex = 0;
        aiCardSlots[0].setVisible(true);
        aiCardSlots[0].setIcon(ResourceLoader.loadImageScaled("/assets/pictures/BackOfCard.jpg", 50, 50));
        aiIndex = 1; // next AI card goes to slot 1
    }

    // Reveals one AI card at the current AI index then advances the index
    private void addAICardToDisplay(int cardValue) {
        if (aiIndex < 9) {
            aiCardSlots[aiIndex].setVisible(true);
            aiCardSlots[aiIndex].setIcon(ResourceLoader.loadCardImage(cardValue));
            aiIndex++;
        }
    }

    // Flips the AI's hidden card at round end
    private void revealAIHiddenCard() {
        aiCardSlots[aiHiddenCardIndex].setIcon(
                ResourceLoader.loadCardImage(gameController.getAIHiddenCard()));
    }

    // Resets all card slots — clears icons and hides labels
    private void resetCardDisplay() {
        index = 0;
        aiIndex = 0;
        aiCardsRevealed = 0;
        for (JLabel slot : cardSlots) {
            slot.setIcon(null);
            slot.setVisible(false);
        }
        for (JLabel slot : aiCardSlots) {
            slot.setIcon(null);
            slot.setVisible(false);
        }
    }

    // Resets only the player card slots — used when rotating to next player
    private void resetPlayerCardDisplay() {
        index = 0;
        for (JLabel slot : cardSlots) {
            slot.setIcon(null);
            slot.setVisible(false);
        }
    }

    // ── UI Updates ───────────────────────────────────────────
    // Updates player total and AI visible total labels
    private void updateTotals() {
        playerTotal.setText(String.valueOf(player.getHandTotal()));

        // Only sum AI cards that have been revealed (skip hidden card at index 0)
        int visibleAITotal = 0;
        ArrayList<Card> aiHand = gameController.getAI().getHand();
        for (int i = 1; i <= aiCardsRevealed && i < aiHand.size(); i++) {
            visibleAITotal += aiHand.get(i).getValue();
        }
        aiTotal.setText("? + " + visibleAITotal);
        updateSummaryLabels();
    }

    // Updates the side summary labels showing other players' names and totals
    private void updateSummaryLabels() {
        JLabel[] nameLabels = {p1Name, p2Name, p3Name, p4Name, p5Name, p6Name};
        JTextField[] totalFields = {p1Total, p2Total, p3Total, p4Total, p5Total, p6Total};

        // Hide everything in single player
        if (!isMultiplayer) {
            for (JLabel l : nameLabels) {
                l.setVisible(false);
            }
            for (JTextField t : totalFields) {
                t.setVisible(false);
            }
            return;
        }

        // Show all players except the current one
        int labelIndex = 0;
        for (int i = 0; i < allPlayers.size(); i++) {
            if (i == currentPlayerIndex) {
                continue; // skip active player
            }
            Player p = allPlayers.get(i);
            nameLabels[labelIndex].setText(p.getName());
            nameLabels[labelIndex].setVisible(true);

            // Show total, ? if not yet played, DEAD if eliminated
            if (!p.isAlive()) {
                totalFields[labelIndex].setText("DEAD");
            } else if (playerStayed[i]) {
                totalFields[labelIndex].setText(String.valueOf(p.getHandTotal()));
            } else {
                totalFields[labelIndex].setText("?");
            }
            totalFields[labelIndex].setVisible(true);
            labelIndex++;
        }

        // Hide any unused summary slots
        for (int i = labelIndex; i < nameLabels.length; i++) {
            nameLabels[i].setVisible(false);
            totalFields[i].setVisible(false);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        pauseButton = new javax.swing.JButton();
        aiSpecialtyCard1 = new javax.swing.JLabel();
        specialtyCardLabel1 = new javax.swing.JLabel();
        specialtyCardLabel2 = new javax.swing.JLabel();
        playerLabel = new javax.swing.JLabel();
        stayButton = new javax.swing.JButton();
        hitButton = new javax.swing.JButton();
        specialtyCard1Button = new javax.swing.JButton();
        aiCard1 = new javax.swing.JLabel();
        deckLabel = new javax.swing.JLabel();
        aiSpecialtyCard2 = new javax.swing.JLabel();
        p1SpecialtyCard2 = new javax.swing.JLabel();
        playerTotal = new javax.swing.JLabel();
        aiTotal = new javax.swing.JLabel();
        aiCard2 = new javax.swing.JLabel();
        aiCard3 = new javax.swing.JLabel();
        aiCard4 = new javax.swing.JLabel();
        aiCard5 = new javax.swing.JLabel();
        p1Card1 = new javax.swing.JLabel();
        p1Card2 = new javax.swing.JLabel();
        p1Card3 = new javax.swing.JLabel();
        p1Card4 = new javax.swing.JLabel();
        p1Card5 = new javax.swing.JLabel();
        p1Card6 = new javax.swing.JLabel();
        p1Card7 = new javax.swing.JLabel();
        p1Card8 = new javax.swing.JLabel();
        p1Card9 = new javax.swing.JLabel();
        aiCard6 = new javax.swing.JLabel();
        aiCard7 = new javax.swing.JLabel();
        aiCard8 = new javax.swing.JLabel();
        aiCard9 = new javax.swing.JLabel();
        p1Name = new javax.swing.JLabel();
        p6Total = new javax.swing.JTextField();
        p6SpecialtyCard1 = new javax.swing.JLabel();
        p1Total = new javax.swing.JTextField();
        p2Total = new javax.swing.JTextField();
        p3Total = new javax.swing.JTextField();
        p4Total = new javax.swing.JTextField();
        p5Total = new javax.swing.JTextField();
        p2Name = new javax.swing.JLabel();
        p3Name = new javax.swing.JLabel();
        p4Name = new javax.swing.JLabel();
        p5Name = new javax.swing.JLabel();
        p1SpecialtyCard1 = new javax.swing.JLabel();
        p2SpecialtyCard1 = new javax.swing.JLabel();
        p3SpecialtyCard1 = new javax.swing.JLabel();
        p4SpecialtyCard1 = new javax.swing.JLabel();
        p5SpecialtyCard1 = new javax.swing.JLabel();
        p6Name = new javax.swing.JLabel();
        p6SpecialtyCard2 = new javax.swing.JLabel();
        p2SpecialtyCard2 = new javax.swing.JLabel();
        p3SpecialtyCard2 = new javax.swing.JLabel();
        p4SpecialtyCard2 = new javax.swing.JLabel();
        p5SpecialtyCard2 = new javax.swing.JLabel();
        specialtyCard2Button = new javax.swing.JButton();
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pauseButton.setContentAreaFilled(false);
        pauseButton.setBorder(null);
        pauseButton.setBackground(new java.awt.Color(0, 0, 0, 0));
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });
        jPanel1.add(pauseButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 20, 40, 40));
        jPanel1.add(aiSpecialtyCard1, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 130, 50, 50));
        jPanel1.add(specialtyCardLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 460, 50, 50));
        jPanel1.add(specialtyCardLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 460, 50, 50));

        playerLabel.setFont(FontLoader.getVT323(40f));
        playerLabel.setForeground(new java.awt.Color(255, 255, 255));
        jPanel1.add(playerLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 520, 160, 30));

        stayButton.setFont(FontLoader.getVT323(24f));
        stayButton.setBackground(new java.awt.Color(0, 0, 0, 100));
        stayButton.setForeground(new java.awt.Color(255, 255, 255));
        stayButton.setContentAreaFilled(false);
        stayButton.setText("Stay");
        stayButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black));
        jPanel1.add(stayButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 560, 180, 80));

        hitButton.setBackground(new java.awt.Color(0, 0, 0, 100));
        hitButton.setFont(FontLoader.getVT323(24f));
        hitButton.setContentAreaFilled(false);
        hitButton.setForeground(new java.awt.Color(255, 255, 255));
        hitButton.setText("Hit");
        hitButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black));
        jPanel1.add(hitButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 560, 180, 80));

        specialtyCard1Button.setFont(FontLoader.getVT323(14f));
        specialtyCard1Button.setBackground(new java.awt.Color(0, 0, 0, 100));
        specialtyCard1Button.setForeground(new java.awt.Color(255, 255, 255));
        specialtyCard1Button.setContentAreaFilled(false);
        specialtyCard1Button.setText("Use");
        specialtyCard1Button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black));
        jPanel1.add(specialtyCard1Button, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 520, 60, -1));

        aiCard1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(aiCard1, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 20, 50, 50));

        deckLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.add(deckLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 240, 50, 50));
        jPanel1.add(aiSpecialtyCard2, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 180, 50, 50));
        jPanel1.add(p1SpecialtyCard2, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 20, 50, 50));

        playerTotal.setFont(FontLoader.getVT323(40f));
        playerTotal.setForeground(new java.awt.Color(255, 255, 255));
        playerTotal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        playerTotal.setPreferredSize(new java.awt.Dimension(70, 20));
        jPanel1.add(playerTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 510, 40, 40));

        aiTotal.setFont(FontLoader.getVT323(40f));
        aiTotal.setForeground(new java.awt.Color(255, 255, 255));
        aiTotal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        aiTotal.setName(""); // NOI18N
        aiTotal.setPreferredSize(new java.awt.Dimension(70, 20));
        jPanel1.add(aiTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 30, 170, 50));

        aiCard2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(aiCard2, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 20, 50, 50));

        aiCard3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(aiCard3, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 20, 50, 50));

        aiCard4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(aiCard4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 90, 50, 50));

        aiCard5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(aiCard5, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 90, 50, 50));

        p1Card1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(p1Card1, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 320, 50, 50));

        p1Card2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(p1Card2, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 320, 50, 50));

        p1Card3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(p1Card3, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 320, 50, 50));

        p1Card4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(p1Card4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 390, 50, 50));

        p1Card5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(p1Card5, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 390, 50, 50));

        p1Card6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(p1Card6, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 390, 50, 50));

        p1Card7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(p1Card7, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 460, 50, 50));

        p1Card8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(p1Card8, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 460, 50, 50));

        p1Card9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(p1Card9, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 460, 50, 50));

        aiCard6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(aiCard6, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 90, 50, 50));

        aiCard7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(aiCard7, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 160, 50, 50));

        aiCard8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(aiCard8, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 160, 50, 50));

        aiCard9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.add(aiCard9, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 160, 50, 50));

        p1Name.setFont(FontLoader.getVT323(40f));

        p1Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(p1Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 100, 40));

        p6Total.setBackground(new java.awt.Color(0, 0, 0));

        p6Total.setFont(new java.awt.Font("VT323", 1, 40)); // NOI18N

        p6Total.setForeground(new java.awt.Color(255, 255, 255));
        p6Total.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                p6TotalActionPerformed(evt);
            }
        });
        jPanel1.add(p6Total, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 320, 50, 40));

        p6SpecialtyCard1.setFont(FontLoader.getVT323(12f));
        p6SpecialtyCard1.setForeground(new java.awt.Color(0, 0, 0));
        p6SpecialtyCard1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        p6SpecialtyCard1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel1.add(p6SpecialtyCard1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 320, 50, 50));

        p1Total.setBackground(new java.awt.Color(0, 0, 0));

        p1Total.setFont(new java.awt.Font("VT323", 1, 40)); // NOI18N

        p1Total.setForeground(new java.awt.Color(255, 255, 255));
        p1Total.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                p1TotalActionPerformed(evt);
            }
        });
        jPanel1.add(p1Total, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, 50, 40));

        p2Total.setBackground(new java.awt.Color(0, 0, 0));

        p2Total.setFont(new java.awt.Font("VT323", 1, 40)); // NOI18N

        p2Total.setForeground(new java.awt.Color(255, 255, 255));
        p2Total.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                p2TotalActionPerformed(evt);
            }
        });
        jPanel1.add(p2Total, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 80, 50, 40));

        p3Total.setBackground(new java.awt.Color(0, 0, 0));

        p3Total.setFont(new java.awt.Font("VT323", 1, 40)); // NOI18N

        p3Total.setForeground(new java.awt.Color(255, 255, 255));
        p3Total.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                p3TotalActionPerformed(evt);
            }
        });
        jPanel1.add(p3Total, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 140, 50, 40));

        p4Total.setBackground(new java.awt.Color(0, 0, 0));

        p4Total.setFont(new java.awt.Font("VT323", 1, 40)); // NOI18N

        p4Total.setForeground(new java.awt.Color(255, 255, 255));
        p4Total.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                p4TotalActionPerformed(evt);
            }
        });
        jPanel1.add(p4Total, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 200, 50, 40));

        p5Total.setBackground(new java.awt.Color(0, 0, 0));

        p5Total.setFont(new java.awt.Font("VT323", 1, 40)); // NOI18N

        p5Total.setForeground(new java.awt.Color(255, 255, 255));
        p5Total.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                p5TotalActionPerformed(evt);
            }
        });
        jPanel1.add(p5Total, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 260, 50, 40));

        p2Name.setFont(FontLoader.getVT323(40f));

        p2Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(p2Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 100, 40));

        p3Name.setFont(FontLoader.getVT323(40f));

        p3Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(p3Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 100, 40));

        p4Name.setFont(FontLoader.getVT323(40f));

        p4Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(p4Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 100, 40));

        p5Name.setFont(FontLoader.getVT323(40f));

        p5Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(p5Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 260, 100, 40));

        p1SpecialtyCard1.setFont(FontLoader.getVT323(12f));
        p1SpecialtyCard1.setForeground(new java.awt.Color(0, 0, 0));
        p1SpecialtyCard1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        p1SpecialtyCard1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel1.add(p1SpecialtyCard1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 20, 50, 50));

        p2SpecialtyCard1.setFont(FontLoader.getVT323(12f));
        p2SpecialtyCard1.setForeground(new java.awt.Color(0, 0, 0));
        p2SpecialtyCard1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        p2SpecialtyCard1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel1.add(p2SpecialtyCard1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 80, 50, 50));

        p3SpecialtyCard1.setFont(FontLoader.getVT323(12f));
        p3SpecialtyCard1.setForeground(new java.awt.Color(0, 0, 0));
        p3SpecialtyCard1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        p3SpecialtyCard1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel1.add(p3SpecialtyCard1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 140, 50, 50));

        p4SpecialtyCard1.setFont(FontLoader.getVT323(12f));
        p4SpecialtyCard1.setForeground(new java.awt.Color(0, 0, 0));
        p4SpecialtyCard1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        p4SpecialtyCard1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel1.add(p4SpecialtyCard1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 200, 50, 50));

        p5SpecialtyCard1.setFont(FontLoader.getVT323(12f));
        p5SpecialtyCard1.setForeground(new java.awt.Color(0, 0, 0));
        p5SpecialtyCard1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        p5SpecialtyCard1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel1.add(p5SpecialtyCard1, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 260, 50, 50));

        p6Name.setFont(FontLoader.getVT323(40f));

        p6Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(p6Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 320, 100, 40));

        p6SpecialtyCard2.setFont(FontLoader.getVT323(12f));
        p6SpecialtyCard2.setForeground(new java.awt.Color(0, 0, 0));
        p6SpecialtyCard2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        p6SpecialtyCard2.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel1.add(p6SpecialtyCard2, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 310, 50, 50));

        p2SpecialtyCard2.setFont(FontLoader.getVT323(12f));
        p2SpecialtyCard2.setForeground(new java.awt.Color(0, 0, 0));
        p2SpecialtyCard2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        p2SpecialtyCard2.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel1.add(p2SpecialtyCard2, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 70, 50, 50));

        p3SpecialtyCard2.setFont(FontLoader.getVT323(12f));
        p3SpecialtyCard2.setForeground(new java.awt.Color(0, 0, 0));
        p3SpecialtyCard2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        p3SpecialtyCard2.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel1.add(p3SpecialtyCard2, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 140, 50, 50));

        p4SpecialtyCard2.setFont(FontLoader.getVT323(12f));
        p4SpecialtyCard2.setForeground(new java.awt.Color(0, 0, 0));
        p4SpecialtyCard2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        p4SpecialtyCard2.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel1.add(p4SpecialtyCard2, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 200, 50, 50));

        p5SpecialtyCard2.setFont(FontLoader.getVT323(12f));
        p5SpecialtyCard2.setForeground(new java.awt.Color(0, 0, 0));
        p5SpecialtyCard2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        p5SpecialtyCard2.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPanel1.add(p5SpecialtyCard2, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 260, 50, 50));

        specialtyCard2Button.setFont(FontLoader.getVT323(14f));
        specialtyCard2Button.setForeground(new java.awt.Color(255, 255, 255));
        specialtyCard2Button.setText("Use");
        specialtyCard2Button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black));
        jPanel1.add(specialtyCard2Button, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 520, 60, -1));
        jPanel1.add(backgroundLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1200, 710));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Pause
    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
        // TODO add your handling code here:
        PauseScreen pause = new PauseScreen();
        pause.setLocationRelativeTo(this); 
        pause.setVisible(true);
    }//GEN-LAST:event_pauseButtonActionPerformed

    private void p6TotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_p6TotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_p6TotalActionPerformed

    private void p1TotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_p1TotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_p1TotalActionPerformed

    private void p2TotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_p2TotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_p2TotalActionPerformed

    private void p3TotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_p3TotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_p3TotalActionPerformed

    private void p4TotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_p4TotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_p4TotalActionPerformed

    private void p5TotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_p5TotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_p5TotalActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Round3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Round3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Round3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Round3.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ArrayList<escapesequence.Player> players = new ArrayList<>();
                players.add(new escapesequence.Player("Player 1"));
                new Round3(new escapesequence.GameController(players)).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aiCard1;
    private javax.swing.JLabel aiCard2;
    private javax.swing.JLabel aiCard3;
    private javax.swing.JLabel aiCard4;
    private javax.swing.JLabel aiCard5;
    private javax.swing.JLabel aiCard6;
    private javax.swing.JLabel aiCard7;
    private javax.swing.JLabel aiCard8;
    private javax.swing.JLabel aiCard9;
    private javax.swing.JLabel aiSpecialtyCard1;
    private javax.swing.JLabel aiSpecialtyCard2;
    private javax.swing.JLabel aiTotal;
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JLabel deckLabel;
    private javax.swing.JButton hitButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel p1Card1;
    private javax.swing.JLabel p1Card2;
    private javax.swing.JLabel p1Card3;
    private javax.swing.JLabel p1Card4;
    private javax.swing.JLabel p1Card5;
    private javax.swing.JLabel p1Card6;
    private javax.swing.JLabel p1Card7;
    private javax.swing.JLabel p1Card8;
    private javax.swing.JLabel p1Card9;
    private javax.swing.JLabel p1Name;
    private javax.swing.JLabel p1SpecialtyCard1;
    private javax.swing.JLabel p1SpecialtyCard2;
    private javax.swing.JTextField p1Total;
    private javax.swing.JLabel p2Name;
    private javax.swing.JLabel p2SpecialtyCard1;
    private javax.swing.JLabel p2SpecialtyCard2;
    private javax.swing.JTextField p2Total;
    private javax.swing.JLabel p3Name;
    private javax.swing.JLabel p3SpecialtyCard1;
    private javax.swing.JLabel p3SpecialtyCard2;
    private javax.swing.JTextField p3Total;
    private javax.swing.JLabel p4Name;
    private javax.swing.JLabel p4SpecialtyCard1;
    private javax.swing.JLabel p4SpecialtyCard2;
    private javax.swing.JTextField p4Total;
    private javax.swing.JLabel p5Name;
    private javax.swing.JLabel p5SpecialtyCard1;
    private javax.swing.JLabel p5SpecialtyCard2;
    private javax.swing.JTextField p5Total;
    private javax.swing.JLabel p6Name;
    private javax.swing.JLabel p6SpecialtyCard1;
    private javax.swing.JLabel p6SpecialtyCard2;
    private javax.swing.JTextField p6Total;
    private javax.swing.JButton pauseButton;
    private javax.swing.JLabel playerLabel;
    private javax.swing.JLabel playerTotal;
    private javax.swing.JButton specialtyCard1Button;
    private javax.swing.JButton specialtyCard2Button;
    private javax.swing.JLabel specialtyCardLabel1;
    private javax.swing.JLabel specialtyCardLabel2;
    private javax.swing.JButton stayButton;
    // End of variables declaration//GEN-END:variables
}
