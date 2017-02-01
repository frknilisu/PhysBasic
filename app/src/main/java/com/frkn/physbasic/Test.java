package com.frkn.physbasic;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by frkn on 21.01.2017.
 */

public class Test {

    private int id;
    private int imageId;
    private String title, definition;
    private int imageCount;
    private int fileLength;
    private String link;


    public Test(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getInt("id");
            this.imageId = Integer.parseInt(jsonObject.getString("imageId"));
            this.definition = jsonObject.getString("definition");
            this.title = jsonObject.getString("title");
            this.imageCount = jsonObject.getInt("imageCount");
            this.fileLength = jsonObject.getInt("fileLength");
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

    public int getImageCount() {
        return imageCount;
    }

    public int getFileLength() {
        return fileLength;
    }

    public String getLink() {
        return link;
    }
}
