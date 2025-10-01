package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HistorialResponse {
    @SerializedName("items")
    private List<HistorialItem> items;

    public List<HistorialItem> getItems() { return items; }
}
