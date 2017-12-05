This project is a simple Tetris block game to start with. I have implemented one before with awt, and this is a JavFX version.

The game contains basic functionality of Tetris block game. 

I made a class called Block which is a StackPane that represent a single Block. In the Data class, I included data for all types of Blocks(ie: Z-Block, T-Block...Experience from my previous version). In the TetrisBlock class, I included a fixedBlocks array which contains all the non-movable Blocks and a movingBlocks which contains the moving block user can interact with. The program reads user input and change the movingBlocks. A timer updates the game board. A score label records the scores.
