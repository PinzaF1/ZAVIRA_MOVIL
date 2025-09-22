package com.example.zavira_movil;

import com.google.gson.annotations.SerializedName;

public class BasicResponse {
    @SerializedName("success") private Boolean success;
    @SerializedName("message") private String message;

    public Boolean isSuccess() { return success != null && success; }
    public String getMessage() { return message; }
}
