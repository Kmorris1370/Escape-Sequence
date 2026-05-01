package escapesequence;

/**
 * @author Akera Griffith & Kaitlyn Morris
 * AI Player Class
 */

import java.util.ArrayList;

public class AIPlayer extends Player {
    private int round;

    private static final int ROUND_1_THRESHOLD = 17;
    private static final int ROUND_2_THRESHOLD = 15;
    private static final int ROUND_3_THRESHOLD = 13;

    public AIPlayer() {
        super("The System");
        this.round = 1;
    }

    public void advanceRound() {
        if (round < 3) round++;
    }

    public int getRound() { return round; }

    public boolean shouldHit() {
        int total = getHandTotal();
        switch (round) {
            case 1: return total < ROUND_1_THRESHOLD;
            case 2: return total < ROUND_2_THRESHOLD;
            case 3: return total < ROUND_3_THRESHOLD;
            default: return false;
        }
    }

    // AI holds specialty cards until Round 3 only
    public boolean shouldUseSpecialtyCard() {
        return round == 3 && hasSpecialtyCards();
    }

    //Full turn: handles frozen state, specialty cards, and hit/stand
    public void takeTurn(Deck deck, ArrayList<Player> opponents) {
        if (isFrozen()) {
            setFrozen(false);
            return;
        }

        //Pre-draw: PEEK or FREEZE before hitting
        if (shouldUseSpecialtyCard()) {
            useSpecialtyCardStrategically(deck, opponents, false);
        }

        while (shouldHit()) {
            int drawnValue = deck.drawCard();
            if (drawnValue == -1) break;

            receiveCard(new Card(drawnValue, false));

            //Post-draw: SHIELD or REVERSE if busting
            if (shouldUseSpecialtyCard()) {
                useSpecialtyCardStrategically(deck, opponents, true);
            }
        }
    }

    //postDraw = true  → react to bust (SHIELD, REVERSE)
    //postDraw = false → proactive use (PEEK, FREEZE)
    private void useSpecialtyCardStrategically(Deck deck, ArrayList<Player> opponents, boolean postDraw) {
        ArrayList<SpecialtyCard> held = getSpecialtyCards();

        for (int i = 0; i < held.size(); i++) {
            SpecialtyCard.Type type = held.get(i).getType();

            if (postDraw && isBust()) {
                if (type == SpecialtyCard.Type.SHIELD) {
                    useSpecialtyCard(i);
                    applyShield();
                    return;
                }
                if (type == SpecialtyCard.Type.REVERSE) {
                    useSpecialtyCard(i);
                    applyReverse(deck);
                    return;
                }
            }

            if (!postDraw) {
                if (type == SpecialtyCard.Type.PEEK) {
                    useSpecialtyCard(i);
                    applyPeek(deck);
                    return;
                }
                if (type == SpecialtyCard.Type.FREEZE) {
                    Player target = getMostThreateningPlayer(opponents);
                    if (target != null) {
                        useSpecialtyCard(i);
                        target.setFrozen(true);
                        return;
                    }
                }
            }
        }
    }

    //Removes the last drawn card
    private void applyShield() {
        ArrayList<Card> hand = getHand();
        if (!hand.isEmpty()) hand.remove(hand.size() - 1);
    }

    //Draws a card and subtracts its value
    private void applyReverse(Deck deck) {
        int val = deck.drawCard();
        if (val != -1) receiveCard(new Card(-val, false));
    }

    //Peeks at next card — if it would bust, AI stands naturally
    private void applyPeek(Deck deck) {
        int nextValue = deck.peekCard();
        //If next card would bust, shouldHit() will return false after this
        //since we don't draw here — AI effectively stands
    }

    //Returns alive non-frozen opponent with highest total
    private Player getMostThreateningPlayer(ArrayList<Player> opponents) {
        Player target = null;
        int highest = -1;
        for (Player p : opponents) {
            if (p.isAlive() && !p.isFrozen() && p.getHandTotal() > highest) {
                highest = p.getHandTotal();
                target = p;
            }
        }
        return target;
    }
}

