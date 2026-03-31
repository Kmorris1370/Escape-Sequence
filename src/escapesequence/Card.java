package escapesequence;

/**
 *
 * @author Akera Griffith & Kaitlyn Morris
 */

public class Card {
    private int value;
    private boolean faceUp;
    
    public Card(int value, boolean faceUp) {
        this.value = value;
        this.faceUp = faceUp;
    }
    
    public int getValue() {
        return value;
    }
    
    public boolean isFaceUp() {
        return faceUp;
    }
    
    //Reveal the AI players card 
    public void flip() {
        faceUp = !faceUp;
    }
    
    //For displaying cards in UI 
    //Returns the value if face-up, otherwise hides it
    @Override
    public String toString(){
        if (faceUp) {
            return String.valueOf(value);
        } else {
            return "[?]";
        }
    }
}
