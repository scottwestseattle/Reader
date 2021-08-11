package com.e.rhino.program;

import android.graphics.Color;
import android.util.Log;

import androidx.core.app.NotificationCompatSideChannelService;

import com.e.rhino.R;
import com.e.rhino.RssReader;

import java.util.ArrayList;
import java.util.List;

public class ProgramContent {

    /**
     * The array of items from the rss feed
     */
    public static List<ProgramItem> programList = new ArrayList<ProgramItem>();

    public static int getBackgroundImageResourceId(int index) {
        int id = 0;
        int bgImagesResourceId[] = {
                R.drawable.bg_1,
                R.drawable.bg_2,
                R.drawable.bg_0,
                R.drawable.bg_3
        };

        if (index < bgImagesResourceId.length)
            id = bgImagesResourceId[index];

        return id;
    }

    public static int getBackgroundColor(int index) {
        int id = 0;
        int values[] = {
                Color.rgb(35, 87, 122), // blue
                Color.rgb(169, 61, 7),   // maroon
                Color.rgb(35, 101, 71), // green
                Color.rgb(223, 147, 1) // orange
        };

        if (index >= values.length)
        {
            index = index % values.length;
        }

        id = values[index];

        return id;
    }

    ProgramContent()
    {
        Log.i("parse", "ProgramContent started");
    }

    static {
        if (programList.size() == 0) {
            String url = "https://espdaily.com/courses/rss-reader";
            Log.i("ProgramContent", "Getting program list from rss...");
            RssReader.fetchProgramList(url, programList);
        }
        else {
            Log.i("ProgramContent", "Programs already loaded");
        }
    }
}
