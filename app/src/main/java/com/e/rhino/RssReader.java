package com.e.rhino;

import android.util.Log;

import com.e.rhino.program.ProgramItem;
import com.e.rhino.exercises.content.ExerciseContent;
import com.e.rhino.history.content.HistoryContent;
import com.e.rhino.sessions.content.SessionContent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RssReader {

    static private XmlPullParserFactory xmlFactoryObject;
    static private volatile boolean parsingComplete = false;
    static public String urlString = "";

    // Program
    static private String programName = "";
    static private int programId = -1;
    static private String programDescription = "";
    static private int sessionCount = 0;
    static List<ProgramItem> programItems = null;
    static public final Map<Integer, ProgramItem> programMap = new HashMap<Integer, ProgramItem>();

    // Session
    static private int sessionCourseId = -1;
    static private int sessionId = -1;
    static private int sessionNumber = -1;
    static private String sessionName = "";
    static private String sessionDescription = "";
    static private int sessionExerciseCount = -1;
    static private int sessionSeconds = -1;
    static private String sessionParentName = "";
    static private int sessionType = -1;
    static public final Map<Integer, SessionContent.SessionItem> sessionMap = new HashMap<Integer, SessionContent.SessionItem>();

    // Exercise
    static private String exerciseName = "";
    static private int exerciseRunSeconds = -1;
    static private int exerciseBreakSeconds = -1;
    static private String exerciseDescription = "";
    static private String exerciseImageName = "";
    static List<ExerciseContent.ExerciseItem> exerciseItems = null;
    static List<ExerciseContent.Question> questions = null;

    // History
    static private String historyDatetime = "";
    static private String historyProgramName = "";
    static private int historyProgramId = -1;
    static private String historySessionName = "";
    static private int historySessionId = -1;
    static private String historyTime = "";
    static private int historySeconds = -1;
    static List<HistoryContent.HistoryItem> historyItems = null;

    static public List<SessionContent.SessionItem> getSessionList(int courseId) {
        List<SessionContent.SessionItem> sessionItems = null;

        ProgramItem programItem = programMap.get(courseId);
        if (null != programItem)
            sessionItems = programItem.sessionItems;

        return sessionItems;
    }

    static public SessionContent.SessionItem getNextSession(int courseId, int sessionId) {
        SessionContent.SessionItem session = null;
        List<SessionContent.SessionItem> sessionItems = getSessionList(courseId);
        if (null != sessionItems) {
            Iterator<SessionContent.SessionItem> iterator = sessionItems.iterator();
            boolean next = false;
            while (iterator.hasNext()) {
                SessionContent.SessionItem item = iterator.next();
                if (item.id == sessionId) {
                    next = true;
                }
                else if (next) {
                    session = item;
                    break;
                }
            }
        }
        return session;
    }

    private RssReader()
    {
        // only allow static use
    }

    static public void fetchProgramList(String url, List<ProgramItem> items) {
        items.clear();
        programItems = items;
        sessionCourseId = -1; // flag that we're not loading sessions

        fetchXML(url);
    }

    static public void fetchExerciseList(String url, List<ExerciseContent.ExerciseItem> items) {
        if (null != questions)
            questions.clear();
        items.clear();
        exerciseItems = items;

        fetchXML(url);
    }

    static public void fetchHistoryList(String url, List<HistoryContent.HistoryItem> items) {
        items.clear();
        historyItems = items;

        fetchXML(url);
    }

    static public void fetchXML(String url) {

        parsingComplete = false;
        urlString = url;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);

                    // Starts the query
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myparser = xmlFactoryObject.newPullParser();

                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(stream, null);

                    parse(myparser);

                    stream.close();
                } catch (Exception e) {
                    Log.i("RssReader:fetchXML", "exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        try {
            thread.join(); // wait for the thread to finish
        } catch (InterruptedException e) {
            Log.e("RssReader:thread.join()", e.getMessage());
            e.printStackTrace();
        }
    }

    static public void parse(XmlPullParser myParser) {
        int event;
        String text = null;
        int order = 1;

        // testing data
        boolean testing = false;
        int testingRunSeconds = 3;
        int testingBreakSeconds = 3;
        int testingExerciseCount = 1;

        try {
            event = myParser.getEventType();

            List<SessionContent.SessionItem> sessionItems = new ArrayList<SessionContent.SessionItem>();

            int languageId = Speech.languageDefault;
            while (event != XmlPullParser.END_DOCUMENT) {

                String name = myParser.getName();
                //Log.i("parse", "name tag: " + name);

                switch (event) {

                    case XmlPullParser.START_TAG:
                        //
                        // get the language attribute from here because attributes aren't available on end tag
                        //
                        String ns = myParser.getNamespace();
                        String language = myParser.getAttributeValue(ns, "language");
                        if (null != language && language.length() > 0) {
                            try {
                                languageId = Integer.parseInt(language);
                            } catch (NumberFormatException nfe) {}
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;

                    case XmlPullParser.END_TAG:

                        //
                        // the 'course' block
                        //
                        if(name.equals("course")){
                            if (sessionCourseId <= 0) { // make sure we're not loading sessions
                                ProgramItem item = new ProgramItem(
                                        programId,
                                        programName,
                                        programDescription,
                                        programItems.size(), // used for image id
                                        sessionCount,
                                        sessionItems
                                );

                                programItems.add(item);
                                programMap.put(programId, item);

                                sessionItems = new ArrayList<SessionContent.SessionItem>();
                                sessionCount = 0; // re-start the count
                            }
                        }
                        else if (name.equals("course_name")){
                            programName = text.trim();
                        }
                        else if(name.equals("course_id")){
                            try {
                                programId = Integer.parseInt(text);
                            } catch(NumberFormatException nfe){}
                        }
                        else if(name.equals("course_description")){
                            programDescription = text.trim();
                        }
                        //
                        // the 'lesson' block
                        //
                        else if(name.equals("lesson")){

                            sessionCount++; // count the session from the program list

                            if (null != sessionItems) {
                                SessionContent.SessionItem item = new SessionContent.SessionItem(
                                        sessionId,
                                        sessionName,
                                        sessionDescription,
                                        sessionNumber,
                                        sessionParentName,
                                        sessionSeconds,
                                        sessionExerciseCount,
                                        sessionType
                                );

                                sessionItems.add(item);
                                sessionMap.put(sessionId, item);
                            }
                        }
                        else if(name.equals("lesson_id")){
                            try {
                                sessionId = Integer.parseInt(text);
                            } catch(NumberFormatException nfe){}
                        }
                        else if (name.equals("lesson_name")){
                            sessionName = text.trim();
                        }
                        else if(name.equals("lesson_description")){
                            sessionDescription = text.trim();
                        }
                        else if(name.equals("lesson_parent")){
                            sessionParentName = text.trim();
                        }
                        else if(name.equals("lesson_number")){
                            try {
                                sessionNumber = Integer.parseInt(text);
                            } catch(NumberFormatException nfe){}
                        }
                        else if(name.equals("lesson_exercise_count")){
                            try {
                                sessionExerciseCount = Integer.parseInt(text);
                            } catch(NumberFormatException nfe){}
                        }
                        else if(name.equals("lesson_seconds")){
                            try {
                                sessionSeconds = Integer.parseInt(text);
                            } catch(NumberFormatException nfe){}
                        }
                        else if(name.equals("lesson_type")){
                            try {
                                sessionType = Integer.parseInt(text);
                            } catch(NumberFormatException nfe){}
                        }                        //
                        // the 'history' block
                        //
                        else if(name.equals("history")){

                            sessionCount++; // count the session from the program list

                            if (null != historyItems) {
                                HistoryContent.HistoryItem item = new HistoryContent.HistoryItem(
                                        historyProgramName,
                                        historyProgramId,
                                        historySessionName,
                                        historySessionId,
                                        historyDatetime,
                                        historySeconds
                                );
                                historyItems.add(item);
                            }
                        }
                        else if (name.equals("history_datetime")){
                            historyDatetime = text.trim();
                        }
                        else if (name.equals("history_programName")){
                            historyProgramName = text.trim();
                        }
                        else if(name.equals("history_programId")){
                            try {
                                historyProgramId = Integer.parseInt(text);
                            } catch(NumberFormatException nfe){}
                        }
                        else if (name.equals("history_sessionName")){
                            historySessionName = text.trim();
                        }
                        else if(name.equals("history_sessionId")){
                            try {
                                historySessionId = Integer.parseInt(text);
                            } catch(NumberFormatException nfe){}
                        }
                        else if(name.equals("history_seconds")){
                            try {
                                historySeconds = Integer.parseInt(text);
                            } catch(NumberFormatException nfe){}
                        }
                        else if(name.equals("history_time")){
                            historyTime = text.trim();
                        }
                        //
                        // the 'exercise' block
                        //
                        if(name.equals("record")){
                            //Log.i("parse", "record end tag, save the record");
                            ExerciseContent.ExerciseItem ei = new ExerciseContent.ExerciseItem(
                                    exerciseName,
                                    exerciseDescription,
                                    questions
                            );
                            exerciseItems.add(ei);
                            questions = new ArrayList<ExerciseContent.Question>();
                        }
                        else if (name.equals("name")){
                            exerciseName = text.trim();
                            Log.i("parse", "name value: " + text);
                        }
                        else if (name.equals("question")){
                            if (null == questions)
                                questions = new ArrayList<ExerciseContent.Question>();

                            questions.add(new ExerciseContent.Question(text.trim()));
                            int size = (null == questions) ? 0 : questions.size();
                            if (size > 0) {
                                questions.get(size - 1).questionLanguage = languageId;
                            }

                        }
                        else if (name.equals("answer")){
                            text = text.trim();
                            if (text.length() > 0) {
                                int size = (null == questions) ? 0 : questions.size();
                                if (size > 0) {
                                    questions.get(size - 1).answer = text.trim();
                                    questions.get(size - 1).answerLanguage = languageId;
                                }
                            }
                        }
                        //
                        // skip all others
                        //
                        else{
                            // skip
                        }

                        break;
                }

                event = myParser.next();
            }

            parsingComplete = true;
        }

        catch (Exception e) {
            Log.i("xml:parse", "exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static public boolean isLoaded()
    {
        return parsingComplete;
    }

    static public void ping(String url) {

        urlString = url;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);

                    // Starts the query
                    conn.connect();
                    InputStream stream = conn.getInputStream();
                    stream.close();
                } catch (Exception e) {
                    Log.i("RssReader:ping", "exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        try {
            thread.join(); // wait for the thread to finish
        } catch (InterruptedException e) {
            Log.e("RssReader:thread.join()", e.getMessage());
            e.printStackTrace();
        }
    }
}