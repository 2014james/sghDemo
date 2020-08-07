package com.android.sgh.audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WaveHeader
{
    private static final String TAG = "WaveHeader";

    public final char fileID[] = { 'R', 'I', 'F', 'F' };
    
    public int fileLength;
    
    public char wavTag[] = { 'W', 'A', 'V', 'E' };;
    
    public char FmtHdrID[] = { 'f', 'm', 't', ' ' };
    
    public int FmtHdrLeth;
    
    public short FormatTag;
    
    public short Channels;
    
    public int SamplesPerSec;
    
    public int AvgBytesPerSec;
    
    public short BlockAlign;
    
    public short BitsPerSample;
    
    public char DataHdrID[] = { 'd', 'a', 't', 'a' };
    
    public int DataHdrLeth;
    
    public byte[] getHeader() throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        WriteChar(bos, fileID);//R I F F 4个字节
        WriteInt(bos, fileLength);//总长度（去除ID和Size所占的字节数后剩下的其他字节数目） 4个字节
        WriteChar(bos, wavTag);//W A V E 4个字节
        WriteChar(bos, FmtHdrID);//f m t 4个字节
        WriteInt(bos, FmtHdrLeth);//16 4个字节（数值为16或18，18则最后又附加信息）
        WriteShort(bos, FormatTag);//编码方式 2个字节 0x0001 （为1时表示线性PCM编码，大于1时表示有压缩的编码。）
        WriteShort(bos, Channels);//音频通道 2个字节  声道数目，1--单声道；2--双声道
        WriteInt(bos, SamplesPerSec);//采样率 4个字节
        WriteInt(bos, AvgBytesPerSec);//Byte率 4个字节 采样频率*音频通道数*每次采样得到的样本位数/8
        WriteShort(bos, BlockAlign);//数据块对齐单位(每个采样需要的字节数) 2个字节 通道数*每次采样得到的样本位数/8
        WriteShort(bos, BitsPerSample);//样本数据位数 2个字节 16
        WriteChar(bos, DataHdrID);//d a t a 4个字节
        WriteInt(bos, DataHdrLeth);//pcm长度 4个字节
        bos.flush();
        byte[] r = bos.toByteArray();
        //Log.e(TAG, "getHeader - " + r.length); 44
        bos.close();
        return r;
    }
    
    private void WriteShort(ByteArrayOutputStream bos, int s)
            throws IOException
    {
        byte[] mybyte = new byte[2];
        mybyte[1] = (byte) ((s << 16) >> 24);
        mybyte[0] = (byte) ((s << 24) >> 24);
        bos.write(mybyte);
    }
    
    private void WriteInt(ByteArrayOutputStream bos, int n) throws IOException
    {
        byte[] buf = new byte[4];
        buf[3] = (byte) (n >> 24);
        buf[2] = (byte) ((n << 8) >> 24);
        buf[1] = (byte) ((n << 16) >> 24);
        buf[0] = (byte) ((n << 24) >> 24);
        bos.write(buf);
    }
    
    private void WriteChar(ByteArrayOutputStream bos, char[] id)
    {
        for (int i = 0; i < id.length; i++)
        {
            char c = id[i];
            bos.write(c);
        }
    }
}