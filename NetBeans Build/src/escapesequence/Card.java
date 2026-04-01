package escapesequence;

/**
 *
 * @author Akera Griffith & Kaitlyn Morris
 * Card Class
 */

public class Card {
    private int value;
    private boolean faceUp;
    
    public Card(int value, boolean faceUp) {
        this.value = value;
        this.faceUp = faceUp;
    }
    
    //Return Value
    public int getValue() {
        return value;
    }
    
    //True if face up
    public boolean isFaceUp() {
        return faceUp;
    }
    
    //Reveal the AI players card 
    public void flip() {
        faceUp = !faceUp;
    }
      
}
