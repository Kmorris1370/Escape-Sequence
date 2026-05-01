package escapesequence.UI;

/**
 * @author Akera Griffith & Kaitlyn Morris 
 * Escape Sequence Round3 Interface
 */

import escapesequence.*;
import static escapesequence.SpecialtyCard.Type.FREEZE;
import static escapesequence.SpecialtyCard.Type.PEEK;
import static escapesequence.SpecialtyCard.Type.REVERSE;
import static escapesequence.SpecialtyCard.Type.SHIELD;
import static escapesequence.SpecialtyCard.Type.SWAP;
import java.awt.Color;
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
    private boolean buttonsLocked = false;
    private int screenW = 1200, screenH = 700;

    // ── Fixed Specialty Slot Tracking ────────────────────────
    private SpecialtyCard[] slotCards = new SpecialtyCard[2]; // initial slot assignment
    private boolean[] slotUsed = {false, false};               // permanently used slots
    private boolean pendingEscapeWin = false;                  // single-player 2-PAC win flag

    // ── Single Player Constructor ────────────────────────────
    public Round3(String playerName) {
        initComponents();
        initCardSlots();
        initAICardSlots();
        setMaximumSize(null);
        applyFullScreen();
        SoundManager.enableButtonSounds(this.getContentPane());
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
        applyFullScreen();
        SoundManager.enableButtonSounds(this.getContentPane());
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

    // ── Full Screen Helper ───────────────────────────────────
    private void applyFullScreen() {
        java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        screenW = screen.width;
        screenH = screen.height;
        jPanel1.setPreferredSize(screen);
        jPanel1.setMinimumSize(screen);
        backgroundLabel.setBounds(0, 0, screenW, screenH);
        setSize(screenW, screenH);
        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
    }

    // ── Setup Methods ────────────────────────────────────────
    // Loads all background images and icons
    private void setupUI() {
        
        backgroundLabel.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/Round3.png", screenW, screenH));
        javax.swing.SwingUtilities.invokeLater(() -> backgroundLabel.setBounds(0, 0, screenW, screenH));
        pauseButton.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/Pause.png", 40, 40));
        deckLabel.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/deck.png", 50, 50));
        // Style keycard labels
        for (JLabel l : new JLabel[]{keycardLabel1, keycardLabel2, keycardLabel3}) {
            l.setFont(FontLoader.getVT323(24f));
            l.setForeground(new Color(198, 40, 40));
            l.setVisible(false);
        }
        for (JLabel l : new JLabel[]{keycard1, keycard2, keycard3}) {
            l.setVisible(false);
        }
        // Hide wild card controls until player has a wild card
        wildCard.setVisible(false);
        useWildCard.setVisible(false);
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
        specialtyCard1Button.addActionListener(e -> useAbilitySlot(0));
        specialtyCard2Button.addActionListener(e -> useAbilitySlot(1));
        useWildCard.addActionListener(e -> useWildCardAction());
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
        buttonsLocked = false;
        gameController.startRound();

        // In multiplayer, update current player reference
        player = isMultiplayer ? allPlayers.get(currentPlayerIndex) : player;
        gameController.startPlayerTurn(player);
        playerLabel.setText(player.getName());

        // Display player's opening card(s)
        for (int val : gameController.getPlayerOpeningCards(player)) {
            addCardToDisplay(val);
            SoundManager.playDeal();
        }

        // Show AI's hidden card face-down
        dealAIOpeningCards();

        // Assign specialty cards to fixed slots at round start (no shifting after use)
        initSlotCards();
        // Reset slot-used state for new player
        slotUsed[0] = false;
        slotUsed[1] = false;

        // AI plays its full turn now — cards revealed gradually as player hits
        gameController.playAITurn();

        updateTotals();
        updateSummaryLabels();
        updateAbilityUI();
        updateAISpecialtyDisplay();
        updateKeycardDisplay();
        updateActionButtons();
        animateTurnIndicator();
    }

    // ── Player Actions ───────────────────────────────────────
    // Called when player clicks Hit
    private void onHit() {
        if (buttonsLocked || !gameController.playerCanHit(player)) {
            return;
        }
        gameController.playerHit(player);
        addCardToDisplay(gameController.getLastPlayerCard(player));
        SoundManager.playDeal();

        // Disable buttons during timer delay
        buttonsLocked = true;
        updateActionButtons();
        updateTotals();

        // After 1 second, reveal one AI card
        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
            ArrayList<Card> aiHand = gameController.getAI().getHand();
            int nextRevealIndex = aiCardsRevealed + 1; // skip index 0 (hidden card)

            if (nextRevealIndex < aiHand.size()) {
                // Reveal next AI card
                addAICardToDisplay(aiHand.get(nextRevealIndex).getValue());
                SoundManager.playDeal();
                aiCardsRevealed++;
                updateTotals();
            } else {
                // No more AI cards to reveal — AI has stayed
                GameDialog.showTimed(Round3.this, "The System stayed.", 1500);
            }

            buttonsLocked = false;
            updateActionButtons();
        });
        timer.setRepeats(false); // fire once only
        timer.start();
    }

    // Called when player clicks Stay
    private void onStay() {
        if (buttonsLocked) {
            return;
        }
        gameController.resolvePeekShuffle(); // shuffle peeked card if player chose not to hit
        buttonsLocked = true;
        updateActionButtons();

        if (isMultiplayer) {
            playerStayed[currentPlayerIndex] = true;

            // Reveal one AI card when a player stays
            ArrayList<Card> aiHand = gameController.getAI().getHand();
            int nextRevealIndex = aiCardsRevealed + 1;

            if (nextRevealIndex < aiHand.size()) {
                javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
                    addAICardToDisplay(aiHand.get(nextRevealIndex).getValue());
                    SoundManager.playDeal();
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
                        SoundManager.playDeal();
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
            gameController.startPlayerTurn(player);
            playerLabel.setText(player.getName());

            // Reset fixed slots for the new player
            initSlotCards();
            slotUsed[0] = false;
            slotUsed[1] = false;

            // Clear player cards for next player's turn
            resetPlayerCardDisplay();
            resetAIDisplayForNextPlayer();

            // Show next player's opening cards
            for (int val : gameController.getPlayerOpeningCards(player)) {
                addCardToDisplay(val);
                SoundManager.playDeal();
            }

            updateTotals();
            updateSummaryLabels();
            updateAbilityUI();
            buttonsLocked = false;
            updateActionButtons();

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

    // Shows outcome dialog then navigates to EscapeScreen or GameOver
    private void showOutcome(GameController.RoundOutcome outcome) {
        SwingUtilities.invokeLater(() -> {
            SoundManager.playOutcome(outcome != GameController.RoundOutcome.ELIMINATED);
            GameDialog.show(this, gameController.getRoundSummaryMessage(outcome));

            // Tiebreaker — modal, execution continues after it closes
            ArrayList<Player> tied = gameController.getTiedPlayers();
            if (!tied.isEmpty() && gameController.canAwardMoreKeycards()) {
                new TiebreakerRound(this, gameController, tied).setVisible(true);
            }

            // 2nd keycard bonus (may set pendingEscapeWin for single-player)
            pendingEscapeWin = false;
            handleKeycardBonus();

            gameController.advanceRound();

            // Single-player earned 2nd PAC in Round 3 → instant win
            if (pendingEscapeWin) {
                new EscapeScreen(gameController).setVisible(true);
                dispose();
                return;
            }

            // Navigate to EscapeScreen if any player survived with a keycard
            ArrayList<Player> survivors = gameController.getSurvivors();
            if (!survivors.isEmpty()) {
                new EscapeScreen(gameController).setVisible(true);
            } else {
                GameDialog.show(this, "All players have been eliminated!");
                new GameOver(gameController).setVisible(true);
            }
            dispose();
        });
    }

    // Round 3 keycard bonus: single-player = instant win; multiplayer = gift only
    private void handleKeycardBonus() {
        for (Player p : gameController.getKeycardBonusPlayers()) {
            if (!gameController.isMultiplayer()) {
                // Single player 2nd PAC in Round 3 = win
                p.clearPendingKeycardBonus();
                pendingEscapeWin = true;
                return;
            }
            // Multiplayer: gift only
            java.util.ArrayList<String> giftChoices = new java.util.ArrayList<>();
            for (Player other : gameController.getPlayers()) {
                if (other != p && other.isAlive() && !other.hasKeycard()) {
                    giftChoices.add("Gift to " + other.getName());
                }
            }
            if (giftChoices.isEmpty()) {
                // All others already have PACs — nothing happens
                p.clearPendingKeycardBonus();
                continue;
            }
            String[] options = giftChoices.toArray(new String[0]);
            String choice = GameDialog.showChoice(this,
                p.getName() + " earned a 2nd keycard!\nGift it to:", options);
            if (choice != null && choice.startsWith("Gift to ")) {
                String recipientName = choice.substring("Gift to ".length());
                for (Player recipient : gameController.getPlayers()) {
                    if (recipient.getName().equals(recipientName)) {
                        p.transferKeycardTo(recipient);
                        break;
                    }
                }
            }
            p.clearPendingKeycardBonus();
        }
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
        aiCardSlots[0].setIcon(ResourceLoader.loadImageScaled("/assets/pictures/deck.png", 50, 50));
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
        SoundManager.playDeal();
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



    private void resetAIDisplayForNextPlayer() {
        aiCardsRevealed = 0;
        aiIndex = 1;
        for (int i = 0; i < aiCardSlots.length; i++) {
            aiCardSlots[i].setIcon(null);
            aiCardSlots[i].setVisible(false);
        }
        aiCardSlots[0].setVisible(true);
        aiCardSlots[0].setIcon(ResourceLoader.loadImageScaled("/assets/pictures/deck.png", 50, 50));
        aiTotal.setText("? + 0");
    }

    private void updateActionButtons() {
        boolean canHit = !buttonsLocked && gameController.playerCanHit(player);
        boolean canStay = !buttonsLocked && player != null && player.isAlive() && (!isMultiplayer || !playerStayed[currentPlayerIndex]);
        hitButton.setEnabled(canHit);
        stayButton.setEnabled(canStay);
        specialtyCard1Button.setEnabled(!buttonsLocked && !slotUsed[0] && slotCards[0] != null);
        specialtyCard2Button.setEnabled(!buttonsLocked && !slotUsed[1] && slotCards[1] != null);
        useWildCard.setEnabled(!buttonsLocked && player != null && player.hasWildCard());
    }

    private void animateTurnIndicator() {
        final String baseText = player.getName();
        playerLabel.setText(">>> " + baseText + " <<<");
        playerLabel.setForeground(new Color(255, 220, 120));
        javax.swing.Timer flashTimer = new javax.swing.Timer(250, null);
        flashTimer.addActionListener(new java.awt.event.ActionListener() {
            int count = 0;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                playerLabel.setForeground((count % 2 == 0) ? Color.WHITE : new Color(255, 220, 120));
                count++;
                if (count >= 6) {
                    flashTimer.stop();
                    playerLabel.setForeground(Color.WHITE);
                    playerLabel.setText(baseText);
                }
            }
        });
        flashTimer.start();
    }

    private String getSpecialtyImagePath(SpecialtyCard.Type type) {
        switch (type) {
            case SHIELD: return "/assets/pictures/shield.png";
            case REVERSE: return "/assets/pictures/reverse.png";
            case FREEZE: return "/assets/pictures/freeze.png";
            case SWAP: return "/assets/pictures/swap.png";
            case PEEK: return "/assets/pictures/peek.png";
            default: return "/assets/pictures/deck.png";
        }
        
    }

    // Saves the initial specialty card references to fixed slots at round start
    private void initSlotCards() {
        ArrayList<SpecialtyCard> cards = player.getSpecialtyCards();
        slotCards[0] = cards.size() > 0 ? cards.get(0) : null;
        slotCards[1] = cards.size() > 1 ? cards.get(1) : null;
    }

    private void updateAbilityUI() {
        updateAbilitySlot(0, specialtyCardLabel1, specialtyCard1Button);
        updateAbilitySlot(1, specialtyCardLabel2, specialtyCard2Button);
        // Wild card — only visible in Round 3 when player holds one
        boolean hasWild = player != null && player.hasWildCard();
        wildCard.setVisible(hasWild);
        useWildCard.setVisible(hasWild);
        if (hasWild) {
            wildCard.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/wild.png", 50, 50));
            wildCard.setToolTipText(new SpecialtyCard(SpecialtyCard.Type.WILD).toString());
        }
    }

    // Uses a fixed slot — slot icon stays until used, never shifts
    private void updateAbilitySlot(int slotIndex, JLabel label, javax.swing.JButton button) {
        if (slotUsed[slotIndex] || slotCards[slotIndex] == null) {
            label.setVisible(false);
            label.setIcon(null);
            button.setVisible(false);
            return;
        }
        label.setVisible(true);
        button.setVisible(true);
        SpecialtyCard card = slotCards[slotIndex];
        label.setIcon(ResourceLoader.loadImageScaled(getSpecialtyImagePath(card.getType()), 50, 50));
        label.setToolTipText(card.toString());
    }

    private void useAbilitySlot(int slotIndex) {
        if (buttonsLocked || player == null) return;
        if (slotUsed[slotIndex] || slotCards[slotIndex] == null) return;

        useSpecialtyCard(slotIndex);

        redrawPlayerCards();
        redrawVisibleAICards();
        updateTotals();
        updateAbilityUI();
        updateActionButtons();
    }

    // Handles the standalone Wild card button (separate from specialty slots)
    private void useWildCardAction() {
        if (buttonsLocked || player == null || !player.hasWildCard()) return;
        String input = GameDialog.showInput(this, "Choose a value from 1 to 9 for Wild.");
        if (input == null) return;
        try {
            int chosen = Integer.parseInt(input.trim());
            if (chosen < 1 || chosen > 9) {
                GameDialog.show(this, "Enter a number from 1 to 9.");
                return;
            }
            gameController.applyWild(player, chosen);
            GameDialog.show(this, "Wild used. Added " + chosen + ".");
            wildCard.setVisible(false);
            useWildCard.setVisible(false);
            redrawPlayerCards();
            updateTotals();
            updateActionButtons();
        } catch (NumberFormatException ex) {
            GameDialog.show(this, "Enter a valid number.");
        }
    }

    private void useSpecialtyCard(int slotIndex) {
        SpecialtyCard card = slotCards[slotIndex];
        if (card == null) return;

        // REVERSE must be used after at least one hit
        if (card.getType() == SpecialtyCard.Type.REVERSE && player.getHand().size() < 2) {
            GameDialog.show(this, "Reverse must be used after hitting at least once.");
            return;
        }

        // Remove this specific card from the player's hand by reference
        boolean removed = player.getSpecialtyCards().remove(card);
        if (!removed) return;
        slotUsed[slotIndex] = true;

        switch (card.getType()) {
            case SHIELD:
                gameController.applyShield(player);
                GameDialog.show(this, "Shield used. Last drawn card removed.");
                break;
            case REVERSE:
                gameController.applyReverse(player);
                GameDialog.show(this, "Reverse used. A card was subtracted from your total.");
                break;
            case FREEZE:
                applyFreezeChoice();
                break;
            case SWAP:
                applySwapChoice();
                break;
            case PEEK:
                GameDialog.show(this, "Next card in deck: " + gameController.applyPeek());
                break;
            default:
                break;
        }
    }

    private void applyFreezeChoice() {
        ArrayList<Player> targets = new ArrayList<>();
        if (isMultiplayer) {
            for (Player p : allPlayers) {
                if (p != player && p.isAlive()) {
                    targets.add(p);
                }
            }
        }
        targets.add(gameController.getAI());

        if (targets.isEmpty()) {
            GameDialog.show(this, "No valid target to freeze.");
            return;
        }

        String[] options = new String[targets.size()];
        for (int i = 0; i < targets.size(); i++) {
            options[i] = targets.get(i).getName();
        }

        String selected = GameDialog.showChoice(this, "Choose a target to freeze:", options);
        if (selected == null) return;

        for (Player target : targets) {
            if (target.getName().equals(selected)) {
                gameController.applyFreeze(target);
                GameDialog.show(this, selected + " is frozen for the next turn.");
                return;
            }
        }
    }

    private void applySwapChoice() {
        int playerCards = player.getHand().size();
        if (playerCards == 0 || aiCardsRevealed <= 0) {
            GameDialog.show(this, "You need at least one player card and one revealed AI card to swap.");
            return;
        }

        String[] playerOptions = new String[playerCards];
        for (int i = 0; i < playerCards; i++) {
            playerOptions[i] = "Your card " + (i + 1) + " = " + player.getHand().get(i).getValue();
        }
        String[] aiOptions = new String[aiCardsRevealed];
        ArrayList<Card> aiHand = gameController.getAI().getHand();
        for (int i = 0; i < aiCardsRevealed; i++) {
            aiOptions[i] = "AI card " + (i + 1) + " = " + aiHand.get(i + 1).getValue();
        }

        String playerSelection = GameDialog.showChoice(this, "Choose your card:", playerOptions);
        if (playerSelection == null) return;
        String aiSelection = GameDialog.showChoice(this, "Choose a revealed AI card:", aiOptions);
        if (aiSelection == null) return;

        int playerIndex = java.util.Arrays.asList(playerOptions).indexOf(playerSelection);
        int aiIndexChoice = java.util.Arrays.asList(aiOptions).indexOf(aiSelection) + 1;
        gameController.applySwap(player, playerIndex, aiIndexChoice);
        GameDialog.show(this, "Swap complete.");
    }

    private void redrawPlayerCards() {
        resetPlayerCardDisplay();
        for (Card c : player.getHand()) {
            addCardToDisplay(c.getValue());
        }
    }

    private void redrawVisibleAICards() {
        int revealed = aiCardsRevealed;
        resetAIDisplayForNextPlayer();
        ArrayList<Card> aiHand = gameController.getAI().getHand();
        for (int i = 1; i <= revealed && i < aiHand.size(); i++) {
            addAICardToDisplay(aiHand.get(i).getValue());
            aiCardsRevealed++;
        }
    }

    // Shows both AI specialty card icons; hides those already used
    private void updateAISpecialtyDisplay() {
        ArrayList<SpecialtyCard> aiCards = gameController.getAI().getSpecialtyCards();
        if (aiCards.size() > 0) {
            aiSpecialtyCard1.setIcon(ResourceLoader.loadImageScaled(
                getSpecialtyImagePath(aiCards.get(0).getType()), 50, 50));
            aiSpecialtyCard1.setToolTipText(aiCards.get(0).toString());
            aiSpecialtyCard1.setVisible(true);
        } else {
            aiSpecialtyCard1.setIcon(null);
            aiSpecialtyCard1.setVisible(false);
        }
        if (aiCards.size() > 1) {
            aiSpecialtyCard2.setIcon(ResourceLoader.loadImageScaled(
                getSpecialtyImagePath(aiCards.get(1).getType()), 50, 50));
            aiSpecialtyCard2.setToolTipText(aiCards.get(1).toString());
            aiSpecialtyCard2.setVisible(true);
        } else {
            aiSpecialtyCard2.setIcon(null);
            aiSpecialtyCard2.setVisible(false);
        }
    }

    // Shows names and PAC images for players who hold keycards
    private void updateKeycardDisplay() {
        JLabel[] nameLabels = {keycardLabel1, keycardLabel2, keycardLabel3};
        JLabel[] images     = {keycard1, keycard2, keycard3};
        for (JLabel l : nameLabels) { l.setVisible(false); l.setText(""); }
        for (JLabel l : images)      { l.setVisible(false); }
        int slot = 0;
        for (Player p : gameController.getPlayers()) {
            if (p.hasKeycard() && slot < 3) {
                nameLabels[slot].setText(p.getName());
                nameLabels[slot].setVisible(true);
                images[slot].setIcon(ResourceLoader.loadImageScaled("/assets/pictures/P.A.C.png", 70, 50));
                images[slot].setVisible(true);
                slot++;
            }
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
        wildCard = new javax.swing.JLabel();
        useWildCard = new javax.swing.JButton();
        keycardLabel1 = new javax.swing.JLabel();
        keycardLabel2 = new javax.swing.JLabel();
        keycardLabel3 = new javax.swing.JLabel();
        keycard1 = new javax.swing.JLabel();
        keycard2 = new javax.swing.JLabel();
        keycard3 = new javax.swing.JLabel();
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pauseButton.setContentAreaFilled(false);
        pauseButton.setBorder(null);
        pauseButton.setBorderPainted(false);
        pauseButton.setFocusPainted(false);
        pauseButton.setBackground(new java.awt.Color(0, 0, 0, 0));
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });
        jPanel1.add(pauseButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1120, 20, 40, 40));
        jPanel1.add(aiSpecialtyCard1, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 130, 50, 50));
        jPanel1.add(specialtyCardLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 460, 50, 50));
        jPanel1.add(specialtyCardLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 460, 50, 50));

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
        hitButton.setFocusPainted(false);
        jPanel1.add(hitButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 560, 180, 80));

        specialtyCard1Button.setFont(FontLoader.getVT323(14f));
        specialtyCard1Button.setBackground(new java.awt.Color(0, 0, 0, 100));
        specialtyCard1Button.setForeground(new java.awt.Color(255, 255, 255));
        specialtyCard1Button.setContentAreaFilled(false);
        specialtyCard1Button.setText("Use");
        specialtyCard1Button.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black));
        specialtyCard1Button.setContentAreaFilled(false);
        specialtyCard1Button.setFocusPainted(false);
        jPanel1.add(specialtyCard1Button, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 520, 60, -1));

        aiCard1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jPanel1.add(aiCard1, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 20, 50, 50));

        deckLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
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
        jPanel1.add(aiTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 30, 170, 50));

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
        jPanel1.add(p1Name, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 20, 100, 40));

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
        specialtyCard2Button.setContentAreaFilled(false);
        specialtyCard2Button.setFocusPainted(false);
        jPanel1.add(specialtyCard2Button, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 520, 60, -1));
        jPanel1.add(wildCard, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 320, 50, 50));

        useWildCard.setFont(FontLoader.getVT323(14f));
        useWildCard.setForeground(new java.awt.Color(255, 255, 255));
        useWildCard.setText("Use");
        useWildCard.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black));
        useWildCard.setContentAreaFilled(false);
        useWildCard.setFocusPainted(false);
        jPanel1.add(useWildCard, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 380, 60, -1));

        p1Name.setFont(FontLoader.getVT323(40f));

        p1Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(keycardLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 140, 100, 40));

        p1Name.setFont(FontLoader.getVT323(40f));

        p1Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(keycardLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 220, 100, 40));

        p1Name.setFont(FontLoader.getVT323(40f));

        p1Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(keycardLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 330, 100, 40));

        p1Name.setFont(FontLoader.getVT323(40f));

        p1Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(keycard1, new org.netbeans.lib.awtextra.AbsoluteConstraints(990, 130, 70, 50));

        p1Name.setFont(FontLoader.getVT323(40f));

        p1Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(keycard2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 240, 70, 50));

        p1Name.setFont(FontLoader.getVT323(40f));

        p1Name.setForeground(java.awt.Color.WHITE);
        jPanel1.add(keycard3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1050, 370, 70, 50));
        jPanel1.add(backgroundLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

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
    private javax.swing.JLabel keycard1;
    private javax.swing.JLabel keycard2;
    private javax.swing.JLabel keycard3;
    private javax.swing.JLabel keycardLabel1;
    private javax.swing.JLabel keycardLabel2;
    private javax.swing.JLabel keycardLabel3;
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
    private javax.swing.JButton useWildCard;
    private javax.swing.JLabel wildCard;
    // End of variables declaration//GEN-END:variables
}
