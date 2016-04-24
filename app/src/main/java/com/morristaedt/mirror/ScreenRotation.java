package com.morristaedt.mirror;

import android.content.Intent;
import android.provider.AlarmClock;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Lilyheart on 4/24/2016.
 */
public class ScreenRotation extends AppCompatActivity {
    private static int currentScreenNum;

    public ScreenRotation()
    {
        currentScreenNum = 0;
    }

    public int getCurrentScreenNum()
    {
        return currentScreenNum;
    }

    public void setCurrentScreenNum(int currentScreenNum)
    {
        currentScreenNum = currentScreenNum;
    }

    public static String getNextScreen()
    {
        Intent intent;
        switch (currentScreenNum) {
            // Hello Screen
            case 0:
                currentScreenNum++;
                return "MirrorXKCD.class";
            // XKCD Screen
//            case 2:
//                currentScreenNum++;
//                break;
//            // Septa Screen
//            case 3:
//                currentScreenNum++;
//                break;
//            case 4:
//                currentScreenNum++;
//                break;
            // Withings Screen
            default:
                currentScreenNum = 1;
                return "MirrorActivity.class";
        }
    }
}
