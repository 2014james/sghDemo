package com.max.demo;

public class MixManager {
	static {
		System.loadLibrary("ffmpeg");
		System.loadLibrary("transwav");
	}

	public native String getStringJni();

	/**
	 * 音频出来
	 * 转采样率，转声道
	 *
	 * @param infile 源文件
	 * @param outFile 输出文件
	 * @param sample 采样率
	 * @param channels	声道
	 * @param volume 255
	 * @return
	 */
	public native int TransMp3ToWav(String infile, String outFile, int sample,
			int channels, int volume);

	public native int MixMp4(String video, String audio, String outfile);

	public native int TransVideo(String acc, String ogg);

	public native int MixAudio(String audio, String aa, String mp4File);
	
	public native int getCurrSizeMp3();
	
	public native int getTotailLen(String infile);

}
