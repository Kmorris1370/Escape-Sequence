package escapesequence;

/**
 * @author Akera Griffith & Kaitlyn Morris
 *  Class that controls game flow
 */
import java.util.ArrayList;

public class GameController {
    private ArrayList<Player> players;
    private AIPlayer ai;
    private Deck deck;
    private SpecialtyDeck specialtyDeck;
    private int currentRound;
    private int pacCount;

    private static final int MAX_PACS = 3;
    
    //Constructor
    public GameController(ArrayList<Player> players) {
        this.players = players;
        this.ai = new AIPlayer();
        this.deck = new Deck(players.size());
        this.specialtyDeck = new SpecialtyDeck(players.size());
        this.currentRound = 1;
        this.pacCount = 0;
    }

    //Called by UI to start the current round
    public void startRound() {
        clearAllHands();

        //Deal 2 cards to each alive player and AI
        for (Player p : players) {
            if (p.isAlive()) {
                p.recieveCard(new Card(deck.drawCard(), true));
                p.recieveCard(new Card(deck.drawCard(), true));
            }
        }
        ai.recieveCard(new Card(deck.drawCard(), true));
        ai.recieveCard(new Card(deck.drawCard(), false)); // one AI card hidden

    }

    //Called by UI after all players have taken their turn
    public void playAITurn() {
        ai.takeTurn(deck, players);
    }

    //Called by UI to resolve outcomes for all players after AI turn
    public void resolveRound() {
        for (Player p : players) {
            if (!p.isAlive()) continue;
            RoundOutcome outcome = resolveOutcome(p);
            applyOutcome(p, outcome);
        }
    }

    //Advances to the next round — called by UI after showing results
    public void advanceRound() {
        if (currentRound < 3) {
            currentRound++;
            ai.advanceRound();
        }
    }


    public void playerHit(Player p) {
        if (playerCanHit(p)) {
            p.recieveCard(new Card(deck.drawCard(), true));
        }
    }

    public void playerStand(Player p) {
        //No state change needed — UI stops offering hit/stand
    }

    //Called by UI at the start of each player's turn
    public void startPlayerTurn(Player p) {
        if (p.isFrozen()) p.setFrozen(false);
    }

    //Returns true if player is allowed to hit — used by UI to enable/disable hit button
    public boolean playerCanHit(Player p) {
        return p.isAlive() && !p.isFrozen() && !p.isBust();
    }

    //── Specialty Card Actions (called by UI) ────────────────

    public void applyShield(Player p) {
        ArrayList<Card> hand = p.getHand();
        if (!hand.isEmpty()) hand.remove(hand.size() - 1);
    }

    public void applyReverse(Player p) {
        int val = deck.drawCard();
        if (val != -1) p.recieveCard(new Card(-val, true));
    }

    public void applyFreeze(Player target) {
        target.setFrozen(true);
    }

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

    // Returns the next card value without drawing — UI displays it to player
    public int applyPeek() {
        return deck.peekCard();
    }

    public enum RoundOutcome {
        WIN_WITH_PAC,
        PROCEED_NO_PAC,
        ELIMINATED,
        ALL_BUST_PROCEED
    }

    public RoundOutcome resolveOutcome(Player p) {
        boolean playerBust = p.isBust();
        boolean aiBust = ai.isBust();

        if (playerBust && aiBust)  return RoundOutcome.ALL_BUST_PROCEED;
        if (playerBust && !aiBust) return RoundOutcome.ELIMINATED;

        // Neither bust — compare scores
        if (p.getHandTotal() >= ai.getHandTotal()) {
            return (pacCount < MAX_PACS) ? RoundOutcome.WIN_WITH_PAC : RoundOutcome.PROCEED_NO_PAC;
        }
        return RoundOutcome.PROCEED_NO_PAC;
    }

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

    // ── End Game ─────────────────────────────────────────────

    // Returns players who survived (alive + have keycard)
    public ArrayList<Player> getSurvivors() {
        ArrayList<Player> survivors = new ArrayList<>();
        for (Player p : players) {
            if (p.isAlive() && p.hasKeycard()) survivors.add(p);
        }
        return survivors;
    }

    public boolean isGameOver() {
        return currentRound > 3;
    }

    // ── Getters (for UI) ─────────────────────────────────────

    public int getCurrentRound()          { return currentRound; }
    public AIPlayer getAI()               { return ai; }
    public ArrayList<Player> getPlayers() { return players; }
    public int getPacCount()              { return pacCount; }
    public int getDeckSize()              { return deck.size(); }

    // ── Helpers ──────────────────────────────────────────────

    private void clearAllHands() {
        for (Player p : players) p.clearHand();
        ai.clearHand();
    }
}