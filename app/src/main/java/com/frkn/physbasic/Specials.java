package com.frkn.physbasic;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by frkn on 21.01.2017.
 */

public class Specials {

    private int id;
    private int imageId;
    private String title, definition;

    public Specials(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getInt("id");
            this.imageId = Integer.parseInt(jsonObject.getString("imageId"));
            this.definition = jsonObject.getString("definition");
            this.title = jsonObject.getString("title");
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

}
