package com.tagbox.taglink;

/**
 * Created by Suhas on 12/1/2016.
 */

public class PostMessageData {
    public String postMessage;

    public PostMessageData() {
    }
    public PostMessageData(String message) {
        this.postMessage = message;
    }

    /*********** Get Methods ******************/

    public String getPostMessage()
    {
        return this.postMessage;
    }

    /*********** Set Methods ******************/
    public void setPostMessage(String message)
    {
        this.postMessage = message;
    }

}
