package escapesequence;

/**
 *
 * @author Akera Griffith & Kaitlyn Morris
 */

public class SpecialtyCard {

    public enum Type {
        SHIELD, WILD, REVERSE, FREEZE, SWAP, PEEK
    }
    
    private Type type;
    
    public SpecialtyCard(Type type) {
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }
    
    //Returns the cards name effect for display purposes
    @Override
    public String toString() {
        switch (type) {
            case SHIELD:  return "Shield  - Return your last drawn card";
            case WILD:    return "Wild    - Choose your cards value";
            case REVERSE: return "Reverse - Subtract your last drawn card";
            case FREEZE:  return "Freeze  - Skip a chosen player's next turn";
            case SWAP:    return "Swap    - Exchange a card with the dealer";
            case PEEK:    return "Peek    - View next card in the deck"; 
            default:      return "Unknown";
        }
    }
}
