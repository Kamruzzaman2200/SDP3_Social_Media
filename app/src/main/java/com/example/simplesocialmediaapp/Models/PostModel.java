package com.example.simplesocialmediaapp.Models;

import java.util.ArrayList;

public class PostModel {
    String uid,content,path,timestamp;
    ArrayList<CommentsModel> comments;
    ArrayList<String> likes;

    public PostModel(){}

    public PostModel(String uid,String content,String path,String timestamp,ArrayList<CommentsModel> comments,ArrayList<String> likes)
    {
        this.uid = uid;
        this.content = content;
        this.path = path;
        this.timestamp = timestamp;
        this.comments = comments;
        this.likes = likes;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<CommentsModel> getComments() {
        return comments;
    }

    public void setComments(ArrayList<CommentsModel> comments) {
        this.comments = comments;
    }

    public ArrayList<String> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }
}
