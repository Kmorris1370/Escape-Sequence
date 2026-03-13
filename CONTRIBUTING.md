# Contributing to Escape Sequence
**The Space Cadets** | Kaitlyn Morris & Akera Griffith

This document outlines the standards and practices to follow when contributing to this project. Please read this before writing any new code or creating new screens.

---
## Code Contributions


| File | Package | Methods |
|------|---------|---------|
| `AIPlayer.java` | `escapesequence` | `advanceRound()`, `getRound()`, `shouldHit()`, `shouldUseSpecialtyCard()` |
| `Card.java` | `escapesequence` | `getValue()`, `isFaceUp()`, `flip()`, `toString()` |
| `Deck.java` | `escapesequence` | `buildDeck(numPlayers)`, `shuffle()`, `drawCard()`, `size()` |
| `EscapeSequence.java` | `escapesequence` | TBD |
| `Index.java` | `escapesequence` | TBD |
| `Player.java` | `escapesequence` | `getName()`, `hasKeycard()`, `giveKeycard()`, `isAlive()`, `eliminate()`, `isFrozen()`, `setFrozen(frozen)`, `receiveCard(card)`, `getHandTotal()`, `getHand()`, `clearHand()`, `receiveSpecialtyCard(card)`, `useSpecialtyCard(index)`, `getSpecialtyCards()`, `hasSpecialtyCards()`, `toString()` |
| `SpecialtyCard.java` | `escapesequence` | `getType()`, `toString()` |
| `SpecialtyDeck.java` | `escapesequence` | `buildDeck(numPlayers)`, `shuffle()`, `drawCard()`, `size()` |
| `ConfirmationScreen.java` | `escapesequence.UI` | TBD |
| `FontLoader.java` | `escapesequence.UI` | `Font` |
| `TitleInterface.java` | `escapesequence.UI` | TBD |
| `Multiplayer.java` | `escapesequence.UI` | TBD |
| `PauseScreen.java` | `escapesequence.UI` | TBD |
| `ResourceLoader.java` | `escapesequence.UI` | `loadFont`, `fallbackFont`, `loadImageScaled` |
| `Round1.java` | `escapesequence.UI` | TBD |
| `Round2.java` | `escapesequence.UI` | TBD |
| `Round3.java` | `escapesequence.UI` | TBD |
| `RulesScreen.java` | `escapesequence.UI` | TBD |
| `Settings.java` | `escapesequence.UI` | TBD |
| `SinglePlayer.java` | `escapesequence.UI` | TBD |
 
---

## Loading Images

**Always use `ResourceLoader` — never use the NetBeans image chooser dialog.** Using the dialog hardcodes a file path that will break on other machines.

### How to load a background image:
```java
public *NameOfGUI* () {
    initComponents();
    backgroundLabel.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/yourimage.png", width, height));
}
```
### Rules:
- Always call image loading **after** `initComponents()` in the constructor
- Always use `loadImageScaled()` with the exact width and height of the component
- Image files must be placed in `assets.pictures` in the Projects panel
- Image file names are **case sensitive** — match them exactly
- Only use `.png` or `.jpg` files — SVG is not supported

---

## Loading Fonts

**Always use `FontLoader` — never set fonts manually or through the NetBeans properties panel.**

### How to apply a font to a component in generated code:
```java
myLabel.setFont(FontLoader.getVT323(24f));
myButton.setFont(FontLoader.getVT323(24f));
```

### In Design view:
1. Click the component
2. In the Properties panel find **Font**
3. Click `...` → select **Custom Code**
4. Type `FontLoader.getVT323(24f)` (or whatever size you need)

### Rules:
- Never use any font other than VT323 — it is the game's established font
- If VT323 fails to load, `FontLoader` will fall back to Arial automatically
- Font files must stay in `assets.font` in the Projects panel

---
## GitHub Workflow

1. Always **pull before you start working** to get the latest changes:
   - In NetBeans: Team → Pull
   - Or in terminal: `git pull`
2. Commit often with clear descriptive messages
3. Never commit directly to `main` if possible — use a branch for larger features
4. After committing, let the other team member know via messaging so they can pull

---

