package escapesequence;

/**
 *
 * @author Akera Griffith & Kaitlyn Morris
 */

import java.util.ArrayList;

public class Player {
    private String name;
    private ArrayList<Card> hand;
    private ArrayList<SpecialtyCard> specialtyCards;
    private boolean hasKeycard;
    private boolean isAlive;
    private boolean isFrozen;
    
    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.specialtyCards = new ArrayList<>();
        this.hasKeycard = false;
        this.isAlive = true;
        this.isFrozen = false;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean hasKeycard() {
        return hasKeycard;
    }
    
    public boolean isAlive(){
        return isAlive; 
    }
    
    public void eliminate() {
        isAlive = false;
    }
    
    public boolean isFrozen() {
        return isFrozen;
    }
    
    public void setFrozen(boolean frozen) {
        isFrozen = frozen;
    }
    
    //Adds a card to the player's hand
    public void recieveCard(Card card) {
        hand.add(card);
    }
    
    //Calculate the player's current hand total
    public int getHandTotal() {
        int total = 0;
        for (Card card : hand) {
            total += card.getValue();
        }
        return total;
    }
    
    public ArrayList<Card> getHand() {
        return hand;
    }
    
    //Clears the hand at the start of a new round
    public void clearHand() {
        hand.clear();
    }
    
    //Adds a specialty card to the player's held cards
    public void recieveSpecialtyCard(SpecialtyCard card) {
        specialtyCards.add(card);        
    }
    
    //Removes and returns a specialty card by index when used 
    public SpecialtyCard useSpecialtyCard(int index) {
        if (index >= 0 && index < specialtyCards.size()) {
            return specialtyCards.remove(index);
        }
        return null;
    }
    
    public ArrayList<SpecialtyCard> getSpecialtyCards(){
        return specialtyCards;
    }
    
    public boolean hasSpecialtyCards() {
        return !specialtyCards.isEmpty();
    }
    
    @Override
    public String toString(){
        return name + " | Total: " + getHandTotal() + " | Keycard: " + hasKeycard + " | Alive: " + isAlive;
    }
}
