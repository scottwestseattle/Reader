package com.e.rhino.exercises;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;

import com.e.rhino.R;
import com.e.rhino.RssReader;
import com.e.rhino.Speech;
import com.e.rhino.UserPreferences;
import com.e.rhino.exercises.content.ExerciseContent;
import com.e.rhino.history.content.HistoryContent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.net.URLEncoder;
import java.util.List;

public class ExercisesActivity extends AppCompatActivity  implements StartFragment.OnListFragmentInteractionListener {

    private Toolbar mToolbar;
    public static ExerciseContent exercises = null;
    public int currentExerciseIndex = -1;
    public int programId = -1;
    public String programName = "";
    public int sessionId = -1;
    public String sessionName = "";

    @Override
    public void onListFragmentInteraction(ExerciseContent.ExerciseItem exerciseItem) {
        //
        // todo: handle click on an item in the Exercise list: edit exercise
        //
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // get the data
        loadSessionInfo(getIntent());

        // get the data
        exercises = new ExerciseContent(this.sessionId);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        // set title and subtitle
        String title = programName + ": " + this.sessionName;
        String subTitle = exercises.exerciseList.size() + " section";

        ActionBar ab = getSupportActionBar();
        ab.setTitle(title);
        ab.setSubtitle(subTitle);

        // load the first fragment
        loadFragment("StartFragment");

        //
        // set up the bottom fab buttons
        //
        FloatingActionButton fabPlay = findViewById(R.id.fabPlay);
        fabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get the active fragment so we know which action to perform
                Fragment fragment = getActiveFragment();

                if (fragment instanceof StartFragment) {
                    loadFragment("ReaderFragment");
                } else if (fragment instanceof ReaderFragment) {
                    boolean paused = ((ReaderFragment) fragment).onFabPlayPauseClicked();
                    setFabPlayIcon(paused);
                } else if (fragment instanceof ExerciseFragment) {
                    boolean paused = ((ExerciseFragment) fragment).onFabPlayPauseClicked();
                    setFabPlayIcon(paused);
                } else if (fragment instanceof FinishedFragment) {
                    reset();
                    loadFragment("StartFragment");
                }
            }
        });

        FloatingActionButton fabNext = findViewById(R.id.fabNext);
        fabNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get the active fragment so we know which action to perform
                Fragment fragment = getActiveFragment();

                if (fragment instanceof StartFragment) {
                    loadFragment("ReaderFragment");
                } else if (fragment instanceof ReaderFragment) {
                    boolean paused = ((ReaderFragment) fragment).onFabNextClicked();
                    setFabPlayIcon(paused);
                }
            }
        });

        FloatingActionButton fabEnd = findViewById(R.id.fabEnd);
        fabEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get the active fragment so we know which action to perform
                reset();
                boolean showPlayIcon = true;
                Fragment fragment = getActiveFragment();

                if (fragment instanceof StartFragment) {
                    end();
                } else if (fragment instanceof ReaderFragment) {
                    Speech.speak("Terminando...", TextToSpeech.QUEUE_FLUSH);
                    setFabPlayIcon(showPlayIcon);
                    ((ReaderFragment) fragment).onHardStop();
                }
            }
        });

        FloatingActionButton fabMute = findViewById(R.id.fabMute);
        setFabMuteIcon(Speech.isMuted());
        fabMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Speech.setMuted(!Speech.isMuted());
                setFabMuteIcon(Speech.isMuted());
            }
        });

        FloatingActionButton fabFastForward = findViewById(R.id.fabFastForward);
        fabFastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the active fragment so we know which action to perform
                Fragment fragment = getActiveFragment();
                if (fragment instanceof ReaderFragment) {
                    ((ReaderFragment) fragment).onFabPreviousClicked();
                } else if (fragment instanceof ExerciseFragment) {
                    ((ExerciseFragment) fragment).onFabFastForwardClicked();
                }
            }
        });
    }

    private void loadSessionInfo(Intent intent) {
        this.programId = intent.getIntExtra("courseId", -1);
        this.programName = intent.getStringExtra("courseName");
        this.sessionId = intent.getIntExtra("sessionId", -1);
        this.sessionName = intent.getStringExtra("sessionName");
    }

    public void onAddSecondsButtonClick(View view) {
        addSeconds(5);
    }

    public void onSubtractSecondsButtonClick(View view) {
        addSeconds(-5);
    }

    private void addSeconds(int seconds)
    {
    }

    public void loadFragment(String tag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = fm.findFragmentByTag(tag);
        if (null == fragment) {
            showFabButton(R.id.fabFastForward, false);
            switch(tag) {
                case "StartFragment":
                    fragment = new StartFragment();
                    break;
                case "ReaderFragment":
                    showFabButton(R.id.fabFastForward, true);
                    fragment = new ReaderFragment();
                    break;
                case "ExerciseFragment":
                    fragment = new ExerciseFragment();
                    break;
                case "FinishedFragment":
                    saveHistory();
                    fragment = new FinishedFragment();
                    break;
                default:
                    break;
            }
        }

        ft.replace(R.id.fragment_holder, fragment);
        ft.commit();
    }

    private void saveHistory() {

        // save preferences locally
        saveUserPreferences();

        // save the history on the server
        String url = "https://spanish50.com/history/add-public/";
        int totalSeconds = (int) this.exercises.getTotalSeconds();
        try {
            url += URLEncoder.encode(this.programName, "utf-8") + "/";
            url += this.programId + "/";
            url += URLEncoder.encode(this.sessionName, "utf-8") + "/";
            url += this.sessionId + "/";
            url += totalSeconds;
        }
        catch(Exception e)
        {
            Log.e("Exercise", "Error encoding url: " + e.getMessage());
        }

        RssReader.ping(url);

        HistoryContent.clear(); // clear the history so it will have to reload for the next view
    }

    private void saveUserPreferences() {
        UserPreferences.mProgramId = this.programId;
        UserPreferences.mSessionId = this.sessionId;
    }

    public ExerciseContent getExercises()
    {
        return exercises;
    }

    public void reset()
    {
        this.currentExerciseIndex = -1;
    }

    public void end() {
        finish();
        return;
    }

    private Fragment getActiveFragment()
    {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = (null != fm) ? fm.getFragments() : null;
        Fragment fragment = (null != fragments && fragments.size() > 0) ? fragments.get(0) : null;

        return fragment;
    }

    public void setFabPlayIcon(boolean paused) {
        setFabButtonIcon(R.id.fabPlay,
                paused  ? R.drawable.fab_play
                        : android.R.drawable.ic_media_pause);
    }

    public void setFabMuteIcon(boolean muted) {
        setFabButtonIcon(R.id.fabMute,
                muted ? android.R.drawable.ic_lock_silent_mode_off
                        : android.R.drawable.ic_lock_silent_mode);
    }

    public void setFabButtonIcon(int buttonId, int buttonIcon) {
        FloatingActionButton fabButton = findViewById(buttonId);
        fabButton.setImageResource(buttonIcon);
    }

    public void showFabButton(int buttonId, boolean show) {
        FloatingActionButton fabButton = findViewById(buttonId);
        fabButton.setVisibility(show ? FloatingActionButton.VISIBLE : FloatingActionButton.INVISIBLE);
    }

    public boolean isLoaded() {
        return this.exercises.isLoaded();
    }

    public boolean isLastExercise() {
        return (this.currentExerciseIndex == this.exercises.exerciseList.size() - 1);
    }

    public int getTotalExercises() {
        return this.exercises.exerciseList.size();
    }

    public int getTimerSeconds() {

        int seconds = -1;

        return seconds;
    }

    public ExerciseContent.ExerciseItem getNextExercise() {

        ExerciseContent.ExerciseItem ex = null;

        if (this.exercises.isLoaded()) {

            this.currentExerciseIndex++;

            if (this.currentExerciseIndex < this.exercises.exerciseList.size()) {
                ex = this.exercises.exerciseList.get(this.currentExerciseIndex);
            }
        }

        return ex;
    }

    public ExerciseContent.ExerciseItem getCurrentExercise() {
        ExerciseContent.ExerciseItem ex = null;

        if (this.currentExerciseIndex < this.exercises.exerciseList.size())
        {
            ex = this.exercises.exerciseList.get(this.currentExerciseIndex);
        }

        return ex;
    }
}
