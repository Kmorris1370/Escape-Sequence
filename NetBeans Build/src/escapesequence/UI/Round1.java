
package escapesequence.UI;

/**
 * @author Akera Griffith & Kaitlyn Morris
 * Escape Sequence Round 1 Interface
 */

import javax.swing.*;
import escapesequence.*;
import java.util.ArrayList;

public class Round1 extends javax.swing.JFrame {

    private int index = 0; 
    private javax.swing.JLabel[] cardSlots;
    private javax.swing.JLabel[] aiCardSlots;
    private int aiIndex = 0;
     private int aiHiddenCardIndex = 0;
    
    private GameController gameController;
    private Player player;
    
    /** Creates new form Round1 */
    public Round1() {
        setSize(1200,700);
        initComponents();
        initCardSlots();  
        initAICardSlots();
        
        //Hide all card slots initially
        for (JLabel slot : cardSlots) slot.setVisible(false);
        for (JLabel slot : aiCardSlots) slot.setVisible(false);
        
        //Set images
        backgroundLabel.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/Round1.jpg",1200,700));
        pauseButton.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/Pause.png",40,40));
        deckLabel.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/BackOfCard.jpg",50,50));
        
        //Set up game
        setupGame();
        startRound();

        //Wire buttons
        hitButton.addActionListener(e -> onHit());
        stayButton.addActionListener(e -> onStay());
    }
    
    //Initalizing
    private void setupGame() {
        //Create single player
        player = new Player("Player 1");
        ArrayList<Player> players = new ArrayList<>();
        players.add(player);

        //Create controller
        gameController = new GameController(players);

        //Set player name label
        p1Label.setText(player.getName());
    }
    
    //Deal the first cards
    private void startRound() {
        resetCardDisplay();
        gameController.startRound();

        //Display player's 2 opening cards
        for (Card card : player.getHand()) {
            addCardToDisplay(card.getValue());
        }

        //Display AI's opening cards
        int visibleCard = gameController.getAI().getHand().get(0).getValue();
        int hiddenCard  = gameController.getAI().getHand().get(1).getValue();
        dealAIOpeningCards(visibleCard, hiddenCard);

        //Update hit button state
        hitButton.setEnabled(gameController.playerCanHit(player));
    }
    
    //Hit Function
    private void onHit() {
        gameController.playerHit(player);

        //Display the card just drawn 
        Card drawn = player.getHand().get(player.getHand().size() - 1);
        addCardToDisplay(drawn.getValue());

        //Disable hit if bust
        hitButton.setEnabled(gameController.playerCanHit(player));

    }

    //Stay Function
    private void onStay() {
        hitButton.setEnabled(false);
        stayButton.setEnabled(false);

        //AI plays its turn
        gameController.playAITurn();

        //Display any cards AI drew
        ArrayList<Card> aiHand = gameController.getAI().getHand();
        for (int i = 2; i < aiHand.size(); i++) {
            addAICardToDisplay(aiHand.get(i).getValue());
        }

        //Reveal AI hidden card
        revealAIHiddenCard();

        //Resolve outcome
        gameController.resolveRound();
        GameController.RoundOutcome outcome = gameController.resolveOutcome(player);
        showOutcome(outcome);
    }
    
    //Outcome Message
    private void showOutcome(GameController.RoundOutcome outcome) {
        String message;
        switch (outcome) {
            case WIN_WITH_PAC:
                message = "You win! P.A.C. keycard earned.";
                break;
            case PROCEED_NO_PAC:
                message = "You advance — no keycard.";
                break;
            case ELIMINATED:
                message = "You have been eliminated.";
                break;
            case ALL_BUST_PROCEED:
                message = "Both bust! Everyone advances.";
                break;
            default:
                message = "";
        }
        JOptionPane.showMessageDialog(this, message);

        //Navigate to Round 2
        gameController.advanceRound();
        Round2 round2 = new Round2(gameController);
        round2.setVisible(true);
        dispose();
    }
    
    //Player 1 card slots
    private void initCardSlots() {
        cardSlots = new javax.swing.JLabel[]{
            p1Card1, p1Card2, p1Card3, p1Card4, p1Card5, p1Card6, p1Card7, p1Card8, p1Card9
        };           
    }
    
    //AI player card slots
    private void initAICardSlots() {
        aiCardSlots = new javax.swing.JLabel[]{
            aiCard1, aiCard2, aiCard3, aiCard4, aiCard5, aiCard6, aiCard7, aiCard8, aiCard9
        };
    }
    
    //Show player card after hit
    public void addCardToDisplay(int cardValue) {
        cardSlots[index].setVisible(true);
        cardSlots[index].setIcon(ResourceLoader.loadCardImage(cardValue));

        //Shift to next card slot
        if (index < 8) {
            index++;
        }
    }
    
    //AI's opening deal
    public void dealAIOpeningCards(int visibleCardValue, int hiddenCardValue) {
        aiIndex = 0;
        aiHiddenCardIndex = 1;

        //First card is hidden
        aiCardSlots[0].setVisible(true);
        aiCardSlots[0].setIcon(
            ResourceLoader.loadImageScaled("/assets/pictures/BackOfCard.jpg", 50, 50));

        //Second card is visible
        aiCardSlots[1].setVisible(true);
        aiCardSlots[1].setIcon(ResourceLoader.loadCardImage(visibleCardValue));

        aiIndex = 2; //next AI card goes to slot 2
    }

    //Show AI player card after hit
    public void addAICardToDisplay(int cardValue) {      
        aiCardSlots[aiIndex].setVisible(true);
        aiCardSlots[aiIndex].setIcon(ResourceLoader.loadCardImage(cardValue));
        if (aiIndex < 8) {
            aiIndex++;
        }
    }

    //Reveal AI players hidden card 
    public void revealAIHiddenCard() {
        int hiddenValue = gameController.getAI().getHand().get(1).getValue();
        aiCardSlots[aiHiddenCardIndex].setIcon(ResourceLoader.loadCardImage(hiddenValue));
    }
    
    //Reset all the labels and icons
    public void resetCardDisplay() {
        index = 0;
        for (JLabel slot : cardSlots) {
            slot.setIcon(null);
            slot.setVisible(false);
        }
        aiIndex = 0;
        for (javax.swing.JLabel slot : aiCardSlots) {
            slot.setIcon(null);
            slot.setVisible(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        deckLabel = new javax.swing.JLabel();
        pauseButton = new javax.swing.JButton();
        aiCard1 = new javax.swing.JLabel();
        p1Label = new javax.swing.JLabel();
        hitButton = new javax.swing.JButton();
        stayButton = new javax.swing.JButton();
        aiCard6 = new javax.swing.JLabel();
        aiCard7 = new javax.swing.JLabel();
        aiCard9 = new javax.swing.JLabel();
        p1Card1 = new javax.swing.JLabel();
        p1Card4 = new javax.swing.JLabel();
        aiCard8 = new javax.swing.JLabel();
        p1Card5 = new javax.swing.JLabel();
        p1Card6 = new javax.swing.JLabel();
        p1Card7 = new javax.swing.JLabel();
        p1Card8 = new javax.swing.JLabel();
        p1Card3 = new javax.swing.JLabel();
        p1Card2 = new javax.swing.JLabel();
        p1Card9 = new javax.swing.JLabel();
        aiCard5 = new javax.swing.JLabel();
        aiCard4 = new javax.swing.JLabel();
        aiCard3 = new javax.swing.JLabel();
        aiCard2 = new javax.swing.JLabel();
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(new java.awt.Dimension(590, 300));

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        deckLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        deckLabel.setOpaque(true);
        jPanel1.add(deckLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 240, 50, 50));

        pauseButton.setBackground(new java.awt.Color(0, 0, 0, 0));
        pauseButton.setContentAreaFilled(false);
        pauseButton.setBorder(null);
        pauseButton.setFocusPainted(false);
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });
        jPanel1.add(pauseButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 20, 40, 40));

        aiCard1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard1.setOpaque(true);
        jPanel1.add(aiCard1, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 20, 50, 50));

        p1Label.setFont(FontLoader.getVT323(30f));
        p1Label.setForeground(new java.awt.Color(255, 255, 255));
        p1Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        p1Label.setPreferredSize(new java.awt.Dimension(70, 20));
        jPanel1.add(p1Label, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 520, 210, 30));

        hitButton.setBackground(new java.awt.Color(0, 0, 0, 100));
        hitButton.setFont(FontLoader.getVT323(40f));
        hitButton.setForeground(new java.awt.Color(255, 255, 255));
        hitButton.setContentAreaFilled(false);
        hitButton.setText("Hit");
        hitButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black));
        hitButton.setFocusPainted(false);
        jPanel1.add(hitButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 560, 180, 80));

        stayButton.setBackground(new java.awt.Color(0, 0, 0, 100));
        stayButton.setFont(FontLoader.getVT323(40f));
        stayButton.setContentAreaFilled(false);
        stayButton.setForeground(new java.awt.Color(255, 255, 255));
        stayButton.setText("Stay");
        stayButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black, java.awt.Color.black));
        stayButton.setFocusPainted(false);
        jPanel1.add(stayButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 560, 180, 80));

        aiCard6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard6.setOpaque(true);
        jPanel1.add(aiCard6, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 90, 50, 50));

        aiCard7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard7.setOpaque(true);
        jPanel1.add(aiCard7, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 160, 50, 50));

        aiCard9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard9.setOpaque(true);
        jPanel1.add(aiCard9, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 160, 50, 50));

        p1Card1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card1.setOpaque(true);
        jPanel1.add(p1Card1, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 320, 50, 50));

        p1Card4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card4.setOpaque(true);
        jPanel1.add(p1Card4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 390, 50, 50));

        aiCard8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard8.setOpaque(true);
        jPanel1.add(aiCard8, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 160, 50, 50));

        p1Card5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card5.setOpaque(true);
        jPanel1.add(p1Card5, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 390, 50, 50));

        p1Card6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card6.setOpaque(true);
        jPanel1.add(p1Card6, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 390, 50, 50));

        p1Card7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card7.setOpaque(true);
        jPanel1.add(p1Card7, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 460, 50, 50));

        p1Card8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card8.setOpaque(true);
        jPanel1.add(p1Card8, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 460, 50, 50));

        p1Card3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card3.setOpaque(true);
        jPanel1.add(p1Card3, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 320, 50, 50));

        p1Card2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card2.setOpaque(true);
        jPanel1.add(p1Card2, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 320, 50, 50));

        p1Card9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        p1Card9.setOpaque(true);
        jPanel1.add(p1Card9, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 460, 50, 50));

        aiCard5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard5.setOpaque(true);
        jPanel1.add(aiCard5, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 90, 50, 50));

        aiCard4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard4.setOpaque(true);
        jPanel1.add(aiCard4, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 90, 50, 50));

        aiCard3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard3.setOpaque(true);
        jPanel1.add(aiCard3, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 20, 50, 50));

        aiCard2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        aiCard2.setOpaque(true);
        jPanel1.add(aiCard2, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 20, 50, 50));
        jPanel1.add(backgroundLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1200, 700));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Pause
    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
        // TODO add your handling code here:
        PauseScreen pause = new PauseScreen();
        pause.setVisible(true);
    }//GEN-LAST:event_pauseButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Round1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Round1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Round1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Round1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Round1().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aiCard1;
    private javax.swing.JLabel aiCard2;
    private javax.swing.JLabel aiCard3;
    private javax.swing.JLabel aiCard4;
    private javax.swing.JLabel aiCard5;
    private javax.swing.JLabel aiCard6;
    private javax.swing.JLabel aiCard7;
    private javax.swing.JLabel aiCard8;
    private javax.swing.JLabel aiCard9;
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JLabel deckLabel;
    private javax.swing.JButton hitButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel p1Card1;
    private javax.swing.JLabel p1Card2;
    private javax.swing.JLabel p1Card3;
    private javax.swing.JLabel p1Card4;
    private javax.swing.JLabel p1Card5;
    private javax.swing.JLabel p1Card6;
    private javax.swing.JLabel p1Card7;
    private javax.swing.JLabel p1Card8;
    private javax.swing.JLabel p1Card9;
    private javax.swing.JLabel p1Label;
    private javax.swing.JButton pauseButton;
    private javax.swing.JButton stayButton;
    // End of variables declaration//GEN-END:variables

}
