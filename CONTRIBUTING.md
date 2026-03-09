# Contributing to Escape Sequence
**The Space Cadets** | Kaitlyn Morris & Akera Griffith

This document outlines the standards and practices to follow when contributing to this project. Please read this before writing any new code or creating new screens.

---

## Project Structure
```
escape-sequence/
├── src/
│   └── escapesequence/
│       ├── EscapeSequence.java          # Entry Point
│       ├── Index.java                   # 
│       └── UI/
│           ├── FontLoader               # Font load shortcut
│           ├── ResourceLoader           # Loads font and images
│           ├── TitleInterface.java      # Main menu GUI
│           ├── Round1.java              # 1st gameplay screen
│           ├── Round2.java              # 2nd gameplay screen
│           ├── Round3.java              # 3rd gameplay screen
│           ├── RulesScreen.java         # Tutorial pop-up screen
│           ├── PauseScreen.java         # Pause pop-up screen
│           ├── SinglePlayer.java        # Single player screen
│           ├── Multiplayer.java         # Multiplayer screen
│           ├── ConformationScreen.java  # Confirmation pop-up screen
│           └── Settings.java            # Settings screen
├── docs/
│   ├── SRS.pdf                          # Software Requirements Specification
│   ├── ProjectManagementPlan.pdf        # Project Management Plan
│   ├── GameDesignDocument.pdf           # Game Design Document
│   ├── ActivityDiagram.png              # UML Activity Diagram
│   └── UseCaseDiagram.png               # UML Use Case Diagram
├── assets/
│   ├── pictures/                        # Game art and screen assets
│   └── font                             # Font file
├── tutorials
├── .gitignore
├── LICENSE
└── README.md
```

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

