package escapesequence;

/**
 * @author Akera Griffith & Kaitlyn Morris
 * Controls all game flow, round logic, and outcome resolution
 */

import java.util.ArrayList;

public class GameController {

    // ── Fields ───────────────────────────────────────────────
    private ArrayList<Player> players;
    private AIPlayer ai;
    private Deck deck;
    private SpecialtyDeck specialtyDeck;
    private int currentRound;
    private int pacCount;
    private GameMode gameMode;
    private boolean peekPending = false; // true after Peek used, cleared on hit or stay
    private boolean aiStayed = false;    // multiplayer: AI has chosen to stay this round

    private static final int MAX_PACS = 3;

    // ── Game Mode ────────────────────────────────────────────
    public enum GameMode { SINGLE_PLAYER, MULTIPLAYER }

    // ── Round Outcome ────────────────────────────────────────
    public enum RoundOutcome {
        WIN_WITH_PAC,      // player wins and gets a keycard
        PROCEED_NO_PAC,    // player advances but no keycard
        ELIMINATED,        // player busted, AI did not
        ALL_BUST_PROCEED   // both bust — everyone advances
    }

    // ── Constructors ─────────────────────────────────────────

    // Full constructor — used for multiplayer
    public GameController(ArrayList<Player> players, GameMode gameMode) {
        this.players = players;
        this.gameMode = gameMode;
        this.ai = new AIPlayer();
        this.deck = new Deck(players.size());
        this.specialtyDeck = new SpecialtyDeck(players.size());
        this.currentRound = 1;
        this.pacCount = 0;
    }

    // Single player shortcut — defaults to SINGLE_PLAYER mode
    public GameController(ArrayList<Player> players) {
        this(players, GameMode.SINGLE_PLAYER);
    }

    // ── Round Management ─────────────────────────────────────

    // Starts a new round — rebuilds deck, deals opening cards, deals specialty cards in rounds 2+
    public void startRound() {
        clearAllHands();
        aiStayed = false;
        peekPending = false;
        // Reset frozen state at start of every round
        for (Player p : players) p.setFrozen(false);
        ai.setFrozen(false);

        // Rebuild and shuffle deck each round to prevent running out of cards
        deck = new Deck(players.size());
        deck.shuffle();

        System.out.println("=== ROUND " + currentRound + " START ===");
        System.out.println("Deck size: " + deck.size());

        // Deal one opening card to each alive player
        for (Player p : players) {
            if (p.isAlive()) {
                p.receiveCard(new Card(deck.drawCard(), true));
                System.out.println(p.getName() + " opening hand: " + p.getHandTotal());
            }
        }

        // Deal AI's one hidden card
        ai.receiveCard(new Card(deck.drawCard(), false));
        System.out.println("AI opening card (hidden): " + ai.getHand().get(0).getValue());

        // Deal specialty cards starting round 2 (players only — AI plays without specialty cards)
        if (currentRound >= 2) {
            specialtyDeck.dealToAll(players);
        }
    }

    // ── Interactive AI Turn (multiplayer round-robin) ────────

    // True if AI has chosen to stay or has busted — turn should be skipped
    public boolean aiIsDone() {
        return aiStayed || ai.isBust();
    }

    // Drives AI's per-turn decision in multiplayer rotation.
    // Returns true if AI hit (drew a card), false if AI chose to stay.
    public boolean aiTakeSingleAction() {
        if (aiIsDone()) return false;
        if (ai.isFrozen()) {
            // Frozen = locked at current total = treat as stayed
            aiStayed = true;
            System.out.println("AI is frozen — stays at total: " + ai.getHandTotal());
            return false;
        }
        if (!ai.shouldHit()) {
            aiStayed = true;
            System.out.println("AI stays at total: " + ai.getHandTotal());
            return false;
        }
        int v = deck.drawCard();
        if (v == -1) {
            aiStayed = true;
            System.err.println("Deck empty during AI turn — AI forced to stay");
            return false;
        }
        ai.receiveCard(new Card(v, true));
        System.out.println("AI hit -> drew " + v + ", new total: " + ai.getHandTotal());
        return true;
    }

    // Returns the value of the AI's most recently drawn card
    public int getLastAICardValue() {
        ArrayList<Card> hand = ai.getHand();
        if (hand.isEmpty()) return 0;
        return hand.get(hand.size() - 1).getValue();
    }

    // Executes the AI's full turn — hits based on round threshold
    public void playAITurn() {
        System.out.println("--- AI TURN ---");
        System.out.println("AI hand before turn: " + ai.getHandTotal());
        ai.takeTurn(deck, players);
        System.out.println("AI hand after turn: " + ai.getHandTotal());
        System.out.println("AI cards: ");
        for (Card c : ai.getHand()) {
            System.out.println("  " + c.getValue());
        }
    }

    // Resolves and applies outcomes for all alive players
    public void resolveRound() {
        for (Player p : players) {
            if (!p.isAlive()) continue;
            RoundOutcome outcome = resolveOutcome(p);
            applyOutcome(p, outcome);
        }
    }

    // Advances round counter and increases AI difficulty
    public void advanceRound() {
        if (currentRound < 3) {
            currentRound++;
            ai.advanceRound();
        }
    }

    // ── Player Actions ───────────────────────────────────────

    // Deals one card to the player if they are eligible to hit
    public void playerHit(Player p) {
        if (playerCanHit(p)) {
            int val = deck.drawCard();
            if (val == -1) {
                System.err.println("Deck empty — cannot deal card");
                return;
            }
            p.receiveCard(new Card(val, true));
            peekPending = false; // player drew a card — peeked card is now gone
            System.out.println(p.getName() + " hit -> new total: " + p.getHandTotal());
        }
    }

    // Unfreezes a player at the start of their turn
    public void startPlayerTurn(Player p) {
        if (p.isFrozen()) p.setFrozen(false);
    }

    // Returns true if the player is eligible to draw another card
    public boolean playerCanHit(Player p) {
        return p.isAlive() && !p.isFrozen() && !p.isBust();
    }

    // ── Specialty Card Actions ───────────────────────────────

    // SHIELD — removes the last card the player drew
    public void applyShield(Player p) {
        ArrayList<Card> hand = p.getHand();
        if (!hand.isEmpty()) hand.remove(hand.size() - 1);
    }

    // REVERSE — removes the last drawn card from the hand (subtracts its value and removes from display)
    public void applyReverse(Player p) {
        ArrayList<Card> hand = p.getHand();
        if (!hand.isEmpty()) {
            Card removed = hand.remove(hand.size() - 1);
            System.out.println(p.getName() + " reverse -> removed " + removed.getValue() + ", new total: " + p.getHandTotal());
        }
    }

    // FREEZE — prevents target from hitting on their next turn
    public void applyFreeze(Player target) {
        target.setFrozen(true);
    }

    // WILD — lets player choose the value of their next card
    public void applyWild(Player p, int chosenValue) {
        if (chosenValue >= 1 && chosenValue <= 9 && p.hasWildCard()) {
            p.receiveCard(new Card(chosenValue, true));
            p.setHasWildCard(false);
        }
    }

    // SWAP — exchanges one of the player's cards with a visible AI card
    public void applySwap(Player p, int playerCardIndex, int aiCardIndex) {
        ArrayList<Card> pHand = p.getHand();
        ArrayList<Card> aiHand = ai.getHand();
        if (playerCardIndex >= 0 && aiCardIndex >= 0
                && playerCardIndex < pHand.size() && aiCardIndex < aiHand.size()) {
            if (aiHand.get(aiCardIndex).isFaceUp()) {
                Card temp = pHand.get(playerCardIndex);
                pHand.set(playerCardIndex, aiHand.get(aiCardIndex));
                aiHand.set(aiCardIndex, temp);
            }
        }
    }

    // PEEK — returns the next card in the deck without drawing it
    // Sets peekPending so the card is shuffled away if the player stays instead of hitting
    public int applyPeek() {
        peekPending = true;
        return deck.peekCard();
    }

    // Called at the start of onStay — shuffles top card to random position if a peek is pending
    public void resolvePeekShuffle() {
        if (peekPending) {
            deck.shuffleTopCard();
            peekPending = false;
        }
    }

    // ── Outcome Resolution ───────────────────────────────────

    // Determines a player's outcome — only the sole best player earns a keycard;
    // tied players get PROCEED_NO_PAC and are resolved via TiebreakerRound
    public RoundOutcome resolveOutcome(Player p) {
        boolean playerBust = p.isBust();
        boolean aiBust     = ai.isBust();

        if (playerBust && aiBust)  return RoundOutcome.ALL_BUST_PROCEED;
        if (playerBust && !aiBust) return RoundOutcome.ELIMINATED;

        // Player is not bust — check whether they beat or match the AI
        boolean playerBeatsAI = aiBust || p.getHandTotal() >= ai.getHandTotal();
        if (!playerBeatsAI) return RoundOutcome.PROCEED_NO_PAC;

        // Player qualifies — only award keycard if they are the sole best player
        int best = getBestPlayerTotal();
        if (p.getHandTotal() == best && getTiedPlayers().isEmpty()) {
            return (pacCount < MAX_PACS) ? RoundOutcome.WIN_WITH_PAC : RoundOutcome.PROCEED_NO_PAC;
        }
        return RoundOutcome.PROCEED_NO_PAC; // tied players handled by TiebreakerRound
    }

    // Returns the highest hand total among alive, non-busted players who beat (or tied) the AI.
    // Used by resolveOutcome to decide whether a single player should be awarded the keycard.
    private int getBestPlayerTotal() {
        int best = -1;
        for (Player p : players) {
            if (p.isAlive() && !p.isBust()) {
                if (ai.isBust() || p.getHandTotal() >= ai.getHandTotal()) {
                    best = Math.max(best, p.getHandTotal());
                }
            }
        }
        return best;
    }

    // Highest hand total among alive non-busted players, regardless of AI total —
    // used for tiebreaker detection so cadets that tied with each other below the
    // AI's total still get to play a tiebreaker.
    private int getHighestAlivePlayerTotal() {
        int best = -1;
        for (Player p : players) {
            if (p.isAlive() && !p.isBust()) {
                best = Math.max(best, p.getHandTotal());
            }
        }
        return best;
    }

    // Returns players who are tied for the highest non-bust total; empty list if no tie.
    // Only triggers a tiebreaker when the tied players beat (or match) the AI — if the AI's
    // total is higher, the AI wins outright and no PAC is in play.
    public ArrayList<Player> getTiedPlayers() {
        int best = getHighestAlivePlayerTotal();
        if (best < 0) return new ArrayList<>();
        if (!ai.isBust() && best < ai.getHandTotal()) return new ArrayList<>();
        ArrayList<Player> tied = new ArrayList<>();
        for (Player p : players) {
            if (p.isAlive() && !p.isBust() && p.getHandTotal() == best) {
                tied.add(p);
            }
        }
        return tied.size() > 1 ? tied : new ArrayList<>();
    }

    // Awards a keycard to the tiebreaker winner — called by TiebreakerRound
    public void awardTiebreakerKeycard(Player winner) {
        if (pacCount < MAX_PACS) {
            winner.awardKeycard();
            pacCount++;
        }
    }

    // Returns players who earned their 2nd keycard and need to choose Shield or gift
    public ArrayList<Player> getKeycardBonusPlayers() {
        ArrayList<Player> result = new ArrayList<>();
        for (Player p : players) {
            if (p.isPendingKeycardBonus()) result.add(p);
        }
        return result;
    }

    // Returns true if another keycard can still be awarded this game
    public boolean canAwardMoreKeycards() { return pacCount < MAX_PACS; }

    // Applies the outcome — awards keycard, eliminates player, or does nothing
    private void applyOutcome(Player p, RoundOutcome outcome) {
        switch (outcome) {
            case WIN_WITH_PAC:
                p.awardKeycard();
                pacCount++;
                break;
            case ELIMINATED:
                p.eliminate();
                break;
            case ALL_BUST_PROCEED:
            case PROCEED_NO_PAC:
                break;
        }
    }

    // ── Outcome Messages ─────────────────────────────────────

    // Returns the outcome message — single line for single player, full summary for multiplayer
    public String getOutcomeMessage(RoundOutcome outcome) {
        if (!isMultiplayer()) {
            switch (outcome) {
                case WIN_WITH_PAC:     return "You win! P.A.C. keycard earned.";
                case PROCEED_NO_PAC:   return "You advance — no keycard.";
                case ELIMINATED:       return "You have been eliminated.";
                case ALL_BUST_PROCEED: return "Both bust! Everyone advances.";
                default:               return "";
            }
        }

        // Multiplayer — show each player's outcome in one message
        StringBuilder sb = new StringBuilder();
        sb.append("=== ROUND RESULTS ===\n\n");

        for (Player p : players) {
            // Only show outcome for players who were alive this round
            if (!p.isAlive()) {
                sb.append(p.getName()).append(": Eliminated\n");
                continue;
            }
            RoundOutcome playerOutcome = resolveOutcome(p);
            String status;
            switch (playerOutcome) {
                case WIN_WITH_PAC:     status = "Advances + P.A.C. keycard"; break;
                case PROCEED_NO_PAC:   status = "Advances — no keycard"; break;
                case ELIMINATED:       status = "Eliminated"; break;
                case ALL_BUST_PROCEED: status = "Advances (both bust)"; break;
                default:               status = ""; break;
            }
            sb.append(p.getName()).append(": ").append(status).append("\n");
        }

        sb.append("\nThe System: ").append(ai.getHandTotal());
        if (ai.isBust()) sb.append(" (bust)");

        return sb.toString();
    }

    public String getRoundSummaryMessage(RoundOutcome outcome) {
        StringBuilder body = new StringBuilder();

        if (!isMultiplayer()) {
            body.append("Result: ").append(getOutcomeMessage(outcome)).append("\n");
            Player p = players.get(0);
            body.append(p.getName()).append(" total: ").append(p.getHandTotal());
            if (p.isBust()) body.append(" (bust)");
            body.append("\n");
            body.append("P.A.C. keycards: ").append(p.getKeycardCount()).append("\n");
            body.append("Status: ").append(p.isAlive() ? "Alive" : "Eliminated").append("\n");
        } else {
            for (Player p : players) {
                body.append(p.getName()).append(" — Total: ").append(p.getHandTotal());
                if (p.isBust()) body.append(" (bust)");
                body.append(" | Keycards: ").append(p.getKeycardCount());
                body.append(" | Status: ").append(p.isAlive() ? "Alive" : "Eliminated");
                body.append("\n");
            }
        }

        body.append("\nThe System total: ").append(ai.getHandTotal());
        if (ai.isBust()) body.append(" (bust)");
        body.append("\nTotal P.A.C.s awarded so far: ").append(pacCount).append("/").append(MAX_PACS);

        String title = "=== END OF ROUND " + currentRound + " ===";
        int maxWidth = title.length();
        for (String line : body.toString().split("\n")) {
            if (line.length() > maxWidth) maxWidth = line.length();
        }
        int pad = (maxWidth - title.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pad; i++) sb.append(' ');
        sb.append(title).append("\n\n").append(body);
        return sb.toString();
    }

    public String getFinalSummaryMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== FINAL RESULTS ===\n\n");

        int highestKeycards = -1;
        ArrayList<Player> winners = new ArrayList<>();

        for (Player p : players) {
            sb.append(p.getName())
              .append(" — Keycards: ").append(p.getKeycardCount())
              .append(" | Final Status: ").append(p.isAlive() ? "Alive" : "Eliminated");

            if (p.isAlive() && p.hasKeycard()) {
                sb.append(" | Escaped");
            } else if (p.isAlive()) {
                sb.append(" | Left Behind");
            }
            sb.append("\n");

            if (p.getKeycardCount() > highestKeycards) {
                highestKeycards = p.getKeycardCount();
                winners.clear();
                winners.add(p);
            } else if (p.getKeycardCount() == highestKeycards) {
                winners.add(p);
            }
        }

        sb.append("\n");
        ArrayList<Player> survivors = getSurvivors();
        if (survivors.isEmpty()) {
            sb.append("No player escaped.\n");
        } else {
            sb.append("Escaped: ");
            for (int i = 0; i < survivors.size(); i++) {
                if (i > 0) sb.append(i == survivors.size() - 1 ? " and " : ", ");
                sb.append(survivors.get(i).getName());
            }
            sb.append("\n");
        }

        if (!winners.isEmpty() && highestKeycards > 0) {
            sb.append(winners.size() == 1 ? "Ultimate winner: " : "Ultimate winners: ");
            for (int i = 0; i < winners.size(); i++) {
                if (i > 0) sb.append(i == winners.size() - 1 ? " and " : ", ");
                sb.append(winners.get(i).getName());
            }
            sb.append(" with ").append(highestKeycards).append(highestKeycards == 1 ? " keycard." : " keycards.");
        } else {
            sb.append("No ultimate winner — nobody earned a keycard.");
        }

        return sb.toString();
    }

    // ── End Game ─────────────────────────────────────────────

    // Returns list of players who survived (alive and have a keycard)
    public ArrayList<Player> getSurvivors() {
        ArrayList<Player> survivors = new ArrayList<>();
        for (Player p : players) {
            if (p.isAlive() && p.hasKeycard()) survivors.add(p);
        }
        return survivors;
    }

    // Returns true if all three rounds have been completed
    public boolean isGameOver() {
        return currentRound > 3;
    }

    // ── Getters ──────────────────────────────────────────────

    public int getCurrentRound()          { return currentRound; }
    public AIPlayer getAI()               { return ai; }
    public ArrayList<Player> getPlayers() { return players; }
    public int getPacCount()              { return pacCount; }
    public int getDeckSize()              { return deck.size(); }
    public GameMode getGameMode()         { return gameMode; }
    public boolean isMultiplayer()        { return gameMode == GameMode.MULTIPLAYER; }

    // Returns all card values in a player's hand — used by UI to display opening cards
    public ArrayList<Integer> getPlayerOpeningCards(Player p) {
        ArrayList<Integer> values = new ArrayList<>();
        for (Card card : p.getHand()) {
            values.add(card.getValue());
        }
        return values;
    }

    // Returns the AI's hidden card value — called when revealing at round end
    public int getAIHiddenCard() {
        ArrayList<Card> aiHand = ai.getHand();
        if (aiHand.isEmpty()) {
            System.err.println("GameController: AI hand is empty — cannot reveal hidden card");
            return 0;
        }
        return aiHand.get(0).getValue(); // index 0 is always the hidden card
    }

    // Returns values of any extra cards AI drew after its opening card
    public ArrayList<Integer> getAIExtraCards() {
        ArrayList<Integer> values = new ArrayList<>();
        ArrayList<Card> aiHand = ai.getHand();
        for (int i = 1; i < aiHand.size(); i++) { // skip index 0 (hidden)
            values.add(aiHand.get(i).getValue());
        }
        return values;
    }

    // Returns the value of the last card the player drew — used by UI after hit
    public int getLastPlayerCard(Player p) {
        ArrayList<Card> hand = p.getHand();
        if (hand.isEmpty()) {
            System.err.println("GameController: " + p.getName() + "'s hand is empty — cannot get last card");
            return 0;
        }
        return hand.get(hand.size() - 1).getValue();
    }

    // ── Helpers ──────────────────────────────────────────────

    // Clears all hands at the start of a new round
    private void clearAllHands() {
        for (Player p : players) p.clearHand();
        ai.clearHand();
    }
}