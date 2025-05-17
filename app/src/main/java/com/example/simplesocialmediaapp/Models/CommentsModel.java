package com.example.simplesocialmediaapp.Models;

import java.util.ArrayList;

public class CommentsModel {

    String commentuid,comment,timestamp;
    ArrayList<String> likes;

    public CommentsModel(){}

    public CommentsModel(String commentuid,String comment,String timestamp, ArrayList<String> likes)
    {
        this.commentuid = commentuid;
        this.comment = comment;
        this.timestamp = timestamp;
        this.likes = likes;
    }

    public String getCommentuid() {
        return commentuid;
    }

    public void setCommentuid(String commentuid) {
        this.commentuid = commentuid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<String> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }
}
