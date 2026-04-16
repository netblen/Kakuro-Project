# Kakuro Master

## Overview

The Kakuro Puzzle App is an Android application that allows users to play Kakuro puzzles of varying sizes and difficulty levels. The app supports both predefined puzzle templates and randomly generated boards, providing a dynamic and engaging gameplay experience.

---
<img src="https://github.com/user-attachments/assets/3776e9a1-3759-4f76-bada-a5a8644887f6" width="230"/>
<img src="https://github.com/user-attachments/assets/7e563e07-4267-46d1-8e5b-1ef102feb795" width="230"/>
<img src="https://github.com/user-attachments/assets/2ca959b4-2037-4308-9c93-ad03ecee062d" width="230"/>
<img src="https://github.com/user-attachments/assets/56115982-57c3-4cd9-b2da-b54f20d2cec9" width="230"/>
<img src="https://github.com/user-attachments/assets/7001f1b8-750e-4dbb-bfd6-0b2379905538" width="230"/>
<img src="https://github.com/user-attachments/assets/a955d820-f219-4a9a-b3c2-610e4b3610a8" width="230"/>
<img src="https://github.com/user-attachments/assets/0540c4f1-b982-4344-a736-155a10c72034" width="230"/>
<img src="https://github.com/user-attachments/assets/98f68bc1-1c19-4d8a-aef1-3426d39dbb9d" width="230"/>

---
## Features

### Gameplay

* Play Kakuro puzzles on multiple grid sizes (5x5, 7x7, 9x8)
* Multiple difficulty levels (1–6)
* Level 6 generates a **random puzzle** each time

### User Interaction

* Tap a cell to select it
* Use the keypad to enter numbers (1–9)
* Delete values using the delete button
* Real-time validation of moves

### Validation System

* Highlights:

  * Correct cells (green)
  * Conflicting cells (red)
* Prevents invalid Kakuro placements (duplicate numbers or incorrect sums)

### Game Controls

* Undo / Redo functionality
* Hint system (limited hints per game)
* Show solution option (ends the game)

### Timer

* Tracks how long the user takes to solve the puzzle
* Automatically pauses/resumes when app is minimized/restored

### Leaderboard 

* Stores player performance
* Tracks wins and hints used

---

## How It Works

### Board Generation

* Predefined boards are loaded based on selected level and grid size
* Random boards are generated using:

  * Random black cell placement
  * Backtracking algorithm to generate a valid solution
  * Automatic calculation of clue sums

### Game Logic

* Each move updates the board state
* Runs (horizontal and vertical) are validated after every input
* The game checks for completion when all cells are correctly filled


---

## Technologies Used

* **Kotlin** – Main programming language
* **Android SDK** – App development framework
* **GridLayout** – Dynamic board rendering
* **Firebase (Auth & Firestore)** – User data and stats storage
* **Custom Views** – For rendering Kakuro clue cells

---

## Project Structure

* `GameActivity` → Main game logic and UI handling
* `BoardSetup` → Board generation and solving algorithms
* `KakuroCell` → Data model for each cell
* `ClueCellView` → Custom UI for clue cells
* `LeaderboardAdapter` → Displays leaderboard entries

---


## Team

Alexanne Fortin, Anabella Miranda, Bunnita Yem

---

## Notes

This project was developed as part of a course assignment focusing on:

* Object-oriented design
* Interaction and class diagrams
* Android development
* Game logic implementation

