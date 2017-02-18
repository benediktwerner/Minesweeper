Minesweeper Solver
==================

This is an automatic solver for the game [Minesweeper](https://en.wikipedia.org/wiki/Minesweeper_(video_game)). It works with the Windows 7 and Windows 10 versions. It also includes a simple Minesweeper implementation.

## How to use
Run `SimpleMinesweeper` to play the game.

Run `Windows7Minesweeper` to solve the Windows 7 version and `Windows10Minesweeper` for the Windows 10 version. After 3 seconds the program will automatically detect an open Minesweeper game and play it by taking screenshots, analyzing them, calculating the best move and then moving the mouse to execute that move. It should always find the best possible move. If there is no save move the solver will click the square that has the lowest probability to be a bomb.

**Note:** The Windows 7 version sometimes has problems detecting the squares at the top left corner on large boards.
