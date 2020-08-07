package com.android.sgh.bean;

/***
 *create by James
 *
 */
public class Smil {

    public String textId;//textid(w11)
    public int clipBegin;//text开始时间
    public int clipEnd;//text结束时间

    @Override
    public String toString() {
        return "Smil{" +
                "textId='" + textId + '\'' +
                ", clipBegin='" + clipBegin + '\'' +
                ", clipEnd='" + clipEnd + '\'' +
                '}';
    }
}
