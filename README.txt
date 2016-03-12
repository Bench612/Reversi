Compile using 
javac Reversi.java
Run using
java Reversi
Command arguments
-h for a human vs human game
-l to let the AI go first
-n <board size> The maximum board size is 26 and minimum is 4.
-t <total game time limit in seconds>
-u <time limit per turn in seconds>

The AI uses a min-max algorithm with a heuristic function that considers stable pieces on the board.
