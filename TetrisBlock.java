import java.util.Arrays;
import java.util.Random;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Name: TetrisBlock.java
 * 
 * Purpose:
 * This class controls all component and game logic for Tetris Block game.
 * @author Hackerry
 */
public class TetrisBlock extends Application {
	//Useful constants
	private static final int LINE_SCORE = 100;
	private static final String BACK_COLOR = "#EB984E";
	private static final 
			int CELL_LENGTH = 20, WIDTH = 18, HEIGHT = 24,
				LEFT = -1, RIGHT = 1, UP = 0, DOWN = 8;
	private static 
		Block[][] fixedBoard, currBlocks;
	private static int x, y;
	private static int score = 0;
	private static Pane gamePane;
	private static StackPane startPane;
	private static Label scoreL;
	private static BorderPane mainPane;
	private static int sleepTime = 400;
	
	//Useful instance variables
	private Random rand = new Random();
	private Service timer = new Service() {
		@Override
		protected Task createTask() {
			return new Task() {
				@Override
				protected Object call() throws Exception {
					while(true) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								if(currBlocks == null) {
									getNewBlocks();
								}
								move(TetrisBlock.DOWN);
							}
						});
						Thread.sleep(sleepTime);
					}
				}
			};
		}
	};
	
	/**
	 * The JavaFX application start method
	 */
	@Override
    public void start(Stage primaryStage) {
    	mainPane = new BorderPane();
    	
    	startPane = new StackPane();
    	startPane.setStyle("-fx-background-color:" + BACK_COLOR);
    	VBox vBox = new VBox(40);
    	vBox.setPrefWidth(CELL_LENGTH*WIDTH);
    	vBox.setPrefHeight(CELL_LENGTH*HEIGHT);
    	vBox.setAlignment(Pos.CENTER);
    	vBox.setStyle("-fx-background-color:rgba(255,255,255,.3)");
    	Label gameL = new Label("Tetris Block");
    	gameL.setFont(Font.font("Times New Roman", FontPosture.ITALIC, 50));
    	Button startB = new Button("Start");
    	startB.setOnAction(e -> {
    		FadeTransition fade = new FadeTransition();
    		fade.setNode(vBox);
    		fade.setDuration(Duration.millis(600));
    		fade.setFromValue(0.3);
    		fade.setToValue(0);
    		fade.play();
    		fade.setOnFinished(ex -> {
    			mainPane.setCenter(gamePane);
        		timer.start();
    		});
    	});
    	vBox.getChildren().addAll(gameL, startB);
    	startPane.getChildren().add(vBox);
    	mainPane.setCenter(startPane);
    	
    	gamePane = new Pane();
    	gamePane.setPrefWidth(WIDTH*CELL_LENGTH);
    	gamePane.setPrefHeight(HEIGHT*CELL_LENGTH);
    	gamePane.setStyle("-fx-background-color:" + BACK_COLOR);
    	//mainPane.setCenter(gamePane);
    	
    	restart();
    	
    	HBox bottom = new HBox(10);
    	bottom.setPadding(new Insets(5,5,5,10));
    	scoreL = new Label("Score: " + score);
    	scoreL.setFont(Font.font("Times New Roman", FontWeight.BOLD, 20));
    	bottom.getChildren().add(scoreL);
    	mainPane.setBottom(bottom);
    	
    	Scene scene = new Scene(mainPane);
    	scene.setOnKeyPressed(new MyHandler());
    	primaryStage.setScene(scene);
    	primaryStage.setTitle("Tetris Block");
    	primaryStage.show();
    }

	/**
	 * Called to reset the game board and 
	 */
    private void restart() {
    	fixedBoard = new Block[HEIGHT][WIDTH];
		gamePane.getChildren().clear();
		currBlocks = null;
		score = 0;
    }
    
    /**
     * Called when game is over
     */
    private void gameOver() {
    	if(timer.isRunning()) {
    		timer.cancel();
    	}
    	VBox vBox = new VBox(40);
    	vBox.setPrefWidth(CELL_LENGTH*WIDTH);
    	vBox.setPrefHeight(CELL_LENGTH*HEIGHT);
    	vBox.setAlignment(Pos.CENTER);
    	vBox.setStyle("-fx-background-color:rgba(255,255,255,.3)");
    	Label gameOverL = new Label("Game Over");
    	gameOverL.setFont(Font.font("Times New Roman", FontPosture.ITALIC, 50));
    	HBox buttonBox = new HBox(20);
    	buttonBox.setAlignment(Pos.CENTER);
    	Button restartB = new Button("Restart");
    	restartB.setOnAction(e -> {
    		FadeTransition fade = new FadeTransition();
    		fade.setNode(vBox);
    		fade.setDuration(Duration.millis(600));
    		fade.setFromValue(0.3);
    		fade.setToValue(0);
    		fade.play();
    		fade.setOnFinished(ex -> {
    			mainPane.setCenter(gamePane);
    			restart();
    			timer.reset();
        		timer.start();
    		});
    	});
    	Button exitB = new Button("Exit");
    	exitB.setOnAction(e -> {
    		System.exit(0);
    	});
    	buttonBox.getChildren().addAll(restartB, exitB);
    	vBox.getChildren().addAll(gameOverL, buttonBox);
    	startPane.getChildren().clear();
    	startPane.getChildren().add(vBox);
    	mainPane.setCenter(startPane);
    }
    
    /**
     * Randomly get a new moving block
     */
    private void getNewBlocks() {
    	//System.out.println("*********Get new blocks**********");
    	//Get new blocks
    	currBlocks = Data.getBlock(rand.nextInt(Data.BLOCK_NUM));
    	
    	//Randomly rotate the block
    	for(int i = 0; i < rand.nextInt(4); i++) {
    		rotateBlocks();
    	}
    	
    	x = (WIDTH-currBlocks[0].length)/2;
    	y = 0;
    	
    	for(int i = 0; i < currBlocks.length; i++) {
    		for(int j = 0; j < currBlocks[i].length; j++) {
    			if(currBlocks[i][j] != null) {
    				if(fixedBoard[i+y][j+x] != null) {
    					gameOver();
    					return;
    				}
    			}
    		}
    	}
    		
    	for(int i = 0; i < currBlocks.length; i++) {
    		for(int j = 0; j < currBlocks[i].length; j++) {
    			if(currBlocks[i][j] != null) {
    				gamePane.getChildren().add(currBlocks[i][j]);
    				currBlocks[i][j].relocate((x+j)*CELL_LENGTH, (y+i)*CELL_LENGTH);
    			}
    		}
    	}
    	
    	//Future improvement, game over!!!!!!!!!!!!!!!!!!!!!!
    }
    
    /**
     * Update the game board
     */
    private void updateBoard() {
    	//Update Fixed Board
    	for(int i = 0; i < fixedBoard.length; i++) {
    		for(int j = 0; j < fixedBoard[i].length; j++) {
    			if(fixedBoard[i][j] != null) {
    				fixedBoard[i][j].relocate(CELL_LENGTH*j, CELL_LENGTH*i);
    			}
    		}
    	}
    	
    	//Update Moving Blocks
    	for(int i = 0; i < currBlocks.length; i++) {
    		for(int j = 0; j < currBlocks[i].length; j++) {
    			if(currBlocks[i][j] != null) {
    				currBlocks[i][j].relocate(CELL_LENGTH*(j+x), CELL_LENGTH*(i+y));
    			}
    		}
    	}
    	
    	//Update score
    	scoreL.setText("Score: " + score);
    }
    
    /**
     * Rotate the moving blocks
     */
    private void rotateBlocks() {
    	Block[][] temp = new Block[currBlocks[0].length][currBlocks.length];
    	for(int i = 0; i < currBlocks.length; i++) {
    		for(int j = 0; j < currBlocks[i].length; j++) {
    			temp[j][currBlocks.length-i-1] = currBlocks[i][j];
    		}
    	}
    	
    	if(x + temp[0].length > WIDTH-1) {
    		x = WIDTH-temp[0].length;
    	}
    	
    	for(int i = 0; i < temp.length; i++) {
    		for(int j = 0; j < temp[i].length; j++) {
    			if(temp[i][j] != null) {
    				if(i + y >= HEIGHT) {
    		    		//System.out.println("At bottom! No space for rotation");
    		    		return;
    				} else if(fixedBoard[i+y][j+x] != null) {
    					//System.out.println("Clash! cannot rotate");
        				return;
    				}
    			}
    		}
    	}
    	
    	currBlocks = temp;
    	checkBlockFixed();
    }
    
    /**
     * Move down the block
     */
    private void moveDown() {
    	y++;
    }
    
    /**
     * Left or right move the block
     * @param direction LEFT or RIGHT
     */
    private void moveHorizontal(int direction) {
    	if(direction != TetrisBlock.LEFT && direction != TetrisBlock.RIGHT) {
    		return;
    	}
    	int tempX = x+direction;
    	for(int i = 0; i < currBlocks.length; i++) {
    		for(int j = 0; j < currBlocks[i].length; j++) {
    			if(currBlocks[i][j] != null) {
    				if(j + tempX < 0) {
    					//System.out.println("Touch left border!");
    					return;
    				} else if(j + tempX > WIDTH-1) {
    					//System.out.println("Touch right border!");
    					return;
    				} else if(fixedBoard[i+y][j+tempX] != null) {
    					//System.out.println("Touch another block");
    					return;
    				}
    			}
    		}
    	}
    	x = tempX;
    }
    
    /**
     * Check whether the moving block is fixed
     * @return whether the block is fixed
     */
    private boolean checkBlockFixed() {
    	boolean fixed = false;
    	Outer:for(int i = 0; i < currBlocks.length; i++) {
    		for(int j = 0; j < currBlocks[i].length; j++) {
    			if(currBlocks[i][j] != null) {
    				if(i+y == HEIGHT || (i+y < HEIGHT && fixedBoard[i+y][j+x] != null)) {
    					fixed = true;
    					y--;
    					break Outer;
    				}
    			}
    		}
    	}
    	
    	if(fixed) {
    		for(int i = 0; i < currBlocks.length; i++) {
        		for(int j = 0; j < currBlocks[i].length; j++) {
        			if(currBlocks[i][j] != null) {
        				fixedBoard[i+y][j+x] = currBlocks[i][j]; 
        			}
        		}
    		}
    		getNewBlocks();
    		//System.out.println("Fixed");
    	}
    	
    	return fixed;
    }
    
    /**
     * Get user input and categorize it into rotate the block or moving the block.
     * @param direction
     */
    private void move(int direction) {
    	if(!timer.isRunning()) {
    		return;
    	}
    	switch(direction) {
    		case TetrisBlock.UP:
    			rotateBlocks();
    			break;
    		case TetrisBlock.DOWN:
    			moveDown();
    			break;
    		case TetrisBlock.LEFT:
    		case TetrisBlock.RIGHT:
    			moveHorizontal(direction);
    			break;
    		default:
    			break;
    	}
    	
    	checkBlockFixed();
    	checkLineEliminated();
    	updateBoard();
    }
    
    /**
     * Check whether a line of blocks is formed
     */
    private void checkLineEliminated() {
    	Outer:for(int i = fixedBoard.length-1; i > 0; i--) {
    		for(int j = 0; j < fixedBoard[i].length; j++) {
    			if(fixedBoard[i][j] == null) {
    				continue Outer;
    			}
    		}
    		
    		gamePane.getChildren().removeAll(Arrays.asList(fixedBoard[i]));
    		
    		for(int k = i; k > 0; k--) {
    			for(int j = 0; j < fixedBoard[k].length; j++) {
    				fixedBoard[k][j] = fixedBoard[k-1][j];
    			}
    		}
    		
    		for(int k = 0; k < fixedBoard[0].length; k++) {
    			fixedBoard[0][k] = null;
    		}
    		
    		i++;
    		
    		score += LINE_SCORE;
    		if(score >= LINE_SCORE*8 && sleepTime >= 100) {
    			sleepTime -= 10;
    		}
    	}
    }
    
    /**
     * Purpose: Listener for user keyboard input
     */
	private class MyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent event) {
			switch(event.getCode()) {
				case UP: 
				case W:
					move(TetrisBlock.UP);
					break;
				case DOWN: 
				case S:
					move(TetrisBlock.DOWN);
					break;
				case LEFT: 
				case A:
					move(TetrisBlock.LEFT);
					break;
				case RIGHT: 
				case D:
					move(TetrisBlock.RIGHT);
					break;
				case P:
					if(!timer.isRunning()) {
						timer.reset();
						timer.start();
					} else {
						timer.cancel();
					}
					break;
				case R:
					restart();
					break;
				default:
					break;
			}
		}
	}
    
	/**
	 * Helper method to print the board
	 * @param blocks the blocks to be printed
	 */
    public static void printArray(Block[][] blocks) {
    	for(int i = 0; i < blocks.length; i++) {
    		for(int j = 0; j < blocks[i].length; j++) {
    			System.out.print(blocks[i][j] + " ");
    		}
    		System.out.println();
    	}
    }
    
    /**
     * Driver method for JavaFX
     * @param args not used
     */
    public static void main(String[] args) {
    	Application.launch(args);
    }
}

/**
 * Purpose: Represent a single block
 */
class Block extends StackPane {
	//Useful constants
	public static final int T = 0, LLEFT = 1, LRIGHT = 2, SQUARE = 3, STICK = 4, ZLEFT = 5, ZRIGHT = 6;
	private static final Color[] colors = new Color[] {
			Color.rgb(241, 196, 15),	//Yellow hue
			Color.rgb(39, 174, 96), 	//Green hue
			Color.rgb(41, 128, 185), 	//Blue hue
			Color.rgb(231, 76, 60), 	//Red hue
			Color.rgb(142, 68, 173), 	//Purple hue
			Color.rgb(44, 62, 80), 		//Black hue
			Color.rgb(52, 152, 219)		//Aqua hue
		};
	private Color background, border = Color.rgb(0, 0, 0, .4);
	
	/**
	 * Constructor for a block
	 * @param blockType the constant of the block
	 */
	public Block(int blockType) {
		this.background = colors[blockType];
		
		Rectangle rec = new Rectangle();
		rec.setWidth(20);
		rec.setHeight(20);
		rec.setFill(background);
		rec.setStroke(border);
		rec.setArcWidth(8);
		rec.setArcHeight(8);
		
		this.getChildren().addAll(rec);
	}
}

/**
 * Purpose: Store block data
 */
class Data {
	private static final int[][][] DATA = new int[][][] {
		{			//T block
			{1,0},
			{1,1},
			{1,0},
		},
		{			//L block
			{1,1},
			{0,1},
			{0,1},
		},
		{			//L block
			{1,1},
			{1,0},
			{1,0},
		},
		{			//Square block
			{1,1},
			{1,1},
		},
		{			//Stick block
			{1,0},
			{1,0},
			{1,0},
			{1,0}
		},
		{			//Z block
			{1,1,0},
			{0,1,1}
		},
		{			//Z block
			{0,1,1},
			{1,1,0}
		}
	};
	public static final int BLOCK_NUM = DATA.length;
	
	/**
	 * Get blocks array with blocks initialized
	 * @param index the type of block
	 * @return the initialized blocks
	 */
	public static Block[][] getBlock(int index) {
		if(index < 0 || index >= DATA.length) {
			return null;
		}
		int[][] temp = DATA[index];
		Block[][] blocks = new Block[temp.length][temp[0].length];
		
		for(int i = 0; i < temp.length; i++) {
			for(int j = 0; j < temp[i].length; j++) {
				if(temp[i][j] != 0) {
					blocks[i][j] = new Block(index);
				} else {
					blocks[i][j] = null;
				}
			}
		}
		return blocks;
	}
}
