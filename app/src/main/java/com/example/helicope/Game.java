package com.example.helicope;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

public class Game extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);


        requestWindowFeature(Window.FEATURE_NO_TITLE);


        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int currentHighScore = sharedPref.getInt("best", 0);

        GamePanel panel = new GamePanel(this, currentHighScore);


        panel.setHighScoreListener(new GamePanel.HighScoreListener() {
            @Override
            public void onHighScoreUpdated(int best) {

                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("best", best);
                editor.commit();
            }

        });
        setContentView(panel);
    }

}
