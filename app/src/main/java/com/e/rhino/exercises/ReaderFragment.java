package com.e.rhino.exercises;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.e.rhino.R;
import com.e.rhino.Speech;
import com.e.rhino.Tools;
import com.e.rhino.exercises.content.ExerciseContent;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ReaderFragment extends Fragment {

    static private boolean autoStart = false;
    static private boolean started = false;
    private boolean timerPaused = false;
    private boolean finished = false;
    private int secondsRemaining = 0;
    private int secondsRewind = 5;
    private int secondsFastForward = secondsRewind;
    private final int second = 1000; // 1 Second
    private final int countdownSeconds = 5;
    private final int nextCountdownSeconds = 3;
    private int pauseBetweenSentences = 5;
    private final int getReadySeconds = countdownSeconds + 1;
    private Handler handler = new Handler();
    private int currentQuestion = 0;
    private int questionCount = 0;

    private Runnable runnable = new Runnable(){
        public void run() {

            secondsRemaining--;
            updateTimerDisplay(secondsRemaining);

            if (secondsRemaining >= 1) {
                handler.postDelayed(runnable, second); // update in 1 second
            }
            else {
                loadNextQuestion();
            }
        }
    };

    private Runnable startUp = new Runnable(){
        public void run() {

            ExercisesActivity activity = (ExercisesActivity) getActivity();
            if (null != activity) {
                if (activity.isLoaded()) {
                    start();
                } else {
                    Log.i("startup", "waiting one second");
                    handler.postDelayed(startUp, second); // update in 1 second
                }
            }
        }
    };

    public ReaderFragment() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        stopTimer();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reader, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Speech.speak("Terminado", TextToSpeech.QUEUE_FLUSH);
                onHardStop();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        Speech.setCallback(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // Speaking started.
            }

            @Override
            public void onDone(String utteranceId) {
                // Speaking stopped.
                if (utteranceId.length() > 0)
                {
                    if (!finished && !timerPaused)
                        startTimer(pauseBetweenSentences); // pause between sentences
                }
            }

            @Override
            public void onError(String utteranceId) {
                // There was an error.
            }
        });

        if (this.started) {
            loadNext();
        }
        else if (this.autoStart) {
            // not used
            handler.postDelayed(this.startUp, this.second * 2);
        }
        else
        {
            start();
        }


    }

    public boolean onFabPlayPauseClicked() {
        if (started) {
            if (timerPaused) {
                Speech.speak("Continuado.  ", TextToSpeech.QUEUE_FLUSH);
                startTimer(secondsRemaining); // restart timer
            } else {
                Speech.speak("Pausado.  ", TextToSpeech.QUEUE_FLUSH);
                stopTimer();
            }

            timerPaused = !timerPaused;
        }
        else
        {
            start();
        }

        return timerPaused;
    }

    public boolean onFabNextClicked() {
        if (started) {
            //timerPaused = false;
            loadNextQuestion();
        }
        else {
            //start();
        }

        return timerPaused;
    }

    public boolean onFabPreviousClicked() {

        if (started) {
            if (timerPaused) {
                Speech.speak("Resuming...  ", TextToSpeech.QUEUE_FLUSH);
                startTimer(nextCountdownSeconds); // restart timer
                timerPaused = false;
            } else {
                int seconds = nextCountdownSeconds + 1;
                if (secondsRemaining > seconds)
                    secondsRemaining = seconds; // countdown from 3
                else
                    secondsRemaining = 1; // do it now
            }
        } else {
            start();
        }

        ((ExercisesActivity) getActivity()).setFabPlayIcon(timerPaused);

        return timerPaused;
    }

    public void onFabRewindClicked() {
        this.secondsRemaining += this.secondsRewind;
        if (timerPaused)
            updateTimerDisplay(secondsRemaining);
    }

    private int getRandomIndex(List<ExerciseContent.Question> items) {
        int ix = new Random().nextInt(items.size());
        if (items.get(ix).uses == 0) {
            return ix;
        }

        // find the next unused item
        for (int i = 0; i < items.size(); i++)
        {
            ix++;
            if (ix >= items.size())
                ix = 0;

            if (items.get(ix).uses == 0)
                return ix;
        }

        return -1;
    }

    private void start() {
        ExercisesActivity activity = (ExercisesActivity) getActivity();
        if (null != activity) {
            if (activity.isLoaded()) {
                Speech.speak("Empezando", TextToSpeech.QUEUE_ADD);
                this.started = true;
                activity.reset();
                loadNext();
            } else {
                Speech.speak("Wait for exercises to finish loading...", TextToSpeech.QUEUE_ADD);
                handler.postDelayed(this.startUp, this.second); // wait 1 second
            }
        }
    }

    public void onHardStop() {
        this.started = false;
        this.finished = true;
        stopTimer();
        loadFragment("StartFragment");
    }

    private void loadNext() {
        ExercisesActivity activity = (ExercisesActivity) getActivity();
        if (null == activity)
            return;

        ExerciseContent.ExerciseItem exerciseItem = activity.getNextExercise();

        if (null != exerciseItem)
        {
            //resetQuestions(exerciseItem.questions);
            Speech.speak("Comenzando el capítulo: " + exerciseItem.name, TextToSpeech.QUEUE_ADD);

            int seconds = activity.getTimerSeconds();
            String title = "";
            String text = "";

            this.questionCount = 0;
            loadNextQuestion();
            activity.setFabPlayIcon(false);
        }
        else {
            // end
            stopTimer();
            activity.setFabPlayIcon(true);
            loadFragment("FinishedFragment");
        }
    }

    private void resetQuestions(List<ExerciseContent.Question> questions)
    {
        if (null != questions) {
            Iterator<ExerciseContent.Question> iterator = questions.iterator();
            while (iterator.hasNext()) {
                ExerciseContent.Question question = iterator.next();
                question.uses = 0;
            }
        }
    }

    public void loadNextQuestion()
    {
        ExercisesActivity activity = (ExercisesActivity) getActivity();
        if (null == activity)
            return;

        ExerciseContent.ExerciseItem exerciseItem = activity.getCurrentExercise();
        List<ExerciseContent.Question> questions = exerciseItem.questions;

        // read it
        boolean questionFound = false;
        if (null != questions) {
            if (read(questions)) {
                setStaticViews(activity, exerciseItem, exerciseItem.name);
                questionFound = true;
            }
        }

        if (!questionFound)
        {
            this.finished = true;
            //Speech.speak("Fin del capítulo.", TextToSpeech.QUEUE_ADD);
            loadNext();
        }
        else
        {
            this.finished = false;
        }
    }

    private boolean read(List<ExerciseContent.Question> questions) {
        boolean rc = false;
        int randomIndex = getRandomIndex(questions);
        if (randomIndex >= 0) {
            currentQuestion = randomIndex;
            questionCount++;
            ExerciseContent.Question question = questions.get(randomIndex);
            question.uses++;
            if (!this.timerPaused) {

                // calculate how time will be needed to answer
                int seconds = 3;
                String[] words = question.question.split(" ");
                if (words.length >= 8)
                {
                    seconds = words.length / 2; // use 2 words per second
                    seconds = Tools.keepInRange(seconds, 5, 7);
                }

                this.pauseBetweenSentences = seconds;
                Speech.utter(question.question, TextToSpeech.QUEUE_ADD, "reading");
            }
            rc = true;
        }
        else
        {
            // finished?
            rc = false;
        }
        return rc;
    }

    private void startTimer(int seconds)
    {
        this.secondsRemaining = seconds;
        updateTimerDisplay(seconds);
        handler.postDelayed(this.runnable, this.second); // update in 1 second
    }

    private void stopTimer() {
        handler.removeCallbacks(this.runnable);
    }

    private void updateTimerDisplay(int seconds)
    {
        TextView countDown = this.getView().findViewById(R.id.textview_countdown);
        if (null == countDown)
            return;

        if (seconds > 0) {
            countDown.setText(Integer.toString(seconds));
        }
        else
        {
            countDown.setText("");
        }
    }

    public void updateRunSeconds(int seconds) {
        TextView tv = this.getView().findViewById(R.id.textview_exercise_seconds);
        if (null != tv)
            tv.setText(Integer.toString(seconds) + " seconds");
    }

    private void setStaticViews(ExercisesActivity activity, ExerciseContent.ExerciseItem exerciseItem, String title)
    {
        //
        // set static values
        //
        TextView tv = this.getView().findViewById(R.id.textview_title);
        if (null != tv)
            tv.setText(exerciseItem.name );

        tv = this.getView().findViewById(R.id.textview_coming_up);
        if (null != tv)
            tv.setText(this.questionCount + " of " + exerciseItem.questions.size() + " (#" + (this.currentQuestion + 1) + ")");

        tv = this.getView().findViewById(R.id.textview_exercise_name);
        if (null != tv)
            tv.setText(exerciseItem.questions.get(this.currentQuestion).question);
    }

    private void updateTimerAudio(int seconds) {
        if (seconds == this.getReadySeconds) {
            Speech.speak("Starting in: ", TextToSpeech.QUEUE_FLUSH);
        }
        else if (seconds <= this.countdownSeconds && seconds > 0) {
            Speech.speak(Integer.toString(seconds), TextToSpeech.QUEUE_FLUSH);
        }
    }

    private void loadFragment(String tag)
    {
        ExercisesActivity activity = (ExercisesActivity) getActivity();
        if (null != activity) {
            activity.loadFragment(tag);
        }
    }
}
