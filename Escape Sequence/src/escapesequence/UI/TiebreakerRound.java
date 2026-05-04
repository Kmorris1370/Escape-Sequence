package escapesequence.UI;

/**
 * @author Akera Griffith & Kaitlyn Morris
 * Modal tiebreaker dialog — played when multiple players share the highest total.
 */

import escapesequence.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.ArrayList;

public class TiebreakerRound extends JDialog {

    // ── Game State ───────────────────────────────────────────
    private final GameController gameController;
    private final ArrayList<Player> tiedPlayers;
    private Deck tiebreakerDeck;
    private boolean[] playerStayed;
    private int currentPlayerIndex;
    private Player currentPlayer;

    // ── Theme ────────────────────────────────────────────────
    private static final Color BG  = new Color(10, 10, 15);
    private static final Color RED = new Color(198, 40, 40);

    // ── UI Components ────────────────────────────────────────
    private JLabel playerNameLabel;
    private JLabel playerTotalLabel;
    private JLabel statusLabel;
    private JLabel[] cardSlots;
    private JButton hitButton;
    private JButton stayButton;

    // ── Constructor ──────────────────────────────────────────
    public TiebreakerRound(Frame parent, GameController gc, ArrayList<Player> tied) {
        super(parent, true); // modal — blocks calling frame until disposed
        this.gameController = gc;
        this.tiedPlayers    = new ArrayList<>(tied);

        setUndecorated(true);
        buildUI();
        pack();
        setSize(620, 380);
        setLocationRelativeTo(parent);

        startTiebreaker();
    }

    // ── UI Construction ──────────────────────────────────────
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createLineBorder(RED, 2));

        // ── Title ────────────────────────────────────────────
        JLabel titleLabel = new JLabel("TIEBREAKER", SwingConstants.CENTER);
        titleLabel.setFont(FontLoader.getVT323(52f));
        titleLabel.setForeground(RED);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(14, 0, 4, 0));

        // ── Player Info ──────────────────────────────────────
        playerNameLabel = new JLabel("", SwingConstants.CENTER);
        playerNameLabel.setFont(FontLoader.getVT323(34f));
        playerNameLabel.setForeground(Color.WHITE);

        playerTotalLabel = new JLabel("", SwingConstants.CENTER);
        playerTotalLabel.setFont(FontLoader.getVT323(28f));
        playerTotalLabel.setForeground(Color.WHITE);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(FontLoader.getVT323(22f));
        statusLabel.setForeground(RED);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 6, 0));

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 0));
        infoPanel.setBackground(BG);
        infoPanel.add(playerNameLabel);
        infoPanel.add(playerTotalLabel);
        infoPanel.add(statusLabel);

        // ── Card Slots ───────────────────────────────────────
        JPanel cardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        cardPanel.setBackground(BG);
        cardSlots = new JLabel[9];
        for (int i = 0; i < 9; i++) {
            cardSlots[i] = new JLabel();
            cardSlots[i].setPreferredSize(new Dimension(50, 50));
            cardSlots[i].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            cardSlots[i].setOpaque(true);
            cardSlots[i].setBackground(BG);
            cardSlots[i].setVisible(false);
            cardPanel.add(cardSlots[i]);
        }

        // ── Centre Panel ─────────────────────────────────────
        JPanel centrePanel = new JPanel(new BorderLayout());
        centrePanel.setBackground(BG);
        centrePanel.add(infoPanel, BorderLayout.NORTH);
        centrePanel.add(cardPanel, BorderLayout.CENTER);

        // ── Buttons ──────────────────────────────────────────
        hitButton   = buildButton("Hit");
        stayButton  = buildButton("Stay");
        hitButton.addActionListener(e  -> onHit());
        stayButton.addActionListener(e -> onStay());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        buttonPanel.setBackground(BG);
        buttonPanel.add(hitButton);
        buttonPanel.add(stayButton);

        // ── Assemble ─────────────────────────────────────────
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(BG);
        northPanel.add(titleLabel, BorderLayout.CENTER);

        root.add(northPanel,   BorderLayout.NORTH);
        root.add(centrePanel,  BorderLayout.CENTER);
        root.add(buttonPanel,  BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JButton buildButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FontLoader.getVT323(32f));
        btn.setForeground(Color.WHITE);
        btn.setBackground(BG);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(RED));
        btn.setPreferredSize(new Dimension(130, 50));
        return btn;
    }

    // ── Round Flow ───────────────────────────────────────────
    private void startTiebreaker() {
        // Clear hands and deal one fresh card to each tied player
        tiebreakerDeck = new Deck(1); // 9-card deck
        for (Player p : tiedPlayers) {
            p.clearHand();
            p.receiveCard(new Card(tiebreakerDeck.drawCard(), true));
        }

        playerStayed       = new boolean[tiedPlayers.size()];
        currentPlayerIndex = 0;
        currentPlayer      = tiedPlayers.get(0);

        refreshCardDisplay();
        updateDisplay();
    }

    private void onHit() {
        int val = tiebreakerDeck.drawCard();
        if (val == -1) {
            onStay(); // deck exhausted — force stay
            return;
        }
        currentPlayer.receiveCard(new Card(val, true));
        SoundManager.playDeal();
        refreshCardDisplay();
        updateDisplay();

        // Bust = player is out for the rest of the tiebreaker
        if (currentPlayer.isBust()) {
            playerStayed[currentPlayerIndex] = true;
        }

        // Round-robin: one action per turn, then pass
        hitButton.setEnabled(false);
        stayButton.setEnabled(false);
        javax.swing.Timer t = new javax.swing.Timer(900, e -> advanceToNextPlayer());
        t.setRepeats(false);
        t.start();
    }

    private void onStay() {
        playerStayed[currentPlayerIndex] = true;
        hitButton.setEnabled(false);
        stayButton.setEnabled(false);
        advanceToNextPlayer();
    }

    private void advanceToNextPlayer() {
        int next = -1;
        for (int i = 1; i <= tiedPlayers.size(); i++) {
            int idx = (currentPlayerIndex + i) % tiedPlayers.size();
            if (!playerStayed[idx]) { next = idx; break; }
        }

        if (next == -1) {
            resolveTiebreaker();
        } else {
            currentPlayerIndex = next;
            currentPlayer      = tiedPlayers.get(currentPlayerIndex);
            hitButton.setEnabled(true);
            stayButton.setEnabled(true);
            refreshCardDisplay();
            updateDisplay();
            GameDialog.showTimed((Frame) getOwner(), currentPlayer.getName() + "'s turn!", 1200);
        }
    }

    private void resolveTiebreaker() {
        int best = -1;
        ArrayList<Player> winners = new ArrayList<>();

        for (Player p : tiedPlayers) {
            if (!p.isBust()) {
                if (p.getHandTotal() > best) {
                    best = p.getHandTotal();
                    winners.clear();
                    winners.add(p);
                } else if (p.getHandTotal() == best) {
                    winners.add(p);
                }
            }
        }

        if (winners.size() == 1) {
            Player winner = winners.get(0);
            gameController.awardTiebreakerKeycard(winner);
            GameDialog.show((Frame) getOwner(),
                winner.getName() + " wins the tiebreaker and earns a P.A.C. keycard!");
            dispose();
        } else if (winners.isEmpty()) {
            GameDialog.show((Frame) getOwner(),
                "All players busted in the tiebreaker!\nNo keycard awarded.");
            dispose();
        } else {
            // Still tied — replay automatically
            GameDialog.show((Frame) getOwner(), "Still tied! Playing another tiebreaker round...");
            startTiebreaker();
        }
    }

    // ── Display Helpers ──────────────────────────────────────
    private void updateDisplay() {
        playerNameLabel.setText(currentPlayer.getName());
        playerTotalLabel.setText("Total: " + currentPlayer.getHandTotal());
        statusLabel.setText(currentPlayer.isBust() ? "BUST!" : currentPlayer.getName() + "'s turn");
        hitButton.setEnabled(!currentPlayer.isBust());
    }

    private void refreshCardDisplay() {
        // Clear all slots
        for (JLabel slot : cardSlots) {
            slot.setIcon(null);
            slot.setVisible(false);
        }
        // Show current player's cards
        int i = 0;
        for (Card c : currentPlayer.getHand()) {
            if (i >= cardSlots.length) break;
            cardSlots[i].setIcon(ResourceLoader.loadCardImage(c.getValue()));
            cardSlots[i].setVisible(true);
            i++;
        }
    }
}
