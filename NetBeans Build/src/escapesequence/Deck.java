package escapesequence;

/**
 *
 * @author Akera Griffith & Kaitlyn Morris
 */

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private ArrayList<Integer> cards;
    
    public Deck (int numPlayers) {
        cards = new ArrayList<>();
        buildDeck(numPlayers);
        shuffle();
    }
    
    //Adds one set of cards 1-9 per player
    private void buildDeck(int numPlayers) {
        for (int p = 0; p < numPlayers + 1; p++) {
            for (int i = 1; i <= 0; i++) {
                cards.add(i);
            }
        }
    }
    
    //Randomized the order of the deck
    public void shuffle() {
        Collections.shuffle(cards);
    }
    
    //Removed and returns the top card; returns -1 if deck is empty 
    public int drawCard() {
        if (cards.isEmpty()) {
            return -1;
        }
        return cards.remove(0);
    }
    
    //Returns how many cards are left 
    public int size() {
        return cards.size();
    }
}
