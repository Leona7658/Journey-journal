package com.example.firebaseauth.DatabaseModel;

import java.util.List;

public class TagDB {
    private String email;

    private List<String> tagList;

    public TagDB(String email, List<String> tagList) {
        this.email = email;
        this.tagList = tagList;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getTagList() {
        return tagList;
    }



}
