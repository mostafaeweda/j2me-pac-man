/*
 * PlayCanvas.java
 *
 * Created on 21. November 2006, 13:46
 */
package com.pacman;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.LayerManager;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.lcdui.game.TiledLayer;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

/**
 *
 * @author Andreas Jakl
 */
public class PacManCanvas extends GameCanvas implements Runnable, CommandListener
{
    /** Pac-Man step increment in pixels */
    private static final int PACMAN_STEP = 2;
    /** Score increment when the user eats a pac-dot */
    private static final int PACDOT_SCORE_INCREMENT = 10;
    /** Score increment when the user eats a diamond */
    private static final int DIAMOND_SCORE_INCREMENT = 50;

    /** Score increment when the user eats an enemy */
    private static final int ENEMY_SCORE_INCREMENT = 200;
    // the number of lifes the user have, initially
    private static final int INITIAL_LIFES = 3;
    // umber of pac-dots to win the game
    private static final int FOOD_COUNT = 192;
    // number of monsters in the game
    private static final int GHOST_COUNT = 4;

    /** Direction Constants in X-co-ordinate */
    private static final int[] GO_DIRECTION_X = {-PACMAN_STEP, PACMAN_STEP, 0, 0};
    /** Direction Constants in Y-co-ordinate */
    private static final int[] GO_DIRECTION_Y = {0, 0, -PACMAN_STEP, PACMAN_STEP};
    /** the value of the undefined direction */
    private static final int UNDEFINED_DIRECTION = -1;

    /** the sequence of monster live states */
    private static final int []MONSTER_LIVE_SEQ = {0, 1};
    /** the sequence of monster pale states */
    private static final int []MONSTER_PALE_SEQ = {2, 3};
    /** the sequence of monster pale states */
    private static final int []MONSTER_EYE_SEQ = {4};
    /** Initial locations of the ghosts _ X */
    int []GHOST_POSITION_X = {8, 18, 4, 13};
    /** Initial locations of the ghosts _ Y */
    int []GHOST_POSITION_Y = {3, 4 , 9, 11};
    /** player frame sequence */
    private static final int []PLAYER_PLAY_SEQ = {0, 0, 1, 1, 2, 2, 3, 3, 2, 2, 1, 1};
    /** Player die sequence animation */
    private static final int []PLAYER_DIE_SEQ = {4, 5, 6};

    /** The sleep time of the game (time between processing in game loop) */
    private static int SLEEP_TIME = 20;

    /** Reference to the midlet to send exit events. */
    private PacManMIDlet iMidlet;
    /** Command to exit the game. */
    private Command iCmdExit;
    /** Command to pause the game. */
    private Command iCmdPause;
    /** Command to restart the game. */
    private Command iCmdRestart;
    /** Command to resume the game. */
    private Command iCmdResume;
    /** Command to start the game. after end */
    private Command iCmdStart;
    /** Width of the screen. */
    private int iWidth;
    /** Height of the screen. */
    private int iHeight;
    /** Layer manager for managing our graphics objects (sprites and layers). */
    private LayerManager iLayerManager;
    /** Image of the player. */
    private Sprite iPlayerSprite;
    /** Layer containing the background. */
    private TiledLayer iBgLayer;
    /** Current score of the player. */
    private int iScore;
    /** The lifes left for the player */
    private int iLeftLifes = INITIAL_LIFES; // TODO fix
    /** number of eaten dots from the dots */
    private int iEatenPacDots = 0;
    /** Current x-coordinate of the player (center of the image) */
    private int iX;
    /** Current y-coordinate of the player (center of the image) */
    private int iY;
    /** previous x direction */
    private int iLastDirectionX = 0;
    /** previous y direction */
    private int iLastDirectionY = 0;
    /** X window scroll for the background layer */
    private int iWindowScrollX = 0;
    /** Y window scroll for the background layer */
    private int iWindowScrollY = 0;
    /** save the user's intent in direction X */
    private int iIntentX;
    /** save the user's intent in direction Y */
    private int iIntentY;
    /** save the trasform associated with the direction */
    private int iWantTrasform = Sprite.TRANS_NONE;

    /** true when the game is stopped */
    private boolean iGameStopped = true;
    /** true when the game is ended and needs new game */
    private boolean iGameEnded = false;

    // the food sprites
    private Sprite[] iFoodSprites;

    // the diamond sprites
    private Sprite[] iDiamondSprites;
    private GhostSprite[] iGhostSprites;

    private TimerManager iTimerManager;

    /**
     * Tile-IDs for the background map.
     * Created using the mappy-tool.
     */
    private final static char bg_map[][] =
    {	//                                  :
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, // border	1
        { 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 1 }, // 1st row	2
        { 1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1 }, //			3
        { 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1 }, //			4
        { 1, 3, 1, 1, 1, 1, 3, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 3, 1, 1, 1, 1, 3, 1 }, //			5
        { 1, 3, 3, 3, 3, 3, 3, 1, 3, 3, 3, 3, 1, 3, 3, 3, 3, 1, 3, 3, 3, 3, 3, 3, 1 }, //			6
        { 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1 }, //			7
        { 1, 1, 1, 1, 1, 1, 3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 1, 1, 1, 1, 1, 1 }, //			8
        { 1, 1, 1, 1, 1, 1, 3, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 3, 1, 1, 1, 1, 1, 1 }, //			9
        { 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1 }, //			10
        { 1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1 }, //			11
        { 1, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 1 }, //			12
        { 1, 1, 1, 3, 1, 1, 3, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 3, 1, 1, 3, 1, 1, 1 }, //			13
        { 1, 3, 3, 3, 3, 3, 3, 1, 3, 3, 3, 3, 1, 3, 3, 3, 3, 1, 3, 3, 3, 3, 3, 3, 1 }, //			14
        { 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1 }, //			15
        { 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 1 }, //			16
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } // border            17
};
    /** True if the game is running. If the thread should stop, set this to false by calling stop(). */
    private boolean iIsActive = false;

    /** Creates a new instance of PlayCanvas */
    public PacManCanvas(PacManMIDlet aMidlet)
    {
        // set the mode for key input to the synchronous mode
        super(true);
        // User full screen mode for the game
        setFullScreenMode(true);
        // Save reference to the midlet to send events to it
        iMidlet = aMidlet;
        // Create the command so that the player can exit the game
        iCmdExit = new Command("Exit", Command.EXIT, 0);
        addCommand(iCmdExit);
        iCmdPause = new Command("Pause", Command.SCREEN, 1);
        addCommand(iCmdPause);
        iCmdResume = new Command("Resume", Command.SCREEN, 1);
        iCmdStart = new Command("New Game", Command.SCREEN, 2);
        iCmdRestart = new Command("Restart", Command.SCREEN, 2);
        addCommand(iCmdRestart);
        setCommandListener(this);
    }

    /**
     * Initialize the game, set member variables to the states we want and load all graphics.
     */
    public void init()
    {
        iTimerManager = new TimerManager();
        // Get properties of the screen
        iWidth = getWidth();
        iHeight = getHeight();
        // Initialize score with 0
        iScore = -10; //
        iFoodSprites = new Sprite[FOOD_COUNT];
        iDiamondSprites = new Sprite[4];
        iGhostSprites = new GhostSprite[4];
        try
        {
        // Now that all sprites and layers are loaded, create the layer manager
        // and let it manage all our graphic objects
        Image monsterImage = Image.createImage("/Monster.PNG");
        Sprite s;
        GhostSprite gs;

        iLayerManager = new LayerManager();
        Image gameImage = Image.createImage("/Game.PNG");
        // Load player sprite
        iPlayerSprite = new Sprite(Image.createImage("/Pacman.PNG"), 32, 32);

        // Set reference pixel to the center of the player sprite
        iPlayerSprite.defineReferencePixel(iPlayerSprite.getWidth() / 2, iPlayerSprite.getHeight() / 2);
        iPlayerSprite.setFrameSequence(PLAYER_PLAY_SEQ);
        iLayerManager.append(iPlayerSprite);

        // monster settings
        for (int i = 0; i < GHOST_COUNT; i++)
        {
            gs = new GhostSprite(monsterImage, 32, 32, GHOST_POSITION_X[i] * 32, GHOST_POSITION_Y[i] * 32);
            iGhostSprites[i] = gs;
            iLayerManager.append(gs);
        }

        // food sprites
        int k = 0, l = 0;
        for (int i = 0; i < bg_map.length; i++)
        {
            for (int j = 0; j < bg_map[0].length; j++)
            {
                if (bg_map[i][j] == 3)
                {
                    s = new Sprite(gameImage, 32, 32);
                    s.setPosition(j*32, i*32);
                    s.defineCollisionRectangle(11, 11, 10, 10);
                    s.setFrame(2);
                    iLayerManager.append(s);
                    iFoodSprites[k++] = s;
                    bg_map[i][j] = 0;
                }
                else if (bg_map[i][j] == 2)
                {
                    s = new Sprite(gameImage, 32, 32);
                    s.setPosition(j*32, i*32);
                    s.defineCollisionRectangle(11, 11, 10, 10);
                    s.setFrame(1);
                    iLayerManager.append(s);
                    iDiamondSprites[l++] = s;
                    bg_map[i][j] = 0;
                }
            }
            iBgLayer = initBackground();
        }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

        // layer manager settings
        iWindowScrollX = iBgLayer.getWidth() / 2 - iWidth / 2;
        iWindowScrollY = iBgLayer.getHeight() - iHeight;
        iLayerManager.setViewWindow(iWindowScrollX, iWindowScrollY, iWidth, iHeight);
        // Order of appending layers defines z-order (for visibility)
        // Load background layer

        // add background
        iBgLayer.setPosition(0, 0);
        iLayerManager.append(iBgLayer);

        // Define initial positions
        iX = iBgLayer.getWidth() / 2 - iPlayerSprite.getWidth() / 2;
        iY = (int) (iBgLayer.getHeight() - iPlayerSprite.getHeight() * 2);
        // Set player to the horizontal center of the screen and in the lower part of it
        iPlayerSprite.setPosition(iX, iY);

        // Start our game loop
        iIsActive = true;
        Thread t1 = new Thread(this);
        t1.start();
        Thread t2 = new Thread(new SoundPlayer("/pacman_intro.wav", this));
        t2.start();
    }

    public void run()
    {
        while (iIsActive)
        {
            // Handle keys - get current state
            // Process the keys to move the player or fire a shot
            processKeys(getKeyStates());
            // Update world (move bullets, check for collisions)
            updateWorld();

            // Do drawing (draw the current game state to the back buffer)
            drawWorld();

            // Redraw Screen (send back buffer to the visible screen)
            flushGraphics();

            // Give other threads a chance to run
            // Of course a real game should measure the time and between
            // each run of the game loop and update the world accordingly.
            // This version will run at a different speed on every handset.
            try
            {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Do the appropriate action according to the specified game key states.
     *
     * @param aKeyStates game key states that we got from the game canvas.
     */
    private void processKeys(int aKeyStates)
    {
        switch (aKeyStates)
        {
            case LEFT_PRESSED:
                // Move the player left by STEP pixels.
                iWantTrasform = Sprite.TRANS_MIRROR;
                movePlayer(-PACMAN_STEP, 0, true);
                break;
            case RIGHT_PRESSED:
                // Move the player right by STEP pixels.
                iWantTrasform = Sprite.TRANS_NONE;
                movePlayer(PACMAN_STEP, 0, true);
                break;
            case UP_PRESSED:
                // Move the player up by STEP pixels.
                iWantTrasform = Sprite.TRANS_ROT270;
                movePlayer(0, -PACMAN_STEP, true);
                break;
            case DOWN_PRESSED:
                // Move the player down by STEP pixels.
                iWantTrasform = Sprite.TRANS_ROT90;
                movePlayer(0, PACMAN_STEP, true);
                break;
            default:
                if (! iGameStopped)
                {
                    movePlayer(iLastDirectionX, iLastDirectionY, false);
                }
        }
    }

    /**
     * Logic for updating the state of the world. Moves bullets and checks for collisions.
     */
    private void updateWorld()
    {
        GhostSprite ghost;
        iPlayerSprite.defineCollisionRectangle(12, 12, 8, 8);
        if (! iGameStopped && ! iGameEnded) // move enemies if game is going
        {
             moveEnemy();
        }
        else if (iPlayerSprite.getFrameSequenceLength() == PLAYER_DIE_SEQ.length)
        {
            iPlayerSprite.nextFrame();
        }

        Random rnd = new Random();
        boolean done = false;
        for (int i = 0; i < GHOST_COUNT && ! done; i++)
        {
            ghost = iGhostSprites[i];
            ghost.defineCollisionRectangle(10, 10, 12, 12);
            if (iPlayerSprite.collidesWith(ghost, false))
            {
                done = true;
                if (ghost.isSleeping())
                {
                    iScore += ENEMY_SCORE_INCREMENT;
                    int seconds = rnd.nextInt(3) + 3;
                    ghost.setFrameSequence(MONSTER_EYE_SEQ);
                    iTimerManager.scheduleWakeUpGhost(ghost, seconds);
                }
                else
                {
                    iGameStopped = true;
                    iLastDirectionX = 0;
                    iLastDirectionY = 0;
                    iIntentX = 0;
                    iIntentY = 0;
                    iLeftLifes--;
                    iPlayerSprite.setTransform(Sprite.TRANS_NONE);
                    iPlayerSprite.setFrameSequence(PLAYER_DIE_SEQ);
                    SLEEP_TIME = 400;
                    for (int j = 0; j < GHOST_COUNT; j++)
                    {
                        iGhostSprites[j].setVisible(false);
                    }
                    if (iLeftLifes > 0)
                    {
                        iTimerManager.scheduleInitBack(SLEEP_TIME * PLAYER_DIE_SEQ.length);
                    }
                    else
                    {
                        iTimerManager.scheduleLoseGame(SLEEP_TIME * PLAYER_DIE_SEQ.length);
                    }
                }
            }
            ghost.defineCollisionRectangle(0, 0, 32, 32);
        }
        for (int i = 0; i < iFoodSprites.length; i++)
        {
            // check collision with pac dots
            if (iFoodSprites[i].getFrame() != 3 && iPlayerSprite.collidesWith(iFoodSprites[i], false))
            {
                iScore += PACDOT_SCORE_INCREMENT;
                iFoodSprites[i].setFrame(3);
                iEatenPacDots++;
            }
        }
        // power palletes
        for (int i = 0; i < iDiamondSprites.length; i++)
        {
            if (iDiamondSprites[i].getFrame() != 3 && iPlayerSprite.collidesWith(iDiamondSprites[i], false))
            {
                iScore += DIAMOND_SCORE_INCREMENT;
                iDiamondSprites[i].setFrame(3);
                if (iTimerManager.iScheduleWakeUpAllGhostsInstance != null)
                    iTimerManager.iScheduleWakeUpAllGhostsInstance.cancel();
                iTimerManager.scheduleWakeUpAllGhosts(iGhostSprites, rnd.nextInt(5) + 5);
                paleGhosts();
            }
        }
        if (iEatenPacDots == FOOD_COUNT)
        {
            endGame(true);
        }
        // update rest and increase score
    }

    /**
     * Reset the enemies and player location to their original location (e.g. lose life)
     */
    private void reset()
    {
        SLEEP_TIME = 20;
        for (int i = 0; i < GHOST_COUNT; i++)
        {
            iGhostSprites[i].setPosition(GHOST_POSITION_X[i] * 32, GHOST_POSITION_Y[i] * 32);
            iGhostSprites[i].wakeUp();
            iGhostSprites[i].resetDirection();
            iGhostSprites[i].setVisible(true);
        }
        iPlayerSprite.setFrameSequence(PLAYER_PLAY_SEQ);
        // Define initial positions
        iX = iBgLayer.getWidth() / 2 - iPlayerSprite.getWidth() / 2;
        iY = (int) (iBgLayer.getHeight() - iPlayerSprite.getHeight() * 2);
        // Set player to the horizontal center of the screen and in the lower part of it
        iPlayerSprite.setPosition(iX, iY);
        iPlayerSprite.setTransform(Sprite.TRANS_NONE);
        iWantTrasform = Sprite.TRANS_NONE;
        // Set player to the horizontal center of the screen and in the lower part of it
        iWindowScrollX = iBgLayer.getWidth() / 2 - iWidth / 2;
        iWindowScrollY = iBgLayer.getHeight() - iHeight;
        iLayerManager.setViewWindow(iWindowScrollX, iWindowScrollY, iWidth, iHeight);
        iGameStopped = true;
        iGameEnded = false;
    }

    private void paleGhosts()
    {
        for (int i = 0; i < GHOST_COUNT; i++)
        {
            iGhostSprites[i].pale();
        }
    }

    private void endGame(boolean aPlayerWin)
    {
        final Display dis = Display.getDisplay(iMidlet);
        final Form form = new Form("Enter your name please");
        final TextField nameField = new TextField("First Name:", "", 10, TextField.ANY);
        form.append(nameField);
        final Command proceedCmd = new Command("Proceed", Command.SCREEN, 0);
        final Command backToGameCmd = new Command("New Game", Command.SCREEN, 1);
        form.addCommand(proceedCmd);
        form.addCommand(backToGameCmd);
        form.setCommandListener(new CommandListener()
        {
            public void commandAction(Command command, Displayable displayable)
            {
                if (command == proceedCmd)
                {
                    HighScoreRecord.saveMyScore(nameField.getString(), iScore);
                    Form highScoresForm = new Form("High Scores");
                    Vector highScores = HighScoreRecord.loadHighScores();
                    for (int i = 0, n = Math.min(5, highScores.size()); i < n; i++)
                    {
                        HighScoreRecord record = (HighScoreRecord) highScores.elementAt(i);
                        highScoresForm.append(new StringItem(record.getName(), "\"" + record.getHighScore() + "\""));
                    }
                    highScoresForm.addCommand(backToGameCmd);
                    highScoresForm.setCommandListener(new CommandListener()
                    {
                        public void commandAction(Command command, Displayable displayable)
                        {
                            if (command == proceedCmd || command == backToGameCmd)
                            {
                                dis.setCurrent(PacManCanvas.this);
                                restartGame();
                            }
                        }
                    });
                    dis.setCurrent(highScoresForm);
                }
                else if (command == backToGameCmd)
                {
                    dis.setCurrent(PacManCanvas.this);
                    restartGame();
                }
            }
        });
        dis.setCurrent(form);
        removeCommand(iCmdRestart);
        addCommand(iCmdStart);
        SLEEP_TIME = 20;
        iGameStopped = true;
        iGameEnded = true;
    }
    /**
     * Move the player on the x-axis by the specified movement amount.
     *
     *
     * @param aDistance how many pixels to move right (positive number)
     * or left (negative number).
     */
    private void movePlayer(int aDistanceX, int aDistanceY, boolean userRequest)
    {
        if (userRequest)
        {
            iGameStopped = false;
            removeCommand(iCmdResume);
            addCommand(iCmdPause);
        }
        int oldX = iX;
        int oldY = iY;
        if (userRequest)
        {
            iIntentX = aDistanceX;
            iIntentY = aDistanceY;
        }
        iPlayerSprite.defineCollisionRectangle(0, 0, 32, 32);
        iX += iIntentX;
        iY += iIntentY;
        iWindowScrollX += iIntentX;
        iWindowScrollY += iIntentY;
        iPlayerSprite.setPosition(iX, iY);

        if(iPlayerSprite.collidesWith(iBgLayer,false))
        {
            iX -= iIntentX;
            iY -= iIntentY;
            iWindowScrollX -= iIntentX;
            iWindowScrollY -= iIntentY;
            iX += iLastDirectionX;
            iY += iLastDirectionY;
            iWindowScrollX += iLastDirectionX;
            iWindowScrollY += iLastDirectionY;
            iPlayerSprite.setPosition(iX, iY);
            if (iPlayerSprite.collidesWith(iBgLayer, false))
            {
                iX -= iLastDirectionX;
                iY -= iLastDirectionY;
                iWindowScrollX -= iLastDirectionX;
                iWindowScrollY -= iLastDirectionY;
                iPlayerSprite.setPosition(iX, iY);
            }
        }
        else // couldn't apply the user intent
        {
            iPlayerSprite.setTransform(iWantTrasform);
            iLastDirectionX = iIntentX;
            iLastDirectionY = iIntentY;
        }
        // Make sure the player can't move out of the screen
        if (iWindowScrollX < 0)
        {
            iWindowScrollX = 0;
        } else if (iWindowScrollX > (iBgLayer.getWidth() - iWidth))
        {
            iWindowScrollX = iBgLayer.getWidth() - iWidth;
        } else if (iWindowScrollY < 0)
        {
            iWindowScrollY = 0;
        } else if (iWindowScrollY > (iBgLayer.getHeight() - iHeight))
        {
            iWindowScrollY = iBgLayer.getHeight() - iHeight;
        }
        iLayerManager.setViewWindow(iWindowScrollX, iWindowScrollY, iWidth, iHeight);
        if (oldX != iX || oldY != iY)
            iPlayerSprite.nextFrame();
        else
            iPlayerSprite.setFrame(1);
    }

    /**
     * Do a random movement of the enemy
     */
    private void moveEnemy()
    {
        for (int i = 0; i < GHOST_COUNT; i++)
        {
            iGhostSprites[i].moveGhost();
        }
        // random movement for the ghosts
    }

    /**
     * Do the actual drawing work to the back buffer.
     */
    private void drawWorld()
    {
        Graphics g = getGraphics();
        // Draw game contents
        iLayerManager.paint(g, 0, 20);
        // Draw score text
        g.setFont(Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE));
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, iWidth, 20);
        g.setColor(255, 255, 255);
        g.drawString("Score: " + iScore, 0, 0, Graphics.TOP | Graphics.LEFT);
//        g.drawString("PACMAN", iWidth/2 + 10, 0, Graphics.TOP | Graphics.HCENTER);
        g.drawString("Lifes: " + iLeftLifes, iWidth, 0, Graphics.TOP | Graphics.RIGHT);
    }

    /**
     * Load the background tile map that is composed of several small bitmaps.
     */
    private TiledLayer initBackground() throws IOException
    {
        final int tileCols = 25;
        final int tileRows = 17;
        final int tileSizePixels = 32;
        // Create the tiled layer for the background
        TiledLayer bgLayer = new TiledLayer(tileCols, tileRows, Image.createImage("/Game.PNG"), tileSizePixels, tileSizePixels);
        // Assign tiles according to the 1D-array
        for (int i = 0; i < tileRows; i++)
            for (int j = 0; j < tileCols; j++)
                if(bg_map[i][j]!=2&&bg_map[i][j]!=3)
                {
                    bgLayer.setCell(j, i, bg_map[i][j]);
                }
        return bgLayer;
    }

    /**
     * Stop and with that deletes the thread after its next execution.
     */
    public void stop()
    {
        iIsActive = false;
    }

    /**
     * Handle commands sent by the framework.
     */
    public void commandAction(Command command, Displayable displayable)
    {
        if (command == iCmdExit)
        {
            stop();
            iMidlet.exit();
        }
        else if (command == iCmdPause)
        {
            iGameStopped = true;
            removeCommand(iCmdPause);
            addCommand(iCmdResume);
        }
        else if (command == iCmdRestart)
        {
            restartGame();
        }
        else if (command == iCmdStart)
        {
            removeCommand(iCmdStart);
            addCommand(iCmdRestart);
            restartGame();
        }
        else if (command == iCmdResume)
        {
            iGameStopped = false;
            removeCommand(iCmdResume);
            addCommand(iCmdPause);
        }
    }

    private void restartGame()
    {
        for (int i = 0; i < FOOD_COUNT; i++)
            iFoodSprites[i].setFrame(2);
        for (int i = 0; i < 4; i++)
            iDiamondSprites[i].setFrame(1);
        iScore = -10; // due to the first eaten food
        iLeftLifes = INITIAL_LIFES;
        iEatenPacDots = 0;
        reset();
    }

    public void saveSettings()
    {
    }

    public void loadSettings()
    {
    }

    public boolean IsActive()
    {
        return iIsActive;
    }

    /**
     * GhostSprite is a sprite for the ghosts that encapsulates the AI, the ghost does for moving
     */
    private class GhostSprite extends Sprite
    {
        /** x location of the ghost */
        private int iX;
        /** y location of the ghost */
        private int iY;
        /** direction index of the ghost defined by 0, 1, 2, 3 for left, right, up and down respectively */
        private int iLastDirectionIndex;
        /** true if the ghost is sleeping or waiting to be reborned */
        private boolean iSleeping;

        /**
         * Creates a ghost sprite
         * @param aImage
         * @param aWidthFrame
         * @param aHeightFrame
         * @param aPosX
         * @param aPosY
         */
        GhostSprite(Image aImage, int aWidthFrame, int aHeightFrame, int aPosX, int aPosY)
        {
            super(aImage, aWidthFrame, aHeightFrame);
            setPosition(aPosX, aPosY);
            resetDirection();
            iSleeping = false;
            setFrameSequence(MONSTER_LIVE_SEQ);
        }

        /**
         * Sets the position of the sprite
         * @param aPosX
         * @param aPosY
         * @see Sprite#setPosition(int, int) 
         */
        public void setPosition(int aPosX, int aPosY)
        {
            super.setPosition(aPosX, aPosY);
            this.iX = aPosX;
            this.iY = aPosY;
        }

        /**
         * Moves the ghost through the page and animate it
         */
        public void moveGhost()
        {
            nextFrame();
            chooseDirection();
        }

        private void chooseDirection()
        {
            int[]canGo = new int[4];
            int canGoK;
            Random rnd = new Random();
            if ((iX % 32 == 0) && (iY % 32 == 0))
            {
                canGoK = 0;
                for (int i = 0; i < 4; i++)
                {
                    iX += GO_DIRECTION_X[i] * (iSleeping ? 0.5 : 1 );
                    iY += GO_DIRECTION_Y[i] * (iSleeping ? 0.5 : 1 );
                    setPosition(iX, iY);
                    // 0 (00) is opposite to 1 (01)
                    // 2 (10) is opposite to 3 (11)
                    // both combinations has XOR = 1
                    if (! this.collidesWith(iBgLayer,false) && ((iLastDirectionIndex ^ i) != 1 || rnd.nextInt(5) == 1))
                    {
                        canGo[canGoK++] = i;
                    }
                    iX -= GO_DIRECTION_X[i] * (iSleeping ? 0.5 : 1);
                    iY -= GO_DIRECTION_Y[i] * (iSleeping ? 0.5 : 1);
                }
                iLastDirectionIndex = canGo[rnd.nextInt(canGoK)];
            }
            iX += GO_DIRECTION_X[iLastDirectionIndex] * (iSleeping ? 0.5 : 1);
            iY += GO_DIRECTION_Y[iLastDirectionIndex] * (iSleeping ? 0.5 : 1);
            setPosition(iX , iY);
        }

        /**
         * resets the direction of the ghost to UNDEFINED_DIRECTION
         */
        public void resetDirection()
        {
            iLastDirectionIndex = UNDEFINED_DIRECTION;
        }

        public void pale()
        {
            iSleeping = true;
            setFrameSequence(MONSTER_PALE_SEQ);
            // geek maths (reverse direction)
            int i = iLastDirectionIndex, j;
            j = i/2;  j *= 2; j += (i + 1) % 2;
            iLastDirectionIndex = j;
        }

        public void wakeUp()
        {
            iSleeping = false;
            if (iX % 2 == 1) iX++;
            if (iY % 2 == 1) iY++;
            setFrameSequence(MONSTER_LIVE_SEQ);
        }

        public boolean isSleeping()
        {
            return iSleeping;
        }
    }
    private class TimerManager
    {
        Timer iScheduleWakeUpGhostInstance;
        Timer iScheduleWakeUpAllGhostsInstance;
        Timer iScheduleInitBackInstance;
        Timer iScheduleLoseGameInstance;

        public void scheduleWakeUpGhost(final GhostSprite aGhost, int aSeconds)
        {
            iScheduleWakeUpGhostInstance = new Timer();
            iScheduleWakeUpGhostInstance.schedule(new TimerTask()
            {
                public final void run()
                {
                    aGhost.wakeUp();
                }
            }, aSeconds*1000);
        }
        public void scheduleWakeUpAllGhosts(final GhostSprite []aGhosts, int aSeconds)
        {
            iScheduleWakeUpAllGhostsInstance = new Timer();
            iScheduleWakeUpAllGhostsInstance.schedule(new TimerTask()
            {
                public final void run()
                {
                    for (int i = 0; i < GHOST_COUNT; i++)
                    {
                        if (aGhosts[i].isSleeping())
                        {
                            aGhosts[i].wakeUp();
                        }
                    }
                }
            }, aSeconds*1000);
        }

        public void scheduleInitBack(int aMilliSeconds)
        {
            iScheduleInitBackInstance = new Timer();
            iScheduleInitBackInstance.schedule(new TimerTask()
            {
                public final void run()
                {
                    reset();
                }
            }, aMilliSeconds);
        }

        public void scheduleLoseGame(int aMilliSeconds)
        {
            iScheduleLoseGameInstance = new Timer();
            iScheduleLoseGameInstance.schedule(new TimerTask()
            {
                public final void run()
                {
                    endGame(false);
                }
            }, aMilliSeconds);
        }
    }
}
