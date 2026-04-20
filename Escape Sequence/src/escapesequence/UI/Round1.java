
package escapesequence.UI;

/**
 * @author Akera Griffith & Kaitlyn Morris
 * Escape Sequence Round 1 Interface
 */

import javax.swing.*;
import escapesequence.*;
import java.util.ArrayList;

public class Round1 extends javax.swing.JFrame {

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
    public Round1(String playerName) {
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
    public Round1(GameController gameController) {
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
        backgroundLabel.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/Round1.png", 1200, 700));
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
                GameDialog.showTimed(Round1.this, "The System stayed.", 1500);
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

    // Shows outcome dialog then navigates to Round 2
    private void showOutcome(GameController.RoundOutcome outcome) {
        SwingUtilities.invokeLater(() -> {
            GameDialog.show(this, gameController.getOutcomeMessage(outcome));

            boolean allDead = true;
            for (Player p : gameController.getPlayers()) {
                if (p.isAlive()) {
                    allDead = false;
                    break;
                }
            }

            if (allDead) {
                new GameOver(gameController).setVisible(true);
                dispose();
                return;
            }

            // Single player eliminated
            if (outcome == GameController.RoundOutcome.ELIMINATED && !isMultiplayer) {
                new GameOver(gameController).setVisible(true);
                dispose();
                return;
            }

            gameController.advanceRound();
            new Round2(gameController).setVisible(true); // Round3 for Round2's version
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        deckLabel = new javax.swing.JLabel();
        pauseButton = new javax.swing.JButton();
        aiCard1 = new javax.swing.JLabel();
        playerLabel = new javax.swing.JLabel();
        hitButton = new javax.swing.JButton();
        stayButton = new javax.swing.JButton();
        aiCard6 = new javax.swing.JLabel();
        aiCard7 = new javax.swing.JLabel();
        aiCard9 = new javax.swing.JLabel();
        p1Card1 = new javax.swing.JLabel();
        p1Card4 = new javax.swing.JLabel();
        aiCard8 = new javax.swing.JLabel();
        p1Card5 = new javax.swing.JLabel();
        p1Card6 = new javax.swing.JLabel();
        p1Card7 = new javax.swing.JLabel();
        p1Card8 = new javax.swing.JLabel();
        p1Card3 = new javax.swing.JLabel();
        p1Card2 = new javax.swing.JLabel();
        p1Card9 = new javax.swing.JLabel();
        aiCard5 = new javax.swing.JLabel();
        aiCard4 = new javax.swing.JLabel();
        aiCard3 = new javax.swing.JLabel();
        aiCard2 = new javax.swing.JLabel();
        playerTotal = new javax.swing.JLabel();
        aiTotal = new javax.swing.JLabel();
        p1Name = new javax.swing.JLabel();
        p2Name = new javax.swing.JLabel();
        p3Name = new javax.swing.JLabel();
        p4Name = new javax.swing.JLabel();
        p5Name = new javax.swing.JLabel();
        p6Name = new javax.swing.JLabel();
        p1Total = new javax.swing.JTextField();
        p2Total = new javax.swing.JTextField();
        p3Total = new javax.swing.JTextField();
        p4Total = new javax.swing.JTextField();
        p5Total = new javax.swing.JTextField();
        p6Total = new javax.swing.JTextField();
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(new java.awt.Dimension(590, 300));

        jPanel1.setMaximumSize(new java.awt.Dimension(1200, 700));
        jPanel1.setMinimumSize(new java.awt.Dimension(1200, 700));
        jPanel1.setPreferredSize(new java.awt.Dimension(1200, 700));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        deckLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        deckLabel.setOpaque(true);
        jPanel1.add(deckLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 240, 50, 50));

        pauseButton.setBackground(new java.awt.Color(0, 0, 0, 0));
        pauseButton.setContentAreaFilled(false);
        pauseButton.setBorder(null);
        pauseButton.setFocusPainted(false);
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });
        jPanel1.add(pauseButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1130, 20, 40, 40));

        aiCard1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard1.setOpaque(true);
        jPanel1.add(aiCard1, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 20, 50, 50));

        playerLabel.setFont(FontLoader.getVT323(40f));
        playerLabel.setForeground(new java.awt.Color(255, 255, 255));
        playerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        playerLabel.setPreferredSize(new java.awt.Dimension(70, 20));
        jPanel1.add(playerLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 510, 160, 40));

        hitButton.setBackground(new java.awt.Color(0, 0, 0, 100));
        hitButton.setFont(FontLoader.getVT323(40f));
        hitButton.setForeground(new java.awt.Color(255, 255, 255));
        hitButton.setContentAreaFilled(false);
        hitButton.setText("Hit");
        hitButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black));
        hitButton.setFocusPainted(false);
        jPanel1.add(hitButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 560, 180, 80));

        stayButton.setBackground(new java.awt.Color(0, 0, 0, 100));
        stayButton.setFont(FontLoader.getVT323(40f));
        stayButton.setContentAreaFilled(false);
        stayButton.setForeground(new java.awt.Color(255, 255, 255));
        stayButton.setText("Stay");
        stayButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black));
        stayButton.setFocusPainted(false);
        jPanel1.add(stayButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 560, 180, 80));

        aiCard6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard6.setOpaque(true);
        jPanel1.add(aiCard6, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 90, 50, 50));

        aiCard7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard7.setOpaque(true);
        jPanel1.add(aiCard7, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 160, 50, 50));

        aiCard9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard9.setOpaque(true);
        jPanel1.add(aiCard9, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 160, 50, 50));

        p1Card1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card1.setOpaque(true);
        jPanel1.add(p1Card1, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 320, 50, 50));

        p1Card4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card4.setOpaque(true);
        jPanel1.add(p1Card4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 390, 50, 50));

        aiCard8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard8.setOpaque(true);
        jPanel1.add(aiCard8, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 160, 50, 50));

        p1Card5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card5.setOpaque(true);
        jPanel1.add(p1Card5, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 390, 50, 50));

        p1Card6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card6.setOpaque(true);
        jPanel1.add(p1Card6, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 390, 50, 50));

        p1Card7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card7.setOpaque(true);
        jPanel1.add(p1Card7, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 460, 50, 50));

        p1Card8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card8.setOpaque(true);
        jPanel1.add(p1Card8, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 460, 50, 50));

        p1Card3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card3.setOpaque(true);
        jPanel1.add(p1Card3, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 320, 50, 50));

        p1Card2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card2.setOpaque(true);
        jPanel1.add(p1Card2, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 320, 50, 50));

        p1Card9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card9.setOpaque(true);
        jPanel1.add(p1Card9, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 460, 50, 50));

        aiCard5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard5.setOpaque(true);
        jPanel1.add(aiCard5, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 90, 50, 50));

        aiCard4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard4.setOpaque(true);
        jPanel1.add(aiCard4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 90, 50, 50));

        aiCard3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard3.setOpaque(true);
        jPanel1.add(aiCard3, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 20, 50, 50));

        aiCard2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard2.setOpaque(true);
        jPanel1.add(aiCard2, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 20, 50, 50));

        playerTotal.setBackground(new java.awt.Color(0, 0, 0));
        playerTotal.setFont(new java.awt.Font("VT323", 1, 40)); // NOI18N
        playerTotal.setForeground(new java.awt.Color(255, 255, 255));
        jPanel1.add(playerTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 510, 40, 40));

        aiTotal.setBackground(new java.awt.Color(0, 0, 0));
        aiTotal.setFont(new java.awt.Font("VT323", 1, 40)); // NOI18N
        aiTotal.setForeground(new java.awt.Color(255, 255, 255));
        jPanel1.add(aiTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 20, 130, 50));

        p1Name.setFont(FontLoader.getVT323(40f));

        p1Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(p1Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 100, 40));

        p2Name.setFont(FontLoader.getVT323(40f));

        p2Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(p2Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 100, 40));

        p3Name.setFont(FontLoader.getVT323(40f));

        p3Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(p3Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 100, 40));

        p4Name.setFont(FontLoader.getVT323(40f));

        p4Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(p4Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 100, 40));

        p5Name.setFont(FontLoader.getVT323(40f));

        p5Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(p5Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 260, 100, 40));

        p6Name.setFont(FontLoader.getVT323(40f));

        p6Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(p6Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 320, 100, 40));

        p1Total.setBackground(new java.awt.Color(0, 0, 0));

        p1Total.setFont(new java.awt.Font("VT323", 1, 30)); // NOI18N

        p1Total.setForeground(new java.awt.Color(255, 255, 255));
        p1Total.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        p1Total.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        p1Total.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                p1TotalActionPerformed(evt);
            }
        });
        jPanel1.add(p1Total, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, 50, 40));

        p2Total.setBackground(new java.awt.Color(0, 0, 0));

        p2Total.setFont(new java.awt.Font("VT323", 1, 40)); // NOI18N
        p2Total.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        p2Total.setForeground(new java.awt.Color(255, 255, 255));
        p2Total.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(p2Total, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 80, 50, 40));

        p3Total.setBackground(new java.awt.Color(0, 0, 0));

        p3Total.setFont(new java.awt.Font("VT323", 1, 40)); // NOI18N
        p3Total.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        p3Total.setForeground(new java.awt.Color(255, 255, 255));
        p3Total.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(p3Total, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 140, 50, 40));

        p4Total.setBackground(new java.awt.Color(0, 0, 0));

        p4Total.setFont(new java.awt.Font("VT323", 1, 40)); // NOI18N
        p4Total.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        p4Total.setForeground(new java.awt.Color(255, 255, 255));
        p4Total.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(p4Total, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 200, 50, 40));

        p5Total.setBackground(new java.awt.Color(0, 0, 0));

        p5Total.setFont(new java.awt.Font("VT323", 1, 40)); // NOI18N
        p5Total.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        p5Total.setForeground(new java.awt.Color(255, 255, 255));
        p5Total.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(p5Total, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 260, 50, 40));

        p6Total.setBackground(new java.awt.Color(0, 0, 0));
        p6Total.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        p6Total.setFont(new java.awt.Font("VT323", 1, 40)); // NOI18N

        p6Total.setForeground(new java.awt.Color(255, 255, 255));
        p6Total.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel1.add(p6Total, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 320, 50, 40));
        jPanel1.add(backgroundLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1200, 700));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void p1TotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_p1TotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_p1TotalActionPerformed

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
            java.util.logging.Logger.getLogger(Round1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Round1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Round1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Round1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Round1("Player 1").setVisible(true);
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
    private javax.swing.JTextField p1Total;
    private javax.swing.JLabel p2Name;
    private javax.swing.JTextField p2Total;
    private javax.swing.JLabel p3Name;
    private javax.swing.JTextField p3Total;
    private javax.swing.JLabel p4Name;
    private javax.swing.JTextField p4Total;
    private javax.swing.JLabel p5Name;
    private javax.swing.JTextField p5Total;
    private javax.swing.JLabel p6Name;
    private javax.swing.JTextField p6Total;
    private javax.swing.JButton pauseButton;
    private javax.swing.JLabel playerLabel;
    private javax.swing.JLabel playerTotal;
    private javax.swing.JButton stayButton;
    // End of variables declaration//GEN-END:variables

}
