package escapesequence;

/**
 * @author Akera Griffith & Kaitlyn Morris
 * Class for the specialty cards
 */

public class SpecialtyCard {
    public enum Type {
        SHIELD, REVERSE, FREEZE, SWAP, PEEK
        // WILD is not in the deck — awarded when a player earns 2 PACs
    }

    private Type type;

    public SpecialtyCard(Type type) {
        this.type = type;
    }

    public Type getType() { return type; }

    @Override
    public String toString() {
        switch (type) {
            case SHIELD:  return "Shield  - Return your last drawn card";
            case REVERSE: return "Reverse - Subtract your last drawn card";
            case FREEZE:  return "Freeze  - Skip a chosen player's next turn";
            case SWAP:    return "Swap    - Exchange a card with the dealer";
            case PEEK:    return "Peek    - View next card in the deck";
            default:      return "Unknown";
        }
    }
}