package com.example.zavira_movil;

import com.google.gson.annotations.SerializedName;

public class QuizResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
