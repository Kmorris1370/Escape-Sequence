package escapesequence;

/**
 *
 * @author Akera Griffith & Kaitlyn Morris
 */

public class AIPlayer extends Player {
    private int round;
    
    //Threshold per round
    private static final int ROUND_1_THRESHOLD = 17;
    private static final int ROUND_2_THRESHOLD = 15;
    private static final int ROUND_3_THRESHOLD = 13;
    
    public AIPlayer() {
        super("The System");
        this.round = 1;
    }
    
    public void advanceRound() {
        if (round < 3) {
            round++;
        }
    }
    
    public int getRound() {
        return round;
    }
    
    //Return true is AI should hit based on the round threshhold 
    public boolean shouldHit() {
        int total = getHandTotal();
        switch(round) {
            case 1: return total < ROUND_1_THRESHOLD;
            case 2: return total < ROUND_2_THRESHOLD;
            case 3: return total < ROUND_3_THRESHOLD;
            default: return false;
        }
    }
    
    //Returns true if AI should use specialty card 
    public boolean shouldUseSpecialtyCard() { 
        return round == 3 && hasSpecialtyCards();
    }
}
