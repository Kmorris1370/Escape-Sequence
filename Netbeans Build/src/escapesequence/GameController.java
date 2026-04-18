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
        for (Player p : players) {
            if (p.isAlive()) {
                p.recieveCard(new Card(deck.drawCard(), true));
                p.recieveCard(new Card(deck.drawCard(), true));
            }
        }
        ai.recieveCard(new Card(deck.drawCard(), true));
        ai.recieveCard(new Card(deck.drawCard(), false));

        //Deal specialty cards in rounds 2 and 3
        if (currentRound >= 2) {
            specialtyDeck.dealToAll(players, ai);
        }
    }

    //After all players have taken their turn
    public void playAITurn() {
        ai.takeTurn(deck, players);
    }

    //Resolve outcomes for all players after AI turn
    public void resolveRound() {
        for (Player p : players) {
            if (!p.isAlive()) continue;
            RoundOutcome outcome = resolveOutcome(p);
            applyOutcome(p, outcome);
        }
    }

    //Advances to the next round 
    public void advanceRound() {
        if (currentRound < 3) {
            currentRound++;
            ai.advanceRound();
        }
    }

    //Give player a card if they decide to hit 
    public void playerHit(Player p) {
        if (playerCanHit(p)) {
            p.recieveCard(new Card(deck.drawCard(), true));
        }
    }
    
    //Skip player if they chise stay
    public void playerStand(Player p) {
        
    }

    //A check at the start of each player's turn
    public void startPlayerTurn(Player p) {
        if (p.isFrozen()) p.setFrozen(false);
    }

    //Returns true if player is allowed to hit 
    public boolean playerCanHit(Player p) {
        return p.isAlive() && !p.isFrozen() && !p.isBust();
    }

    //SPECIALTY CARD ACTIONS---------------------------------------------------------------------------------------
    
    //If the card just drawn caused the player the bust, remove that card
    public void applyShield(Player p) {
        ArrayList<Card> hand = p.getHand();
        if (!hand.isEmpty()) hand.remove(hand.size() - 1);
    }

    //When REVERSE is played the card the player just drew is subtracted instead of added 
    public void applyReverse(Player p) {
        int val = deck.drawCard();
        if (val != -1) p.recieveCard(new Card(-val, true));
    }
    
    //Freezes a player after a FREEZE card has been played on them
    public void applyFreeze(Player target) {
        target.setFrozen(true);
    }
    
    //Wild Card
    public void applyWild(Player p, int chosenValue) {
        if (chosenValue >= 1 && chosenValue <= 9 && p.hasWildCard()) {
            p.recieveCard(new Card(chosenValue, true));
        p.setHasWildCard(false);
        }
    }
    
    //After drawing a card, play SWAP to switch cards with any other visible card 
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

    //Returns the next card value without drawing 
    public int applyPeek() {
        return deck.peekCard();
    }
    
    //ROUND RESOLUTIONS------------------------------------------------------------------------------
    public enum RoundOutcome {
        WIN_WITH_PAC,
        PROCEED_NO_PAC,
        ELIMINATED,
        ALL_BUST_PROCEED
    }

    public RoundOutcome resolveOutcome(Player p) {
        boolean playerBust = p.isBust();
        boolean aiBust = ai.isBust();

        if (playerBust && aiBust)  return RoundOutcome.ALL_BUST_PROCEED; //AI busts
        if (playerBust && !aiBust) return RoundOutcome.ELIMINATED;       //Player busts

        //Neither bust 
        if (p.getHandTotal() >= ai.getHandTotal()) {
            return (pacCount < MAX_PACS) ? RoundOutcome.WIN_WITH_PAC : RoundOutcome.PROCEED_NO_PAC;
        }
        return RoundOutcome.PROCEED_NO_PAC; //Player does not bust but also does not win
    }

    private void applyOutcome(Player p, RoundOutcome outcome) {
        switch (outcome) {
            case WIN_WITH_PAC: //Continue & recieve keycard
                p.awardKeycard();
                pacCount++;
                break;
            case ELIMINATED: //Player is dead
                p.eliminate();
                break;
            case ALL_BUST_PROCEED: //Continue
            case PROCEED_NO_PAC: //Continue
                break;
        }
    }

    //GAME RESULT--------------------------------------------------------------------------------------------
   
    //Returns players who survived (alive + have keycard)
    public ArrayList<Player> getSurvivors() {
        ArrayList<Player> survivors = new ArrayList<>();
        for (Player p : players) {
            if (p.isAlive() && p.hasKeycard()) survivors.add(p);
        }
        return survivors;
    }
    
    //Game over check
    public boolean isGameOver() {
        return currentRound > 3;
    }

    //GETTERS-----------------------------------------------------------------------------------------------------
    public int getCurrentRound()          { return currentRound; }
    public AIPlayer getAI()               { return ai; }
    public ArrayList<Player> getPlayers() { return players; }
    public int getPacCount()              { return pacCount; }
    public int getDeckSize()              { return deck.size(); }
    private void clearAllHands() {
        for (Player p : players) p.clearHand();
        ai.clearHand();
    }
    
    //Returns player's opening hand values — UI uses this to display cards
    public ArrayList<Integer> getPlayerOpeningCards(Player p) {
        ArrayList<Integer> values = new ArrayList<>();
        for (Card card : p.getHand()) {
            values.add(card.getValue());
        }
        return values;
    }

    //Returns AI's face-up (visible) opening card value
    public int getAIVisibleCard() {
        return ai.getHand().get(0).getValue();
    }

    //Returns AI's face-down (hidden) opening card value
    public int getAIHiddenCard() {
        return ai.getHand().get(1).getValue();
    }

    //Returns any extra cards AI drew during its turn (index 2 onward)
    public ArrayList<Integer> getAIExtraCards() {
        ArrayList<Integer> values = new ArrayList<>();
        ArrayList<Card> aiHand = ai.getHand();
        for (int i = 2; i < aiHand.size(); i++) {
            values.add(aiHand.get(i).getValue());
        }
        return values;
    }

    //Returns the last card the player drew
    public int getLastPlayerCard(Player p) {
        ArrayList<Card> hand = p.getHand();
        return hand.get(hand.size() - 1).getValue();
    }

    //Returns outcome message string — keeps UI logic out of Round1
    public String getOutcomeMessage(RoundOutcome outcome) {
        switch (outcome) {
            case WIN_WITH_PAC:      return "You win! P.A.C. keycard earned.";
            case PROCEED_NO_PAC:    return "You advance — no keycard.";
            case ELIMINATED:        return "You have been eliminated.";
            case ALL_BUST_PROCEED:  return "Both bust! Everyone advances.";
            default:                return "";
        }
    }
}