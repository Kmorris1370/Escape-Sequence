package escapesequence.UI;

/**
 * @author Akera Griffith & Kaitlyn Morris
 * Escape Sequence Rules Interface
 */


import escapesequence.*;

public class RulesScreen extends javax.swing.JFrame {

    public enum Source { MAIN_MENU, SINGLE_PLAYER, PAUSE, MULTIPLAYER }
    private Source source;
    private String playerName;
    private GameController gameController;
    
    public RulesScreen(Source source, String playerName) {
        this(source, playerName, null);
    }
    
    public RulesScreen(Source source, String playerName, GameController gameController) {
        this.source = source;
        this.playerName = playerName;
        this.gameController = gameController;
        initComponents();
        jPanel1.setComponentZOrder(backgroundLabel, jPanel1.getComponentCount() - 1);
        int sw = 1200, sh = 700;
        java.awt.Dimension fixed = new java.awt.Dimension(sw, sh);
        jPanel1.setPreferredSize(fixed);
        jPanel1.setMinimumSize(fixed);
        backgroundLabel.setBounds(0, 0, sw, sh);
        setSize(sw, sh);
        setResizable(false);
        setLocationRelativeTo(null);
        SoundManager.enableButtonSounds(this.getContentPane());
        tutorialPane.setContentType("text/html");
        tutorialPane.setEditable(false);
        tutorialPane.setText(tutorialHTML);
        tutorialPane.setCaretPosition(0);
        backgroundLabel.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/TitleScreen.png", sw, sh));
        backButton.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/BackArrow.png",60,60));
        
        jScrollPane1.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new java.awt.Color(70, 70, 70);   
                this.trackColor = new java.awt.Color(20, 20, 20);    
            }
        });
        
        if (source == Source.MAIN_MENU || source == Source.PAUSE) {
            continueButton.setVisible(false);
        }
        
    }
    
    String tutorialHTML =
    "<html>" +
    "<body style='" +
        "background-color: #0a0a0f;" +
        "color: #c8d6e5;" +
        "font-family: VT323;" +  
        "padding: 24px;" +
        "margin: 0;" +
    "'>" +

    // ── Title ──────────────────────────────────────────────
    "<h1 style='" +
        "color: #ffffff;" +
        "font-size: 28px;" +
        "letter-spacing: 6px;" +
        "text-align: center;" +
        "margin-bottom: 2px;" +
    "'>ESCAPE SEQUENCE</h1>" +

    "<p style='" +
        "text-align: center;" +
        "color: #c62828;" +
        "font-size: 13px;" +
        "letter-spacing: 2px;" +
        "margin-top: 0;" +
    "'>Emergency Briefing — Read Fast.</p>" +

    "<p style='text-align: center; font-size: 13px;'>" +
        "The ship is failing. There are 3 escape pods. Not everyone gets one." +
    "</p>" +

    "<hr style='border: none; border-top: 1px solid #2a3a4a; margin: 16px 0;'/>" +

    // ── Objective ─────────────────────────────────────────
    "<h2 style='color: #c62828; font-size: 15px; letter-spacing: 3px;'>THE OBJECTIVE</h2>" +
    "<p style='font-size: 13px;'>" +
        "Get through 3 obstacles. Earn a P.A.C. keycard. Escape. " +
        "No keycard? <span style='color: #c62828;'>You don't make it out.</span>" +
    "</p>" +

    "<hr style='border: none; border-top: 1px solid #2a3a4a; margin: 16px 0;'/>" +

    // ── Card Game ─────────────────────────────────────────
    "<h2 style='color: #c62828; font-size: 15px; letter-spacing: 3px;'>THE CARD GAME</h2>" +
    "<p style='font-size: 13px;'>" +
        "Each obstacle is a round of cards played against <b style='color:#ffffff;'>The System</b> — the ship's AI." +
    "</p>" +
    "<ul style='font-size: 13px; line-height: 1.8;'>" +
        "<li>Cards are numbered <b>1 through 9</b></li>" +
        "<li>Get as close to <b>21</b> as possible without going over</li>" +
        "<li>The System is dealt one card <b>face-down</b> — you won't see it until results are calculated</li>" +
    "</ul>" +
    "<p style='font-size: 13px; margin-bottom: 4px;'>On your turn:</p>" +
    "<ul style='font-size: 13px; line-height: 1.8;'>" +
        "<li><b style='color:#ffffff;'>Hit</b> — draw another card</li>" +
        "<li><b style='color:#ffffff;'>Stay</b> — lock in your total</li>" +
    "</ul>" +

    "<hr style='border: none; border-top: 1px solid #2a3a4a; margin: 16px 0;'/>" +

    // ── Round Outcomes ────────────────────────────────────
    "<h2 style='color: #c62828; font-size: 15px; letter-spacing: 3px;'>ROUND OUTCOMES</h2>" +
    "<table style='width: 100%; border-collapse: collapse; font-size: 13px;'>" +
        "<tr style='background-color: #111827;'>" +
            "<th style='text-align:left; padding: 8px 12px; color:#c62828; border-bottom: 1px solid #2a3a4a;'>Result</th>" +
            "<th style='text-align:left; padding: 8px 12px; color:#c62828; border-bottom: 1px solid #2a3a4a;'>Consequence</th>" +
        "</tr>" +
        "<tr>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Closest to 21</td>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38; color:#81c784;'>Advance + receive a P.A.C. keycard</td>" +
        "</tr>" +
        "<tr style='background-color: #0d1117;'>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Under 21, not closest</td>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Advance, no keycard</td>" +
        "</tr>" +
        "<tr>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Over 21 (bust)</td>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38; color:#e05555;'>Eliminated</td>" +
        "</tr>" +
        "<tr style='background-color: #0d1117;'>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Over 21, System also busts</td>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Everyone advances — closest still gets the keycard</td>" +
        "</tr>" +
        "<tr>" +
            "<td style='padding: 7px 12px;'>Tie</td>" +
            "<td style='padding: 7px 12px;'>Bonus tiebreaker round — no specialty cards</td>" +
        "</tr>" +
    "</table>" +

    "<hr style='border: none; border-top: 1px solid #2a3a4a; margin: 16px 0;'/>" +

    // ── Specialty Cards ───────────────────────────────────
    "<h2 style='color: #c62828; font-size: 15px; letter-spacing: 3px;'>SPECIALTY CARDS</h2>" +
    "<p style='font-size: 13px;'>Dealt starting in Round 2. One-use. Use them wisely.</p>" +
    "<table style='width: 100%; border-collapse: collapse; font-size: 13px;'>" +
        "<tr style='background-color: #111827;'>" +
            "<th style='text-align:left; padding: 8px 12px; color:#c62828; border-bottom: 1px solid #2a3a4a;'>Card</th>" +
            "<th style='text-align:left; padding: 8px 12px; color:#c62828; border-bottom: 1px solid #2a3a4a;'>Effect</th>" +
        "</tr>" +
        "<tr>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Shield</td>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Return your last drawn card</td>" +
        "</tr>" +
        "<tr style='background-color: #0d1117;'>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Wild</td>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Choose your card's value</td>" +
        "</tr>" +
        "<tr>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Reverse</td>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Subtract your last drawn card from your total</td>" +
        "</tr>" +
        "<tr style='background-color: #0d1117;'>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Freeze</td>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Skip a chosen player's next turn</td>" +
        "</tr>" +
        "<tr>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Swap</td>" +
            "<td style='padding: 7px 12px; border-bottom: 1px solid #1e2a38;'>Exchange one of your cards with The System's</td>" +
        "</tr>" +
        "<tr style='background-color: #0d1117;'>" +
            "<td style='padding: 7px 12px;'>Peek</td>" +
            "<td style='padding: 7px 12px;'>View The System's hidden card or the next card in the deck</td>" +
        "</tr>" +
    "</table>" +
    "<p style='font-size: 12px; color:#a0aec0; margin-top: 8px;'>" +
        "&#9679; You may hold your Round 2 card and use it in Round 3" +
    "</p>" +

    "<hr style='border: none; border-top: 1px solid #2a3a4a; margin: 16px 0;'/>" +

    // ── The Rounds ────────────────────────────────────────
    "<h2 style='color: #c62828; font-size: 15px; letter-spacing: 3px;'>THE ROUNDS</h2>" +
    "<p style='font-size: 13px; line-height: 1.9;'>" +
        "<b style='color:#ffffff;'>Round 1</b> — No specialty cards. Survive or die.<br/>" +
        "<b style='color:#ffffff;'>Round 2</b> — Specialty cards are dealt. Use yours now or save it.<br/>" +
        "<b style='color:#ffffff;'>Round 3</b> — This is the hardest round. After results — players with a keycard escape. Everyone else is left behind. <br/>" +
    "</p>" +

    "<hr style='border: none; border-top: 1px solid #2a3a4a; margin: 16px 0;'/>" +

    // ── Keycards ──────────────────────────────────────────
    "<h2 style='color: #c62828; font-size: 15px; letter-spacing: 3px;'>KEYCARDS</h2>" +
    "<ul style='font-size: 13px; line-height: 1.8;'>" +
        "<li>Only <b>3 keycards</b> exist</li>" +
        "<li>Earned by being closest to 21 in a round</li>" +
        "<li>If you earn a second keycard, convert it to a <b>Wild</b> or give it to another player</li>" +
    "</ul>" +

    "<hr style='border: none; border-top: 1px solid #2a3a4a; margin: 16px 0;'/>" +

    // ── Footer ────────────────────────────────────────────
    "<p style='" +
        "text-align: center;" +
        "color: #c62828;" +
        "font-size: 13px;" +
        "letter-spacing: 4px;" +
        "margin-top: 8px;" +
    "'>SURVIVE. ESCAPE. DON'T LOOK BACK.</p>" +

    "</body></html>";

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tutorialPane = new javax.swing.JTextPane();
        backButton = new javax.swing.JButton();
        continueButton = new javax.swing.JButton();
        continueButton.setBackground(new java.awt.Color(0, 0, 0, 100));
        backgroundLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1000, 650));
        setPreferredSize(new java.awt.Dimension(1000, 650));

        jPanel1.setMinimumSize(new java.awt.Dimension(1200, 700));
        jPanel1.setPreferredSize(new java.awt.Dimension(1200, 700));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane1.setViewportView(tutorialPane);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 20, 870, 550));

        backButton.setBackground(new java.awt.Color(0, 0, 0, 0));
        backButton.setContentAreaFilled(false);
        backButton.setBorder(null);
        backButton.setFocusPainted(false);
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });
        jPanel1.add(backButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 60, 60));

        continueButton.setFont(FontLoader.getVT323(40f));
        continueButton.setForeground(new java.awt.Color(255, 255, 255));
        continueButton.setOpaque(false);
        continueButton.setText("Continue");
        continueButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(102, 0, 0), new java.awt.Color(102, 0, 0), new java.awt.Color(102, 0, 0), new java.awt.Color(102, 0, 0)));
        continueButton.setContentAreaFilled(false);
        continueButton.setFocusPainted(false);
        continueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                continueButtonActionPerformed(evt);
            }
        });
        jPanel1.add(continueButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 580, 200, 70));
        jPanel1.add(backgroundLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //Back Arrow
    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        // TODO add your handling code here:
        dispose(); // Close settings
    }//GEN-LAST:event_backButtonActionPerformed

    private void continueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_continueButtonActionPerformed
        // TODO add your handling code here:
        if (source == Source.SINGLE_PLAYER) {
            new Round1(playerName).setVisible(true);
        } else if (source == Source.MULTIPLAYER) {
            new Round1(gameController).setVisible(true); 
        }
        dispose();
    }//GEN-LAST:event_continueButtonActionPerformed

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
            java.util.logging.Logger.getLogger(RulesScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RulesScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RulesScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RulesScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RulesScreen(RulesScreen.Source.MAIN_MENU, "", null).setVisible(true);;
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JLabel backgroundLabel;
    private javax.swing.JButton continueButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane tutorialPane;
    // End of variables declaration//GEN-END:variables
}
