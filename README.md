# Escape Sequence
### *Not everyone makes it out.*

A first-person 2D survival card game built in Java using JFrame/Swing. Players are trapped aboard a failing spaceship and must fight their way through three deadly obstacles to reach the escape pods — but with only 3 pods and up to 6 players, survival is far from guaranteed.

> Inspired by the atmosphere of *Resident Evil 7* DLC "21" and *Mouthwashing.*
#### Title Screen
<img width="874" height="446" alt="Screenshot 2026-03-09 014458" src="https://github.com/user-attachments/assets/1c506129-1a34-432d-93c3-2d1fe5d880f1" />

---

## Game Overview

The ship is failing. Alarms are blaring. You and your fellow astronauts have one chance to reach the escape pods — but standing between you and survival are three increasingly dangerous obstacles, each a high-stakes round of a custom card game similar to blackjack.

To board an escape pod, you need a **P.A.C. (Personal Access Card)** keycard. Keycards are only awarded to the player who wins each round. There are **3 rounds, 3 keycards, and 3 escape pods**. With up to 6 players competing, the math is brutal — not everyone escapes.

---

## How to Play

### The Deck
The deck consists of cards numbered **1–9**. Each player adds 9 cards to the deck, so the more players there are, the larger the deck.

### Each Round
1. Every player is dealt 2 cards face-up
2. The AI opponent has 1 card face-down (hidden) and 1 card face-up
3. Players take turns choosing to **Hit** (draw a card) or **Stay** (hold their total)
4. Once all players stay, results are calculated

### Round Outcomes
| Result | Consequence |
|---|---|
| Closest to 21 without busting (and beats AI) | Advance + receive a P.A.C. keycard |
| Under 21 but didn't beat the AI | Advance without a keycard |
| Over 21 (bust) | Eliminated — unless the AI also busts |
| AI also busts | Everyone advances; closest player still gets the keycard |
| Tie among players who beat the AI | Bonus tiebreaker round (no specialty cards) |
| AI total beats all players | AI wins the round outright — no tiebreaker, no keycard |

### Specialty Cards
Starting in Round 2, players are dealt specialty cards that can turn the tide of any round:

| Card | Effect |
|---|---|
| 🛡️ **Shield** | Return the card you just drew — it is discarded |
| 🃏 **Wild** | Choose the value of the card yourself (1–9) |
| 🔄 **Reverse** | Removes the last card you drew (subtracts its value and removes it from the display). Must be used after at least one hit |
| ❄️ **Freeze** | Prevent a chosen player from hitting on their next turn |
| 🔀 **Swap** | Swap one of your face-up cards with one of the AI's |
| 👁️ **Peek** | View the next card in the deck |

Players may hold their Round 2 specialty card and use both cards in Round 3. The AI always saves its specialty card for Round 3 — making the final round the most dangerous.

### Keycards
- Only the round winner receives a keycard
- If a player earns a second keycard, they may gift it to another player or convert it into a **Shield** specialty card
- After Round 3, only players with a keycard may board an escape pod

---

## Features

- **1–6 Player Multiplayer** with a single AI opponent
- **Progressive AI Difficulty** — the AI takes greater risks with each round
- **Specialty Card System** — six unique cards that add strategy to every round
- **In-Game Tutorial/Rules Screen** — rules are accessible at any time during play
- **Tiebreaker Rounds** — ties among qualifying players are resolved in bonus rounds with no specialty cards
- **Pause & Game Over Screens** — full game-state flow including pause, restart, and final results
- **Sound Effects** — click, deal, win, and lose audio cues via a custom SoundManager
- **Custom GUI** — fully designed game screens built with Java JFrame/Swing

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java (JDK 11+) |
| UI Framework | JFrame / Swing |
| IDE | Apache NetBeans |
| Version Control | GitHub |

---

## How to Run

### Prerequisites
- Java JDK 11 or higher
- Apache NetBeans IDE (recommended) or any Java-compatible IDE

### Steps
1. Clone the repository:
- git clone https://github.com/your-username/escape-sequence.git
2. Open the project in NetBeans:
- File → Open Project → navigate to the cloned folder
3. Build the project:
- Run → Build Project (F11)
4. Run the game:
- Run → Run Project (F6)
> No external libraries or dependencies are required beyond the standard Java JDK.
---
## Project Structure

```
escape-sequence/
├── src/
│ └── escapesequence/
│ ├── AIPlayer.java # AI opponent logic and aggression scaling
│ ├── Card.java # Single card with value and face-up/down state
│ ├── Deck.java # Numbered deck, scales with player count
│ ├── EscapeSequence.java # Entry point
│ ├── GameController.java # Core game logic, outcome resolution, specialty effects
│ ├── Player.java # Player state, hand, keycard, specialty cards
│ ├── SpecialtyCard.java # Specialty card types and descriptions
│ ├── SpecialtyDeck.java # Specialty deck, scales with player count
│ └── UI/
│ ├── EscapeScreen.java # Final escape pod / outcome screen
│ ├── FontLoader.java # Font load shortcut
│ ├── GameDialog.java # Custom themed dialog/message boxes
│ ├── GameOver.java # Game over screen
│ ├── MainMenu.java # Main menu GUI
│ ├── Multiplayer.java # Multiplayer setup screen
│ ├── PauseScreen.java # Pause pop-up screen
│ ├── ResourceLoader.java # Loads fonts and images
│ ├── Round1.java # 1st gameplay screen
│ ├── Round2.java # 2nd gameplay screen
│ ├── Round3.java # 3rd gameplay screen
│ ├── RulesScreen.java # Tutorial / rules pop-up screen
│ ├── SinglePlayer.java # Single player setup screen
│ ├── SoundManager.java # Plays click / deal / win / lose SFX
│ └── TiebreakerRound.java # Bonus tiebreaker round dialog
├── assets/
│ ├── pictures/ # Game art and card images
│ ├── font/ # Font files (VT323)
│ └── sounds/ # SFX (click, deal, win, lose)
├── docs/
│ ├── SRS.pdf # Software Requirements Specification
│ ├── ProjectManagementPlan.pdf # Project Management Plan
│ ├── GameDesignDocument.pdf # Game Design Document
│ ├── ActivityDiagram.png # UML Activity Diagram
│ └── UseCaseDiagram.png # UML Use Case Diagram
├── tutorials/
├── .gitignore
├── LICENSE
├── CONTRIBUTING.md
└── README.md
```
## Team
| Name | Role |
|---|---|
| Kaitlyn Morris | Project Manager / Developer |
| Akera Griffith | Head Developer |
*The Space Cadets — CS 491*
---
*Licensed under the MIT License*
