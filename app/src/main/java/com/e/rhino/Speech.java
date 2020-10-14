package com.e.rhino;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

public class Speech  {

    private static TextToSpeech tts = null;
    private static boolean mMuted = false;

    public static boolean isMuted() {
        return mMuted;
    }

    public static void setMuted(boolean muted) {
        mMuted = muted;
        if (mMuted)
            shutup();
    }

    public static void init(Context context)
    {
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
                    speak("Listo.", TextToSpeech.QUEUE_ADD);
                } else {
                    Log.i("TTS", "TTS Initialization failed!");
                }
            }
        });
    }

    public static void setCallback(UtteranceProgressListener progressListener)
    {
        tts.setOnUtteranceProgressListener(progressListener);
    }

    public static void utter(CharSequence text, int queueMode, String id)
    {
        if (null != tts && !mMuted)
            tts.speak(text, queueMode, null, id);
    }

    public static void speak(CharSequence text, int queueMode)
    {
        if (null != tts && !mMuted)
            tts.speak(text, queueMode, null, "");
    }

    public static void shutup() {
        if (null != tts && tts.isSpeaking())
            tts.stop();
    }

    public static void shutdown() {
        if (null != tts) {
            tts.shutdown();
        }
    }

}
