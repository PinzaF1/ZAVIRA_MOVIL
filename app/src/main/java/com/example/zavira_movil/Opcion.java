package com.example.zavira_movil;

import com.google.gson.annotations.SerializedName;

public class Opcion {
    @SerializedName("key")  private String key;   // "A"|"B"|"C"|"D"
    @SerializedName("text") private String text;  // texto visible

    public String getKey()  { return key; }
    public String getText() { return text; }
}
