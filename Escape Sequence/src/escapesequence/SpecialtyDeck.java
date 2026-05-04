package escapesequence;

/**
 * @author Akera Griffith & Kaitlyn Morris
 *  Class for the deck of specialty cards
 */

import java.util.ArrayList;
import java.util.Collections;

public class SpecialtyDeck {
    private ArrayList<SpecialtyCard> cards;

    public SpecialtyDeck(int numPlayers) {
        cards = new ArrayList<>();
        buildDeck(numPlayers);
        shuffle();
    }

    //1 player = 1 set (5 cards), 2-3 players = 2 sets, 4-6 players = 3 sets
    private int getSetsForPlayers(int numPlayers) {
        if (numPlayers == 1)      return 1;
        else if (numPlayers <= 3) return 2;
        else                      return 3;
    }

    private void buildDeck(int numPlayers) {
        int sets = getSetsForPlayers(numPlayers);
        for (int s = 0; s < sets; s++) {
            for (SpecialtyCard.Type type : SpecialtyCard.Type.values()) {
                if (type == SpecialtyCard.Type.WILD) continue; // WILD is earned, not dealt
                cards.add(new SpecialtyCard(type));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public SpecialtyCard drawCard() {
        if (cards.isEmpty()) return null;
        return cards.remove(0);
    }

    //Deals one specialty card to each alive player
    public void dealToAll(ArrayList<Player> players) {
        for (Player p : players) {
            if (p.isAlive()) {
                SpecialtyCard card = drawCard();
                if (card != null) p.receiveSpecialtyCard(card);
            }
        }
    }

    public int size() { return cards.size(); }
}