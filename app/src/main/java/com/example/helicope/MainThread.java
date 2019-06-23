package com.example.helicope;

import android.view.SurfaceHolder;

public class MainThread extends Thread {

    private int FPS = 20;
    private boolean running;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;

    public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }


    @Override
    public void run()    {
        long startTime;
        long timeMillis;
        long waitTime;
        int frameCount = 0;
        long targetTime = 1000 / FPS;

        while (running){
            
        }
    }
}
