package com.richard.abigayle.hotelfinder.POJO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LENOVO on 4/26/2018.
 */

public class Distance {
    @SerializedName("text")
    @Expose
    private String text;

    @SerializedName("value")
    @Expose
    private String value;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
