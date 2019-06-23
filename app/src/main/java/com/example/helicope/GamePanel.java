package com.example.helicope;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

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



    public GamePanel(Context context, int best) {
        super(context);
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
}
