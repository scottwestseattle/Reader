package com.e.rhino;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

public class UserPreferences {

    public static int mProgramId = Integer.MIN_VALUE;
    public static int mSessionId = Integer.MIN_VALUE;

    public static void save(AppCompatActivity context) {
        SharedPreferences preferences = context.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("current_program_id", mProgramId);
        editor.putInt("current_session_id", mSessionId);
        editor.commit();
    }

    public static void load(AppCompatActivity context) {
        SharedPreferences preferences = context.getPreferences(Context.MODE_PRIVATE);
        mProgramId = preferences.getInt("current_program_id", Integer.MIN_VALUE);
        mSessionId = preferences.getInt("current_session_id", Integer.MIN_VALUE);
    }

    public static void clear(AppCompatActivity context) {
        mProgramId = Integer.MIN_VALUE;
        mSessionId = Integer.MIN_VALUE;
        save(context);
    }
}
