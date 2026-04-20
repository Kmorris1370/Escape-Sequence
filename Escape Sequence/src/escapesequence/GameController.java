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

        // Rebuild and shuffle deck each round to prevent running out of cards
        deck = new Deck(players.size());
        deck.shuffle();

        System.out.println("=== ROUND " + currentRound + " START ===");
        System.out.println("Deck size: " + deck.size());

        // Deal one opening card to each alive player
        for (Player p : players) {
            if (p.isAlive()) {
                p.recieveCard(new Card(deck.drawCard(), true));
                System.out.println(p.getName() + " opening hand: " + p.getHandTotal());
            }
        }

        // Deal AI's one hidden card
        ai.recieveCard(new Card(deck.drawCard(), false));
        System.out.println("AI opening card (hidden): " + ai.getHand().get(0).getValue());

        // Deal specialty cards starting round 2
        if (currentRound >= 2) {
            specialtyDeck.dealToAll(players, ai);
        }
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
            p.recieveCard(new Card(val, true));
            System.out.println(p.getName() + " hit — new total: " + p.getHandTotal());
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

    // REVERSE — draws a card and subtracts its value instead of adding
    public void applyReverse(Player p) {
        int val = deck.drawCard();
        if (val != -1) p.recieveCard(new Card(-val, true));
    }

    // FREEZE — prevents target from hitting on their next turn
    public void applyFreeze(Player target) {
        target.setFrozen(true);
    }

    // WILD — lets player choose the value of their next card
    public void applyWild(Player p, int chosenValue) {
        if (chosenValue >= 1 && chosenValue <= 9 && p.hasWildCard()) {
            p.recieveCard(new Card(chosenValue, true));
            p.setHasWildCard(false);
        }
    }

    // SWAP — exchanges one of the player's cards with a visible AI card
    public void applySwap(Player p, int playerCardIndex, int aiCardIndex) {
        ArrayList<Card> pHand = p.getHand();
        ArrayList<Card> aiHand = ai.getHand();
        if (playerCardIndex < pHand.size() && aiCardIndex < aiHand.size()) {
            if (aiHand.get(aiCardIndex).isFaceUp()) {
                Card temp = pHand.get(playerCardIndex);
                pHand.set(playerCardIndex, aiHand.get(aiCardIndex));
                aiHand.set(aiCardIndex, temp);
            }
        }
    }

    // PEEK — returns the next card in the deck without drawing it
    public int applyPeek() {
        return deck.peekCard();
    }

    // ── Outcome Resolution ───────────────────────────────────

    // Determines a player's outcome based on their total vs AI total
    public RoundOutcome resolveOutcome(Player p) {
        System.out.println("--- RESOLVING OUTCOME ---");
        System.out.println(p.getName() + " total: " + p.getHandTotal());
        System.out.println("AI total: " + ai.getHandTotal());
        System.out.println("Player bust: " + p.isBust());
        System.out.println("AI bust: " + ai.isBust());

        boolean playerBust = p.isBust();
        boolean aiBust = ai.isBust();

        if (playerBust && aiBust)  return RoundOutcome.ALL_BUST_PROCEED;
        if (playerBust && !aiBust) return RoundOutcome.ELIMINATED;

        // Neither bust — whoever is closest to 21 wins
        if (p.getHandTotal() >= ai.getHandTotal()) {
            return (pacCount < MAX_PACS) ? RoundOutcome.WIN_WITH_PAC : RoundOutcome.PROCEED_NO_PAC;
        }
        return RoundOutcome.PROCEED_NO_PAC;
    }

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
        return ai.getHand().get(0).getValue(); // index 0 is always the hidden card
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
        return hand.get(hand.size() - 1).getValue();
    }

    // ── Helpers ──────────────────────────────────────────────

    // Clears all hands at the start of a new round
    private void clearAllHands() {
        for (Player p : players) p.clearHand();
        ai.clearHand();
    }
}