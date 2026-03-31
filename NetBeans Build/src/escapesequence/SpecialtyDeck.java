package escapesequence;

/**
 *
 * @author Akera Griffith & Kaitlyn Morris
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
    
    //Adds sets of 5 until deck size exceeds the number of possible specialty cards
    private void buildDeck(int numPlayers) {
        int possible = (numPlayers + 1) * 2;
        int deckSize = 0;
        
        while (deckSize <= possible) {
            for (SpecialtyCard.Type type : SpecialtyCard.Type.values()) {
                cards.add(new SpecialtyCard(type));
            }
            deckSize += 5;
        }
    }
    
    //Randomizes the order of the specialty deck
    public void shuffle() {
         Collections.shuffle(cards);
    }
    
    //Removes and returns the top card or nullif the deck is empty 
    public SpecialtyCard drawCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards .remove(0); 
    }
    
    public int size () {
        return cards.size();
    }
    
}
