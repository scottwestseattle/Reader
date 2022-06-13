package com.e.rhino.exercises;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import androidx.annotation.Nullable;

import com.e.rhino.Speech;
import com.e.rhino.Tools;
import com.e.rhino.exercises.content.ExerciseContent;

import java.util.List;
import java.util.Locale;

public class ReaderService extends Service {

    ExerciseContent exercises = null;
    int currentExerciseIndex = -1;
    private int secondsRemaining = -1;
    private final int second = 1000; // 1 Second
    private long mStartTime;
    private String mTotalTime = "";
    private boolean started = false;
    private int questionCount = -1;
    private Handler handler = new Handler();
    boolean finished = false;
    private int currentQuestion = -1;
    private boolean timerPaused = false;
    private int pauseBetweenSentences = 2;
    private ExerciseContent.Question mAnswerPending = null;
    private SpeechService Speech = null;

    @Override

    // execution of service will start
    // on calling this method
    public int onStartCommand(Intent intent, int flags, int startId) {

        Speech = new SpeechService();
        Speech.init(getApplicationContext());

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
                    {
                        if (utteranceId.equals("question"))
                            startTimer(pauseBetweenSentences); // pause after question
                        else
                            startTimer(2); // pause between questions
                    }
                    else
                    {
                        stopSelf();
                    }
                }
            }

            @Override
            public void onError(String utteranceId) {
                // There was an error.
            }
        });

        this.exercises = ExercisesActivity.exercises;

        start();

        // returns the status
        // of the program
        return START_STICKY;
    }

    @Override

    // execution of the service will
    // stop on calling this method
    public void onDestroy() {
        super.onDestroy();

        Speech.shutup();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //
    // The reading functions
    //

    private void startTimer(int seconds)
    {
        this.secondsRemaining = seconds;
        //todo: updateTimerDisplay(seconds);
        handler.postDelayed(this.runnable, this.second); // update in 1 second
    }

    private Runnable runnable = new Runnable(){
        public void run() {

            secondsRemaining--;
            //todo: updateTimerDisplay(secondsRemaining);

            if (secondsRemaining >= 1) {
                handler.postDelayed(runnable, second); // update in 1 second
            }
            else {
                if (mAnswerPending != null) // even number means ask a new question{
                {
                    Speech.utter(mAnswerPending.answer, TextToSpeech.QUEUE_ADD, "answer", mAnswerPending.answerLanguage);
                    mAnswerPending = null; // q and a have been spoken, finished with it
                    //todo: setStaticName(); // show the answer
                }
                else {
                    loadNextQuestion();
                }
            }
        }
    };

    private Runnable startUp = new Runnable(){
        public void run() {
            if (isLoaded()) {
                start();
            } else {
                Log.i("startup", "waiting one second");
                handler.postDelayed(startUp, second); // update in 1 second
            }
        }
    };

    public boolean isLoaded() {
        return this.exercises.isLoaded();
    }

    private void start() {
        if (Speech.isLoaded()) {
            Speech.speak("Empezando", TextToSpeech.QUEUE_ADD);
            this.started = true;
            reset();
            mStartTime = System.currentTimeMillis();
            loadNext();
        }
        else
        {
            // set wait timer
        }
    }

    private void reset()
    {

    }

    private void loadNext() {

        ExerciseContent.ExerciseItem exerciseItem = getNextExercise();
        if (null != exerciseItem)
        {
            //resetQuestions(exerciseItem.questions);
            Speech.speak("Comenzando el capítulo: " + exerciseItem.name, TextToSpeech.QUEUE_ADD);

            this.questionCount = 0;
            loadNextQuestion();
            //todo: activity.setFabPlayIcon(false);
        }
        else {
            // end
            stopTimer();
            //todo: activity.setFabPlayIcon(true);
            //todo: activity.setRunTime(getElapsedTime());
            //todo: loadFragment("FinishedFragment");
            Speech.speak("No hay mas texto, terminando servicio.", TextToSpeech.QUEUE_ADD);
            this.finished = true;
        }
    }

    private void stopTimer() {
        handler.removeCallbacks(this.runnable);
    }

    public void loadNextQuestion()
    {
        ExerciseContent.ExerciseItem exerciseItem = getCurrentExercise();
        List<ExerciseContent.Question> questions = exerciseItem.questions;

        // read it
        boolean questionFound = false;
        if (null != questions) {
            if (read(questions)) {
                //todo: setStaticViews(exerciseItem);
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
        int randomIndex = ExerciseContent.getRandomIndex(questions);
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

                if (question.answer != null) {
                    // if there is an answer, then save for the end of the timer
                    this.mAnswerPending = question;
                }

                Speech.utter(question.question, TextToSpeech.QUEUE_ADD, "question", question.questionLanguage);
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

    public ExerciseContent.ExerciseItem getCurrentExercise() {
        ExerciseContent.ExerciseItem ex = null;

        if (this.currentExerciseIndex < this.exercises.exerciseList.size())
        {
            ex = this.exercises.exerciseList.get(this.currentExerciseIndex);
        }

        return ex;
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

    public class SpeechService {

        //
        // test to see if speech keeps running
        private TextToSpeech tts = null;
        private boolean mMuted = false;
        private boolean mIsLoaded = false;

        public boolean isMuted() {
            return mMuted;
        }

        public static final int languageEnglish = 0;
        public static final int languageSpanish = 1;
        public static final int languageDefault = languageSpanish;

        public boolean isLoaded() {
            return mIsLoaded;
        }

        public void setMuted(boolean muted) {
            mMuted = muted;
            if (mMuted)
                shutup();
        }

        public void init(Context context) {
            if (null != tts) {
                Log.i("Speech", "Already loaded.");
                //speak("Speech already loaded.", TextToSpeech.QUEUE_FLUSH);
                return;
            }

            tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {

                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int ttsLang = tts.setLanguage(Locale.US);
                        if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                                || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("TTS", "The Language is not supported!");
                        } else {
                            Log.i("TTS", "Language Supported.");
                        }

                        Locale locSpanish = new Locale("spa", "ESP");
                        tts.setLanguage(locSpanish);

                        Log.i("TTS", "Initialization success.");
                        mIsLoaded = true;
                        speak("Servicio Listo.", TextToSpeech.QUEUE_ADD);
                    } else {
                        Log.i("TTS", "TTS Initialization failed!");
                    }
                }
            });
        }

        public void setLanguage(int language) {
            String languageCode = "spa";
            String languageCountry = "SPA";

            switch (language) {
                case languageEnglish:
                    languageCode = "eng";
                    languageCountry = "ENG";
                    break;
                default:
                    break;
            }

            Locale loc = new Locale(languageCode, languageCountry);

            if (null != tts)
                tts.setLanguage(loc);
        }

        public void setCallback(UtteranceProgressListener progressListener) {
            tts.setOnUtteranceProgressListener(progressListener);
        }

        public void utter(CharSequence text, int queueMode, String id) {
            if (null != tts && !mMuted)
                tts.speak(text, queueMode, null, id);
        }

        public void utter(CharSequence text, int queueMode, String id, int language) {
            setLanguage(language);
            utter(text, queueMode, id);
            setLanguage(Speech.languageDefault);
        }

        public void speak(CharSequence text, int queueMode, int language) {
            setLanguage(language);
            speak(text, queueMode);
            setLanguage(Speech.languageDefault);
        }

        public void speak(CharSequence text, int queueMode) {
            if (null != tts && !mMuted)
                tts.speak(text, queueMode, null, "");
        }

        public void shutup() {
            if (null != tts && tts.isSpeaking())
                tts.stop();
        }

        public void shutdown() {
            if (null != tts) {
                tts.shutdown();
            }
        }
    }
}
