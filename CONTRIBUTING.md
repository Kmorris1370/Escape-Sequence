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
| `EscapeSequence.java` | `escapesequence` | `main(args)` |
| `GameController.java` | `escapesequence` | `dealOpeningHand()`, `playerHit(p)`, `playerCanHit(p)`, `applyShield(p)`, `applyReverse(p)`, `applyFreeze(target)`, `applyWild(p, value)`, `applySwap(...)`, `applyPeek()`, `resolveOutcome(p)`, `getTiedPlayers()`, `awardTiebreakerKeycard(winner)`, `getAI()` |
| `Player.java` | `escapesequence` | `getName()`, `hasKeycard()`, `awardKeycard()`, `removeOneKeycard()`, `transferKeycardTo(recipient)`, `isAlive()`, `eliminate()`, `isFrozen()`, `setFrozen(frozen)`, `hasWildCard()`, `setHasWildCard(val)`, `isBust()`, `isPendingKeycardBonus()`, `clearPendingKeycardBonus()`, `receiveCard(card)`, `getHandTotal()`, `getHand()`, `clearHand()`, `receiveSpecialtyCard(card)`, `useSpecialtyCard(index)`, `getSpecialtyCards()`, `hasSpecialtyCards()`, `toString()` |
| `SpecialtyCard.java` | `escapesequence` | `getType()`, `toString()` |
| `SpecialtyDeck.java` | `escapesequence` | `buildDeck(numPlayers)`, `shuffle()`, `drawCard()`, `size()` |
| `EscapeScreen.java` | `escapesequence.UI` | Final escape pod outcome screen |
| `FontLoader.java` | `escapesequence.UI` | `getVT323(size)` |
| `GameDialog.java` | `escapesequence.UI` | `show(parent, message)`, `showChoice(parent, prompt, options)`, `showConfirm(parent, message)` |
| `GameOver.java` | `escapesequence.UI` | Game over screen |
| `MainMenu.java` | `escapesequence.UI` | Main menu / title interface |
| `Multiplayer.java` | `escapesequence.UI` | Multiplayer setup screen |
| `PauseScreen.java` | `escapesequence.UI` | Pause overlay dialog |
| `ResourceLoader.java` | `escapesequence.UI` | `loadFont(path)`, `fallbackFont()`, `loadImageScaled(path, w, h)` |
| `Round1.java` | `escapesequence.UI` | First round gameplay screen |
| `Round2.java` | `escapesequence.UI` | Second round gameplay screen (specialty cards introduced) |
| `Round3.java` | `escapesequence.UI` | Third round gameplay screen (final round) |
| `RulesScreen.java` | `escapesequence.UI` | Tutorial / rules pop-up screen |
| `SinglePlayer.java` | `escapesequence.UI` | Single player setup screen |
| `SoundManager.java` | `escapesequence.UI` | `play(sound)`, `setMuted(boolean)`, `isMuted()` |
| `TiebreakerRound.java` | `escapesequence.UI` | Bonus tiebreaker round dialog |

---

## Loading Images

**Always use `ResourceLoader` — never use the NetBeans image chooser dialog.** Using the dialog hardcodes a file path that will break on other machines.

### How to load a background image:

```java
public NameOfGUI() {
    initComponents();
    backgroundLabel.setIcon(ResourceLoader.loadImageScaled("/assets/pictures/yourimage.png", width, height));
}
```

### Rules:
- Always call image loading after `initComponents()` in the constructor
- Always use `loadImageScaled()` with the exact width and height of the component
- Image files must be placed in `assets/pictures` in the Projects panel
- Image file names are case sensitive — match them exactly
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
- Font files must stay in `assets/font` in the Projects panel

---

## Playing Sounds

**Always go through `SoundManager` — never call `Clip` / `AudioSystem` directly from screen code.**

### How to play a sound effect:

```java
SoundManager.play("click");   // also: "deal", "win", "lose"
```

### Rules:
- Sound files must stay in `assets/sounds` in the Projects panel and be `.wav`
- Add new SFX names by extending `SoundManager`, not by hardcoding paths in screens
- Respect the global mute state — never bypass `SoundManager.isMuted()`

---

## Game Dialogs

**Always use `GameDialog` for in-game messages, prompts, and confirmations instead of `JOptionPane`.** `GameDialog` matches the VT323 / red-on-black aesthetic of the rest of the game.

```java
GameDialog.show(this, "Reverse used. A card was subtracted from your total.");
String pick = GameDialog.showChoice(this, "Choose a target to freeze:", names);
```

---

## GitHub Workflow

- Always pull before you start working to get the latest changes:
  - In NetBeans: **Team → Pull**
  - Or in terminal: `git pull`
- Commit often with clear descriptive messages
- Never commit directly to `main` if possible — use a branch for larger features
- After committing, let the other team member know via messaging so they can pull
