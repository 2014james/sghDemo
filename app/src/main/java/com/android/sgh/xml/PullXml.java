package com.android.sgh.xml;

import android.util.Log;
import android.util.Xml;

import com.android.sgh.bean.Smil;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/***
 *create by James
 *
 * XML解析
 * 参考xml存放在value
 *
 * 解析文档获取时间clipBegin和clipEnd的值
 *
 *
 */
public class PullXml {

    static String TAG = PullXml.class.getSimpleName();

    /**
     * xml解析
     *
     * @param path
     * @return
     */
    public static List<Smil> readXML(String path) {
        XmlPullParser parser = Xml.newPullParser();
        FileInputStream fileInputStream = null;
        try {
            File file = new File(path);
            fileInputStream = new FileInputStream(file);
            parser.setInput(fileInputStream, "UTF-8");
            int eventType = parser.getEventType();
            Smil smil = null;
            List<Smil> Smils = new ArrayList<>();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    //开始解释文档
                    case XmlPullParser.START_DOCUMENT:
                        Log.e(TAG, "readXML--> START_DOCUMENT");
                        Smils = new ArrayList<>();
                        break;

                    /**
                     *
                     * 解析开始标签，name以此是
                     * smil、body、par、text、audio
                     *
                     *
                     */
                    case XmlPullParser.START_TAG:
                        String name = parser.getName();
                        Log.e(TAG, "readXML--> " + name);
                        if (name.equals("par")) {
                            smil = new Smil();
                        } else if (smil != null) {
                            if (name.equals("text")) {
                                String src = parser.getAttributeValue(0);

                                int index = src.indexOf("#");
                                String value = src.substring(index + 1, src.length());
                                smil.textId = value;

                                //是audio标签获取标签内容
                            } else if (name.equals("audio")) {

                                //获取标签0位置clipBegin元素值
                                String clipBegin = parser.getAttributeValue(0);
                                //获取标签1位置clipEnd元素值

                                String clipEnd = parser.getAttributeValue(1);
                                smil.clipBegin = Integer.valueOf(clipBegin.substring(0, clipBegin.length() - 2));//截取ms
                                smil.clipEnd = Integer.valueOf(clipEnd.substring(0, clipEnd.length() - 2));

                                Log.e("PullXml", smil.textId + ";" + smil.clipBegin + ";" + smil.clipEnd);
                            }
                        }
                        break;
                    /**
                     *
                     * 结束标签 </par>，name是par
                     *
                     *
                     */
                    case XmlPullParser.END_TAG:
                        Log.e(TAG, "readXML--> END_TAG");
                        /**
                         * 结束标签时，解析完一数据放入集合
                         */
                        if (parser.getName().equals("par")
                                && smil != null) {
                            Smils.add(smil);
                            smil = null;
                        }
                        break;
                }
                eventType = parser.next();
            }
            fileInputStream.close();
            return Smils;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PullXml", "readXML-->Exception: " + e.getLocalizedMessage());
        }
        return null;
    }
}
