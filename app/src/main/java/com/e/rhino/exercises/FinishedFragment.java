package com.e.rhino.exercises;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.e.rhino.R;
import com.e.rhino.Speech;

import java.util.Random;

public class FinishedFragment extends Fragment {

    private static String endMsgs[] = {
            "You killed it like a boss!",
            "You made it your bitch!",
            "You did it like a boss!",
            "Boom chocka locka!",
            "Whazzup?",
            "Who's your daddy!",
            "You've got that Boom Boom Pow!"
    };

    public FinishedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_finished, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ExercisesActivity activity = (ExercisesActivity) getActivity();
        if (null == activity)
            return;

        activity.setFabPlayIcon(true);

        String title = "All exercises completed!";
        Speech.speak(title, TextToSpeech.QUEUE_ADD);
        TextView tvTitle = this.getView().findViewById(R.id.title);
        if (null != tvTitle)
            tvTitle.setText(title);

        String msg = endMsgs[new Random().nextInt(endMsgs.length)];
        Speech.speak(msg, TextToSpeech.QUEUE_ADD);
        TextView tvMsg = this.getView().findViewById(R.id.msg);
        if (null != tvMsg)
            tvMsg.setText(msg);
    }

}
