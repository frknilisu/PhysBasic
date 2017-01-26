package com.frkn.physbasic;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by frkn on 08.11.2016.
 */

public class Chapter {
    private int id;
    private int imageId;
    private String title, definition;
    private boolean lock;
    private int length;
    private String link;


    public Chapter(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getInt("id");
            this.imageId = Integer.parseInt(jsonObject.getString("imageId"));
            this.definition = jsonObject.getString("definition");
            this.title = jsonObject.getString("title");
            this.lock = jsonObject.getBoolean("lock");
            this.length = jsonObject.getInt("length");
            this.link = jsonObject.getString("link");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public int getImageId() {
        return imageId;
    }

    public String getTitle() {
        return title;
    }

    public String getDefinition() {
        return definition;
    }

    public boolean isLock() {
        return lock;
    }

    public int getLength() {
        return length;
    }

    public String getLink() { return link; }
}
