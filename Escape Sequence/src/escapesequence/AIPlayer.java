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

    //Full turn: AI plays the standard hit/stand strategy with no specialty cards
    public void takeTurn(Deck deck, ArrayList<Player> opponents) {
        if (isFrozen()) {
            setFrozen(false);
            return;
        }

        while (shouldHit()) {
            int drawnValue = deck.drawCard();
            if (drawnValue == -1) break;
            receiveCard(new Card(drawnValue, true));
        }
    }
}
