package com.example.simplesocialmediaapp.Models;

public class PostReferenceModel {
    String postpath,timestamp;

    public PostReferenceModel(){}

    public PostReferenceModel(String postpath,String timestamp)
    {
        this.postpath = postpath;
        this.timestamp = timestamp;
    }

    public String getPostpath() {
        return postpath;
    }

    public void setPostpath(String postpath) {
        this.postpath = postpath;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
