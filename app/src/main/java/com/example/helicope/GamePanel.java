package com.example.helicope;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback{


    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;

    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<SmokePuff> smokePuffs;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topBorders;
    private ArrayList<BotBorder> botBorders;
    private long smokeStartTime;
    private long missileStartTime;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;

    private int progressDenominator = 20;

    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean disappear;
    private boolean started;
    private int bestScore;
    int count = 5;

    private HighScoreListener mHighScoreListener;
    public interface HighScoreListener {
        void onHighScoreUpdated(int best);
    }



    public GamePanel(Context context, int best)
    {
        super(context);
        this.bestScore = best;

        this.mHighScoreListener = null;
        getHolder().addCallback(this);
        setFocusable(true);
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
        smokePuffs = new ArrayList<SmokePuff>();
        missiles = new ArrayList<Missile>();
        topBorders = new ArrayList<TopBorder>();
        botBorders = new ArrayList<BotBorder>();

        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();

        thread = new MainThread(getHolder(), this);

        thread.setRunning(true);
        thread.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;
        while (retry && counter < 1000) {
            counter++;
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!player.isPlaying() && newGameCreated && reset) {
                player.setPlaying(true);
                player.setUp(true);
            }
            if (player.isPlaying()) {
                if (!started)
                    started = true;

                reset = false;
                player.setUp(true);
            }
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }


        return super.onTouchEvent(event);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        final float scaleFactorX = getWidth() / (WIDTH * 1.f);
        final float scaleFactorY = getHeight() / (HEIGHT * 1.f);

        if (canvas != null) {
            final int savedState = canvas.save();

            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if (!disappear)
                player.draw(canvas);

            for (SmokePuff sp : smokePuffs) {
                sp.draw(canvas);
            }

            for (Missile m : missiles) {
                m.draw(canvas);
            }


            for (TopBorder tb : topBorders) {
                tb.draw(canvas);
            }

            for (BotBorder bb : botBorders) {
                bb.draw(canvas);
            }


            if (started) {
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }

    }
    public void updateTopBorder(){

        if (player.getScore() % 50 == 0) {
            topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    topBorders.get(topBorders.size() - 1).getX() + 20, 0,
                    (int) ((rand.nextDouble() * (maxBorderHeight)) + 1)));
        }

        for (int i = 0; i < topBorders.size(); i++) {
            topBorders.get(i).update();
            if (topBorders.get(i).getX() < -20) {
                topBorders.remove(i);

                if (topBorders.get(topBorders.size() - 1).getHeight() >= maxBorderHeight) {
                    topDown = false;
                }
                if (topBorders.get(topBorders.size() - 1).getHeight() <= minBorderHeight) {
                    topDown = true;
                }


                if (topDown) {
                    topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topBorders.get(topBorders.size() - 1).getX() + 20,
                            0, topBorders.get(topBorders.size() - 1).getHeight() + 1));
                }

                else {
                    topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topBorders.get(topBorders.size() - 1).getX() + 20,
                            0, topBorders.get(topBorders.size() - 1).getHeight() - 1));
                }
            }
        }
    }

    public void updateBottomBorder(){

        if (player.getScore() % 40 == 0) {
            botBorders.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botBorders.get(botBorders.size() - 1).getX() + 20,
                    (int) ((rand.nextDouble() * maxBorderHeight) + (HEIGHT - maxBorderHeight))));
        }


        for (int i = 0; i < botBorders.size(); i++) {
            botBorders.get(i).update();


            if (botBorders.get(i).getX() < -20) {
                botBorders.remove(i);


                if (botBorders.get(botBorders.size() - 1).getY() <= HEIGHT - maxBorderHeight) {
                    botDown = true;
                }
                if (botBorders.get(botBorders.size() - 1).getY() >= HEIGHT - minBorderHeight) {
                    botDown = false;
                }

                if (botDown) {
                    botBorders.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            botBorders.get(botBorders.size() - 1).getX() + 20, botBorders.get(botBorders.size() - 1).getY() + 1));

                } else {
                    botBorders.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            botBorders.get(botBorders.size() - 1).getX() + 20, botBorders.get(botBorders.size() - 1).getY() - 1));
                }
            }
        }
    }

    public void newGame(){
        if (player.getScore() > bestScore) {
            bestScore = player.getScore();
            if (mHighScoreListener != null)
                mHighScoreListener.onHighScoreUpdated(bestScore);
        }

        disappear = false;
        botBorders.clear();
        topBorders.clear();
        missiles.clear();
        smokePuffs.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;
        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT / 2);

        for (int i = 0; i * 20 < WIDTH + 40; i++) {

            if (i == 0) {
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, 0, 10));
            } else {
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, 0, topBorders.get(i - 1).getHeight() + 1));
            }

        }


        for (int i = 0; i * 20 < WIDTH + 40; i++) {


            if (i == 0) {
                botBorders.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, HEIGHT - minBorderHeight));
            }

            else {
                botBorders.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, botBorders.get(i - 1).getY() - 1));

            }
        }
        newGameCreated = true;

    }
    public void drawText(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: " + (player.getScore() * 3), 10, HEIGHT - 10, paint);
        canvas.drawText(" LIFE: " + count, 205, HEIGHT - 10, paint);
        canvas.drawText("HIGHEST: " + (bestScore * 3), WIDTH - 215, HEIGHT - 10, paint);

        if (!player.isPlaying() && newGameCreated && reset) {
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH / 2 - 50, HEIGHT / 2, paint1);
            count = 5;

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH / 2 - 50, HEIGHT / 2 + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH / 2 - 50, HEIGHT / 2 + 40, paint1);
        }
    }
    public void update(){
        if (player.isPlaying()) {

            if (botBorders.isEmpty()) {
                player.setPlaying(false);
                return;
            }
            if (topBorders.isEmpty()) {
                player.setPlaying(false);
                return;
            }

            bg.update();
            player.update();



            maxBorderHeight = 30 + player.getScore() / progressDenominator;

            if (maxBorderHeight > HEIGHT / 4)
                maxBorderHeight = HEIGHT / 4;
            minBorderHeight = 5 + player.getScore() / progressDenominator;


            for (int i = 0; i < botBorders.size(); i++) {
                if (collision(botBorders.get(i), player)) {
                    player.setPlaying(false);
                    break;
                }
            }

            for (int i = 0; i < topBorders.size(); i++) {
                if (collision(topBorders.get(i), player)) {
                    player.setPlaying(false);
                    break;
                }
            }


            this.updateTopBorder();


            this.updateBottomBorder();


            long missileElapsed = (System.nanoTime() - missileStartTime) / 1000000;
            if (missileElapsed > (2000 - player.getScore() / 4)) {

                if (missiles.size() == 0) {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),
                            R.drawable.missile), WIDTH + 10, HEIGHT / 2, 45, 15, player.getScore(), 13));
                } else {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),
                            R.drawable.missile), WIDTH + 10, (int) (rand.nextDouble() * (HEIGHT - (maxBorderHeight * 2)) + maxBorderHeight),
                            45, 15, player.getScore(), 13));
                }

                missileStartTime = System.nanoTime();
            }

            for (int i = 0; i < missiles.size(); i++) {
                //update missile
                missiles.get(i).update();

                if (collision(missiles.get(i), player)) {

                    if(count<=5 && count!=0){
                        missiles.remove(i);
                        player.setPlaying(true);
                        count--;
                    }
                    else if(count == 0) {
                        player.setPlaying(false);
                        count =5;
                        break;
                    }

                }

                if (missiles.get(i).getX() < -100) {
                    missiles.remove(i);
                    break;

                }
            }


            long elapsed = (System.nanoTime() - smokeStartTime) / 1000000;
            if (elapsed > 120) {
                smokePuffs.add(new SmokePuff(player.getX(), player.getY() + 10));
                smokeStartTime = System.nanoTime();
            }

            for (int i = 0; i < smokePuffs.size(); i++) {
                smokePuffs.get(i).update();
                if (smokePuffs.get(i).getX() < -10) {
                    smokePuffs.remove(i);
                }
            }
        } else {
            player.resetDY();
            if (!reset) {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                disappear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion),
                        player.getX(), player.getY() - 30, 100, 100, 25);
            }
            explosion.update();
            long resetElapsed = (System.nanoTime() - startReset) / 1000000;
            if (resetElapsed > 2500 && !newGameCreated) {
                newGame();
            }

        }
    }

    private boolean collision(GameObject a, GameObject b) {
        if (Rect.intersects(a.getRectangle(), b.getRectangle())) {
            return true;
        }
        return false;
    }
    public void setHighScoreListener(HighScoreListener listener) {
        this.mHighScoreListener = listener;
    }
}
