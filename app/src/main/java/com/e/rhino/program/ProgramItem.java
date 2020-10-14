package com.e.rhino.program;

import com.e.rhino.sessions.content.SessionContent;

import java.util.ArrayList;
import java.util.List;

public class ProgramItem {
    public final int id;
    public final String name;
    public final String description;
    public final int imageId;
    public final int sessionCount;
    public List<SessionContent.SessionItem> sessionItems = new ArrayList<SessionContent.SessionItem>();

    public ProgramItem(int id, String name, String description, int imageId, int sessionCount, List<SessionContent.SessionItem> sessions) {
        this.id = id;
        this.name = name;
        this.imageId = imageId;
        this.description = description;
        this.sessionCount = sessionCount;
        this.sessionItems = sessions;
    }

    @Override
    public String toString() {
        return name;
    }
}
