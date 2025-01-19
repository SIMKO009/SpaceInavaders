package sk.spsepo.spaceinvaders;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

public class SpaceInvadersView extends SurfaceView implements Runnable {
    private Context context;
    private Thread gameThread = null;
    private SurfaceHolder ourHolder;
    private volatile boolean playing;
    private boolean paused = true;

    private Canvas canvas;
    private Paint paint;

    private long timeThisFrame;
    private long fps;

    private int screenX, screenY;

    private PlayerShip playerShip;
    private Bullet[] playerBullets = new Bullet[250];
    private int numBullets;
    private int nextInvaderBullet;
    private Bullet[] invadersBullets = new Bullet[50];
    private int maxInvaderBullets = 50;
    private Invader[] invaders = new Invader[30];
    private int numInvaders = 0, score = 0;
    private int final_score = 0;
    private DefenceBrick[] bricks = new DefenceBrick[200];
    private int numBricks;

    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;

    private final int INCREASE = 10;
    private final int INIT_LIVES = 15;
    private int lives = INIT_LIVES;

    private long menaceInterval = 1000;
    private boolean uhOrOh;
    private long lastMenaceTime = System.currentTimeMillis();

    private long lastShotTime = 0;
    private static final long RELOAD_TIME = 700; // Reload time in milliseconds

    private boolean gameOver = false;

    public SpaceInvadersView(Context context, int x, int y, boolean continueGame) {
        super(context);
        this.context = context;
        ourHolder = getHolder();
        paint = new Paint();
        screenX = x;
        screenY = y;
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;
            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);
            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);
            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);
            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);
            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);
            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);
        } catch (IOException e) {
            Log.e("error", "failed to load sound files");
        }
        if (continueGame) {
            loadGameState();
        } else {
            prepareLevel(false);
        }
    }

    private void prepareLevel(boolean over) {
        paused = true;
        lives = INIT_LIVES;
        final_score = score;
        score = 0;
        numBullets = 0;

        playerShip = new PlayerShip(context, screenX, screenY);
        for (int i = 0; i < playerBullets.length; i++) playerBullets[i] = new Bullet(screenY);
        for (int i = 0; i < invadersBullets.length; i++) invadersBullets[i] = new Bullet(screenY);

        numInvaders = 0;
        for (int column = 0; column < 6; column++)
            for (int row = 0; row < 5; row++)
                invaders[numInvaders++] = new Invader(context, row, column, screenX, screenY);

        numBricks = 0;
        for (int shelterNumber = 0; shelterNumber < 4; shelterNumber++)
            for (int column = 0; column < 10; column++)
                for (int row = 0; row < 5; row++)
                    bricks[numBricks++] = new DefenceBrick(row, column, shelterNumber, screenX, screenY);

        menaceInterval = 1000;

        if (over) {
            gameOver = true;
            paused = true;
        } else {
            gameOver = false;
        }
    }

    private void saveGameState() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("gameState", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("playerX", playerShip.getX());
        editor.putInt("score", score);
        editor.putInt("lives", lives);

        for (int i = 0; i < numInvaders; i++) {
            editor.putInt("invaderX_" + i, invaders[i].getX());
            editor.putInt("invaderY_" + i, invaders[i].getY());
            editor.putBoolean("invaderVisible_" + i, invaders[i].getVisibility());
        }

        editor.apply();
    }

    private void loadGameState() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("gameState", Context.MODE_PRIVATE);

        playerShip.setX(sharedPreferences.getInt("playerX", screenX / 2));
        playerShip.setY(sharedPreferences.getInt("playerY", screenY - 135));
        score = sharedPreferences.getInt("score", 0);
        lives = sharedPreferences.getInt("lives", INIT_LIVES);

        for (int i = 0; i < numInvaders; i++) {
            invaders[i].setX(sharedPreferences.getInt("invaderX_" + i, 0));
            invaders[i].setY(sharedPreferences.getInt("invaderY_" + i, 0));
            invaders[i].setVisibility(sharedPreferences.getBoolean("invaderVisible_" + i, true));
        }
    }

    @Override
    public void run() {
        while (playing) {
            long startFrameTime = System.currentTimeMillis();
            if (!paused) update();
            draw();
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) fps = 1000 / timeThisFrame;

            if (!paused) {
                if ((startFrameTime - lastMenaceTime) > menaceInterval) {
                    if (uhOrOh) soundPool.play(uhID, 1, 1, 0, 0, 1);
                    else soundPool.play(ohID, 1, 1, 0, 0, 1);
                    lastMenaceTime = System.currentTimeMillis();
                    uhOrOh = !uhOrOh;
                }
            }
        }
    }

    private void update() {
        boolean bumped = false;
        boolean lost = false;
        playerShip.update(fps);

        for (Bullet bullet: playerBullets)
            if (bullet.getStatus())
                bullet.update(fps);

        for (Bullet bullet: invadersBullets)
            if (bullet.getStatus())
                bullet.update(fps);

        for (Invader invader: invaders)
            if (invader.getVisibility()) {
                invader.update(fps);
                if (invader.takeAim(playerShip.getX(), playerShip.getLength()))
                    if (invadersBullets[nextInvaderBullet].shoot(invader.getX() + invader.getLength() / 2, invader.getY(), 1)) {
                        nextInvaderBullet++;
                        if (nextInvaderBullet == maxInvaderBullets)
                            nextInvaderBullet = 0;
                    }
                if (invader.getX() > screenX - invader.getLength() || invader.getX() < 0)
                    bumped = true;
            }

        if (bumped) {
            for (Invader invader: invaders) {
                invader.dropDownAndReverse();
                if (invader.getY() > screenY - screenY / 10)
                    lost = true;
            }
            menaceInterval = menaceInterval - 80;
        }

        if (lost) prepareLevel(true);

        for (Bullet bullet: playerBullets) {
            if (bullet.getImpactPointY() < 0)
                bullet.setInactive();
        }

        for (Bullet bullet: invadersBullets) {
            if (bullet.getImpactPointY() > screenY)
                bullet.setInactive();
        }

        for (Bullet bullet: playerBullets)
            if (bullet.getStatus())
                for (Invader invader: invaders)
                    if (invader.getVisibility() && RectF.intersects(bullet.getRect(), invader.getRect())) {
                        invader.setInvisible();
                        bullet.setInactive();
                        soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                        score = score + INCREASE;
                        if (score == numInvaders * INCREASE) {
                            prepareLevel(false);
                            break;
                        }
                    }

        for (Bullet bullet: invadersBullets)
            if (bullet.getStatus())
                for (DefenceBrick brick: bricks)
                    if (brick.getVisibility() && (RectF.intersects(bullet.getRect(), brick.getRect()))) {
                        bullet.setInactive();
                        brick.setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }

        for (Bullet bullet: playerBullets)
            if (bullet.getStatus())
                for (DefenceBrick brick: bricks)
                    if (brick.getVisibility() && RectF.intersects(bullet.getRect(), brick.getRect())) {
                        bullet.setInactive();
                        brick.setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }

        for (int i = 0; i < invadersBullets.length; i++) {
            if (invadersBullets[i].getStatus())
                if (RectF.intersects(playerShip.getRect(), invadersBullets[i].getRect())) {
                    invadersBullets[i].setInactive();
                    lives--;
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);
                    if (lives == 0)
                        prepareLevel(true);
                }
        }
    }

    private void draw() {
        if (ourHolder.getSurface().isValid()) {
            canvas = ourHolder.lockCanvas();
            canvas.drawColor(Color.rgb(0, 0, 0));

            if (gameOver) {
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(120);
                paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
                canvas.drawText("Prehral si!", screenX / 2 - 350, screenY / 2 - 100, paint);
                paint.setTextSize(50);
                canvas.drawText("Tvoje skóre: " + final_score, screenX / 2 - 200, screenY / 2 + 100, paint);
                canvas.drawText("Stlač pre novú hru", screenX / 2 - 240, screenY / 2 + 250, paint);
            } else {
                paint.setColor(Color.argb(255, 255, 255, 255));
                canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), screenY - 135, paint);
                for (int i = 0; i < numInvaders; i++) {
                    if (invaders[i].getVisibility()) {
                        if (uhOrOh) {
                            canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                        } else {
                            canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
                        }
                    }
                }
                for (int i = 0; i < numBricks; i++) {
                    if (bricks[i].getVisibility()) {
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }
                for (int i = 0; i < playerBullets.length; i++) {
                    Bullet bullet = playerBullets[i];
                    if (bullet.getStatus()) {
                        canvas.drawRect(bullet.getRect(), paint);
                    }
                }
                for (int i = 0; i < invadersBullets.length; i++) {
                    if (invadersBullets[i].getStatus()) {
                        canvas.drawRect(invadersBullets[i].getRect(), paint);
                    }
                }

                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(50);
                paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
                canvas.drawText("SKÓRE: " + score, 10, 55, paint);
                for (int i = 0; i < lives; i++) {
                    int left = (screenX - (lives * 50 + (lives - 1) * 10)) / 2 + i * (50 + 10);
                    int right = left + 50;
                    canvas.drawRect(left, 15, right, 60, paint);
                }

                paint.setColor(Color.rgb(255, 255, 255));
                canvas.drawRect(screenX - 100, 0, screenX, 100, paint);
                paint.setColor(Color.rgb(0, 0, 0));
                paint.setTextSize(50);
                paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
                canvas.drawText("||", screenX - 80, 60, paint);
            }
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {
        playing = false;
        saveGameState();
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean run = true;
        int j = 0;
        boolean shot = false;
        while (run && j < 100) {
            j++;
            int switchInt = motionEvent.getAction() & MotionEvent.ACTION_MASK;

            boolean id2Exists = true;
            int id2 = 0;
            try {
                id2 = motionEvent.getPointerId(1);
            } catch (Exception e) {
                id2Exists = false;
            }
            switch (switchInt) {
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_DOWN:
                    paused = false;
                    if (gameOver) {
                        // Restart the game if it's over
                        gameOver = false;
                        prepareLevel(false);
                        paused = false;
                        break;
                    }
                    if (motionEvent.getX() > screenX - 100 && motionEvent.getY() < 100) {
                        paused = true;
                        saveGameState();
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                        break;
                    }

                    if (motionEvent.getY() > screenY * 3 / 4)
                        if (motionEvent.getX() > screenX / 2)
                            playerShip.setMovementState(playerShip.RIGHT);
                        else
                            playerShip.setMovementState(playerShip.LEFT);
                    else if (id2Exists && motionEvent.getY(id2) > screenY * 3 / 4)
                        if (motionEvent.getX(id2) > screenX / 2)
                            playerShip.setMovementState(playerShip.RIGHT);
                        else
                            playerShip.setMovementState(playerShip.LEFT);

                    if (motionEvent.getY() <= screenY * 3 / 4) {
                        Bullet bullet = new Bullet(screenY);
                        if (numBullets < playerBullets.length) {
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - lastShotTime >= RELOAD_TIME) {
                                playerBullets[numBullets] = bullet;
                                if (bullet.shoot(playerShip.getX() + playerShip.getLength() / 2, screenY, bullet.UP) && shot) {
                                    soundPool.play(shootID, 1, 1, 0, 0, 1);
                                    shot = false;
                                }
                                lastShotTime = currentTime; // Update the last shot time
                                numBullets++;
                            }
                        } else {
                            numBullets = 0;
                        }
                        run = false;
                    } else if (id2Exists) {
                        if (motionEvent.getY(id2) < screenY * 3 / 4) {
                            Bullet bullet = new Bullet(screenY);
                            if (numBullets < playerBullets.length) {
                                long currentTime = System.currentTimeMillis();
                                if (currentTime - lastShotTime >= RELOAD_TIME) {
                                    playerBullets[numBullets] = bullet;
                                    if (bullet.shoot(playerShip.getX() + playerShip.getLength() / 2, screenY, bullet.UP) && shot) {
                                        soundPool.play(shootID, 1, 1, 0, 0, 1);
                                        shot = false;
                                    }
                                    lastShotTime = currentTime; // Update the last shot time
                                    numBullets++;
                                }
                            } else {
                                numBullets = 0;
                            }
                            run = false;
                        }
                    }
                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    run = false;
                    if (motionEvent.getY() > screenY * 3 / 4)
                        playerShip.setMovementState(playerShip.STOPPED);
                    else if (id2Exists)
                        if (motionEvent.getY(id2) > screenY * 3 / 4)
                            playerShip.setMovementState(playerShip.STOPPED);
                    playerShip.setMovementState(playerShip.STOPPED);
                    break;
            }
        }
        return true;
    }
}