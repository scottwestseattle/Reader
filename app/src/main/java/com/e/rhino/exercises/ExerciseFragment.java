package com.e.rhino.exercises;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.e.rhino.R;
import com.e.rhino.Speech;
import com.e.rhino.Tools;
import com.e.rhino.exercises.content.ExerciseContent;

public class ExerciseFragment extends Fragment {

    private boolean timerPaused = false;
    private int secondsRemaining = -1;
    private int secondsRewind = 5;
    private int secondsFastForward = secondsRewind;
    private final int second = 1000; // 1 Second
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable(){
        public void run() {

            secondsRemaining--;
            updateTimerDisplay(secondsRemaining);

            if (secondsRemaining >= 1) {
                handler.postDelayed(runnable, second); // update in 1 second
                updateTimerAudio(secondsRemaining);
            } else {
                stopTimer();
                showNextFragment();
            }
        }
    };

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exercise, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Speech.speak("Terminando", TextToSpeech.QUEUE_FLUSH);
                onHardStop();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        loadCurrent();
    }

    public void onHardStop() {
        stopTimer();
        loadFragment("StartFragment");
    }

    public boolean onFabNextClicked() {
        Speech.shutup();
        stopTimer();
        showNextFragment();

        return timerPaused;
    }

    public boolean onFabPlayPauseClicked() {

        if (timerPaused) {
            Speech.speak("Continued.  ", TextToSpeech.QUEUE_FLUSH);
            startTimer(secondsRemaining); // restart timer
        }
        else {
            Speech.speak("paused.  ", TextToSpeech.QUEUE_FLUSH);
            stopTimer();
        }

        timerPaused = !timerPaused;

        return timerPaused;
    }

    public void onFabRewindClicked() {
        this.secondsRemaining += this.secondsRewind;
        if (timerPaused)
            updateTimerDisplay(secondsRemaining);
    }

    public void onFabFastForwardClicked() {
        this.secondsRemaining -= this.secondsFastForward;
        if (this.secondsRemaining <= 0)
            this.secondsRemaining = timerPaused ? 0 : 1;

        if (timerPaused) {
            updateTimerDisplay(secondsRemaining);
        }
    }

    private void setButtonText(String text, int buttonId) {
        Button button = this.getView().findViewById(buttonId);
        if (null != button)
            button.setText(text);
    }

    private void updateTimerAudio(int seconds) {

        if (seconds > 10 && ((seconds - 1) % 10) == 0) {
            //
            // give instructions 1 seconds before 10 seconds threshold
            //
            speakInstructions();
        }
        else if (seconds > 10 && (seconds % 10) == 0) {
            //
            // give update every 10 seconds
            //
            Speech.speak(getSecondsRemainingMessage(seconds), TextToSpeech.QUEUE_ADD);
        }
        else if (seconds == 11)
        {
            //
            // give last instructions before countdown
            //
            //speakInstructions();
        }
        else if (seconds <= 10 && seconds > 0) {
            //
            // countdown last 10 seconds
            //
            Speech.speak(Integer.toString(seconds), TextToSpeech.QUEUE_FLUSH);
        }
    }

    public void speakInstructions() {
    }

    private String getSecondsRemainingMessage(int seconds) {

        String msg = Tools.getRandomString(
                "# seconds to go",
                "# seconds remaining",
                "# more seconds",
                "Go for # seconds longer",
                "Go for # more seconds",
                "Keep it up for # seconds longer",
                "Keep going for # more seconds",
                "Continue for # seconds longer",
                "There are # seconds remaining",
                "There are # more seconds to go"
                );

        String voiceMsg = "";

        if (seconds > 60)
        {
            int minutes = seconds / 60;
            seconds = seconds % minutes;

            voiceMsg = Float.toString(minutes)
                + " minutes and " + Integer.toString(seconds) + " seconds remaining.";
        }
        else
        {
            voiceMsg = msg.replace("#", (CharSequence)Integer.toString(seconds)) + ".";
        }


        return voiceMsg;
    }

    private void loadCurrent() {

        ExercisesActivity activity = (ExercisesActivity) getActivity();
        ExerciseContent.ExerciseItem exerciseItem = activity.getCurrentExercise();
        if (null != exerciseItem) {
            setStaticViews(exerciseItem, activity.getTotalExercises());
            Speech.speak("Begin.  Do " + exerciseItem.name, TextToSpeech.QUEUE_FLUSH);
            startTimer(10);
        }
    }

    private void showNextFragment() {
        if ( ((ExercisesActivity)getActivity()).isLastExercise() )
            loadFragment("FinishedFragment");
        else
            loadFragment("ReaderFragment");
    }

    private void startTimer(int seconds)
    {
        // start the exercise timer
        this.secondsRemaining = seconds;
        updateTimerDisplay(seconds);
        handler.postDelayed(runnable, second); // update in 1 second
    }

    private void stopTimer() {
        handler.removeCallbacks(runnable);
    }

    private void setStaticViews(ExerciseContent.ExerciseItem exerciseItem, int totalExercises)
    {
        //
        // set static field values
        //
        TextView exerciseCount = this.getView().findViewById(R.id.textview_exercise_count);
        if (null != exerciseCount)
            exerciseCount.setText(exerciseItem.order + " of " + totalExercises);

        TextView exerciseName = this.getView().findViewById(R.id.textview_exercise_name);
        if (null != exerciseName)
            exerciseName.setText(exerciseItem.name);
    }

    private void updateTimerDisplay(int seconds)
    {
        if (seconds >= 0) {
            View view = this.getView();
            if (null != view) {
                TextView countDown = view.findViewById(R.id.textview_countdown);
                if (null != countDown)
                    countDown.setText(Integer.toString(seconds));
            }
        }
    }

    public void loadFragment(String tag)
    {
        ExercisesActivity activity = (ExercisesActivity) getActivity();
        if (null != activity) {
            activity.loadFragment(tag);
        }
    }
}
