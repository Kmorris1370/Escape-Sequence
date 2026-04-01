package escapesequence;

/**
 *
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
    
    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.specialtyCards = new ArrayList<>();
        this.hasKeycard = false;
        this.isAlive = true;
        this.isFrozen = false;
    }
    
    //Get Name
    public String getName() {
        return name;
    }
    
    //True if has keycard
    public boolean hasKeycard() {
        return hasKeycard;
    }
    
    //True if player is alive
    public boolean isAlive(){
        return isAlive; 
    }
    
    //True if player is dead
    public void eliminate() {
        isAlive = false;
    }
    
    //True if players hand is over 21
    public boolean isBust() {
        return getHandTotal() > 21;
    }
    
    //True if the player has been frozen
    public boolean isFrozen() {
        return isFrozen;
    }
    
    //Freeze player after they've been frozen
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
    
    //Returns specialty cards
    public ArrayList<SpecialtyCard> getSpecialtyCards(){
        return specialtyCards;
    }
    
    //Return true if player has specialty cards
    public boolean hasSpecialtyCards() {
        return !specialtyCards.isEmpty();
    }
    
    //Gives player a keycard
    public void awardKeycard() {
        hasKeycard = true;
    }
}
