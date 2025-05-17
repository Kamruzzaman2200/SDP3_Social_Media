package com.example.simplesocialmediaapp.Models;

import java.util.ArrayList;

public class PostSearchModel {
    String postid;
    ArrayList<String> content;

    public PostSearchModel(){}

    public PostSearchModel(String postid, ArrayList<String> content)
    {
        this.postid = postid;
        this.content = content;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public ArrayList<String> getContent() {
        return content;
    }

    public void setContent(ArrayList<String> content) {
        this.content = content;
    }
}
