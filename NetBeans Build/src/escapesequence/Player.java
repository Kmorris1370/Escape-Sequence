package escapesequence;

/**
 * @author Akera Griffith & Kaitlyn Morris
 * Player Class
 */

import java.util.ArrayList;

public class Player {
    private String name;
    private ArrayList<Card> hand;
    private ArrayList<SpecialtyCard> specialtyCards;
    private boolean hasKeycard;
    private boolean isAlive;
    private boolean isFrozen;
    private boolean hasWildCard;
    private int keycardCount;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.specialtyCards = new ArrayList<>();
        this.hasKeycard = false;
        this.isAlive = true;
        this.isFrozen = false;
        this.hasWildCard = false;
        this.keycardCount = 0;
    }

    public String getName()                    { return name; }
    public boolean hasKeycard()                { return hasKeycard; }
    public boolean isAlive()                   { return isAlive; }
    public boolean isFrozen()                  { return isFrozen; }
    public boolean hasWildCard()               { return hasWildCard; }
    public int getKeycardCount()               { return keycardCount; }
    public ArrayList<Card> getHand()           { return hand; }
    public ArrayList<SpecialtyCard> getSpecialtyCards() { return specialtyCards; }

    public void setFrozen(boolean frozen)      { isFrozen = frozen; }
    public void setHasWildCard(boolean val)    { hasWildCard = val; }

    public void eliminate()                    { isAlive = false; }

    public boolean isBust()                    { return getHandTotal() > 21; }

    //Awards keycard, tracks count, auto-awards Wild on 2nd keycard
    public void awardKeycard() {
        hasKeycard = true;
        keycardCount++;
        if (keycardCount == 2) hasWildCard = true;
    }

    public void recieveCard(Card card)         { hand.add(card); }

    public int getHandTotal() {
        int total = 0;
        for (Card card : hand) total += card.getValue();
        return total;
    }

    public void clearHand()                    { hand.clear(); }

    public void recieveSpecialtyCard(SpecialtyCard card) {
        specialtyCards.add(card);
    }

    public SpecialtyCard useSpecialtyCard(int index) {
        if (index >= 0 && index < specialtyCards.size()) {
            return specialtyCards.remove(index);
        }
        return null;
    }

    public boolean hasSpecialtyCards()         { return !specialtyCards.isEmpty(); }

    @Override
    public String toString() {
        return name + " | Total: " + getHandTotal() + " | Keycard: " + hasKeycard + " | Alive: " + isAlive;
    }
}