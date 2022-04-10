package com.example.huntinggame;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int height = 5;
    private static final int width = 3;
    private static int lifeCount = 3;

    ImageView[][] gameBoard = new ImageView[height][width];
    ImageView[] lives;
    Button leftBTN;
    Button rightBTN;
    Button upBTN;
    Button downBTN;
    TextView score_TXT;
    LinearLayout grid;

    int score = 0;
    Random rnd = new Random();

    // positions for hunter and hunted in [x,y] format
    int[] hunterPos = new int[2];
    int[] huntedPos = new int[2];

    int huntedMovement = 3; //default is down so it wont move

    /**
     * Programmatically create an ImageView grid for the game with width X height dimensions
     *
     */
    void initGrid()
    {
        for(int i = 0; i < height; i++)
        {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < width; j++)
            {
                ImageView cell = new ImageView(this);
                cell.setLayoutParams(new LayoutParams(grid.getWidth() / width, grid.getHeight() / height)); //set cell size
                cell.setBackgroundResource(R.drawable.border);
                row.addView(cell);
                gameBoard[i][j] = cell;
            }
            grid.addView(row);
        }
        initPlayers();
    }

    /**
     * function to setup hunter and hunted starting positions and zero-ing the score
     */
    void initPlayers()
    {
        //zero game's score
        score = 0;
        score_TXT.setText(String.valueOf(score));

        gameBoard[hunterPos[1]][hunterPos[0]].setImageResource(0);
        gameBoard[huntedPos[1]][huntedPos[0]].setImageResource(0);
        //save hunter's coordinates
        hunterPos[1] = 0;
        hunterPos[0] = width / 2;

        //save hunted post
        huntedPos[1] = height - 1;
        huntedPos[0] = width / 2;

        gameBoard[hunterPos[1]][hunterPos[0]].setImageResource(R.drawable.boss); // set hunter's base location in the middle of top row
        gameBoard[huntedPos[1]][huntedPos[0]].setImageResource(R.drawable.man); // set hunted base location in the middle of bottom row
    }

    /**
     * find the relevant views in the app
     */
    void findViews()
    {
        score_TXT = findViewById(R.id.Main_TXT_Score);
        grid = findViewById(R.id.Main_Layout_Grid);

        lives = new ImageView[] {findViewById(R.id.Main_IMG_Heart1),
                findViewById(R.id.Main_IMG_Heart2),
                findViewById(R.id.Main_IMG_Heart3)};

        setButtons();
    }

    /**
     * setup buttons for the app, find their views and set listeners for each
     * assign player's movement direction according to the button clicked:
     * 0 -> right, 1 -> up, 2 -> left, 3-> down
     */
    void setButtons()
    {
        leftBTN = findViewById(R.id.Main_BTN_Left);
        rightBTN = findViewById(R.id.Main_BTN_Right);
        upBTN = findViewById(R.id.Main_BTN_up);
        downBTN = findViewById(R.id.Main_BTN_down);


        rightBTN.setOnClickListener((v) -> huntedMovement = 0);
        upBTN.setOnClickListener((v) -> huntedMovement = 1);
        leftBTN.setOnClickListener((v) -> huntedMovement = 2);
        downBTN.setOnClickListener((v) -> huntedMovement = 3);
    }

    /**
     *
     * @param item the item to move, hunter or hunted
     * @param xStep the amount to move on the x Axis
     * @param yStep the amount to move on the y Axis
     * @param drawable the picture to draw, to move the hunter\hunted on the screen
     */
    void moveItem(int[] item, int xStep, int yStep, int drawable)
    {
        int new_X = item[0] + xStep;
        int new_Y = item[1] + yStep;

        if(new_X >= width ||  new_X < 0 || new_Y >= height || new_Y < 0) //out of bounds
        {
            return;
        }
        gameBoard[item[1]][item[0]].setImageResource(0); //delete prev pic
        item[0] += xStep;
        item[1] += yStep;
        gameBoard[item[1]][item[0]].setImageResource(drawable); //delete prev pic

    }

    /**
     * since 0-3 are used to choose direction, conversion is needed to transform it into an yStep,xStep
     * I map the numbers 0-3 into {1,0,1,0} accordingly using %2 to get axis of movement: 1->y axis, 0 -> x axis
     * then I map it to get {1,-1,-1,1} to decide the movement direction on the axis
     * @param num a number to represent movement direction
     * @return and array consisting of [yStep, xStep]
     */
    int[] numberToDirection(int num)
    {
        int[] movement = {0, 0};
        int sign = num >= 2 ? -1 : 1;
        int axis = (num + 1) % 2;
        movement[axis] = sign * (axis * 2 -1);

        return movement;
    }

    /**
     *
     * @return if hunter and hunted on the same cell
     */
    boolean checkHit()
    {
        return hunterPos[0] == huntedPos[0] && hunterPos[1] == huntedPos[1];
    }

    /**
     * test if game is over and reduce lives each time player is hit
     * @return if the game is over
     */
    boolean checkGameOver()
    {
        if(checkHit()) {
            lifeCount--;
            lives[lifeCount].setVisibility(View.INVISIBLE);
            if(lifeCount == 0)
            {
                finish();
                System.exit(0);
            }
            score = 0;
            return true;
        }
        return false;
    }

    /**
     * tick function for the game's clock.
     * 2 game over checks are required, once to check if hunter stepped on hunter, or the opposite
     */
    void tick()
    {
        score++;
        if(checkGameOver()) // check for game over
        {
            initPlayers();
            return;
        }
        score_TXT.setText(String.valueOf(score));
        int[] move = numberToDirection(huntedMovement);
        moveItem(huntedPos, move[1], move[0],R.drawable.man); // if hunted steps on hunter.
        if(checkGameOver())
        {
            initPlayers();
            return;
        }

        int dir = rnd.nextInt(4); //0-3
        move = numberToDirection(dir);
        moveItem(hunterPos, move[1], move[0], R.drawable.boss);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();

        grid.post(() -> { //since I need the layout's size, I use the post method
            initGrid();
            Handler clock = new Handler();
            clock.postDelayed(new Runnable() {
                @Override
                public void run() {
                   tick();
                   clock.postDelayed(this, 1000);
                }
            },1000);
        });
    }
}