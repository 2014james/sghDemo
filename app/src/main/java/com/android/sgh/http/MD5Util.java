package com.android.sgh.http;

import android.util.Log;

import java.security.MessageDigest;


/***
 * MD5加密方法(服务器)
 * 
 * @author Administrator
 *
 */
public class MD5Util {

	/***
	 * MD5加密 生成32位小写md5码,已跟服务器端api对接好,不要随意改动方法
	 */
	public static String string2MD5(String inStr) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			Log.e("error","md5生成异常  " + e.toString());
			e.printStackTrace();
			return null;
		}
		char[] charArray = inStr.toCharArray();
		byte[] byteArray = new byte[charArray.length];
		// 遍历传入String,char数组转byte数组
		for (int i = 0; i < charArray.length; i++)
			byteArray[i] = (byte) charArray[i];
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}

}
