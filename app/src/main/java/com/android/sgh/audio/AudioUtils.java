package com.android.sgh.audio;

import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.util.Log;


import com.android.sgh.util.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;


public class AudioUtils {

    private static final String TAG = "AudioUtils";


    public static final String END = "FE20FE20";

//	private int size;


    public static boolean hebing(List<String> paths, String savaPath) {
        try {
            for (int i = 0; i < paths.size(); i++) {
                FileOutputStream fos = new FileOutputStream(savaPath + "bbbb.wav");
                FileInputStream fis = new FileInputStream(paths.get(i));
                byte[] temp = new byte[fis.available()];
                int len = temp.length;
                if (i == 0) {
                    while (fis.read(temp) > 0) {
                        fos.write(temp, 0, len);
                    }
                } else {
                    while (fis.read(temp) > 0) {
                        fos.write(temp, 44, len - 44);
                    }
                }
                fos.flush();
                fis.close();
            }

        } catch (IOException e) {
            return false;
        }

        return true;

    }

    /**
     * @param partsPaths     要合成的音频路径数组
     * @param unitedFilePath 输入合并结果数组
     */
    public static boolean uniteWavFile(List<String> partsPaths, String unitedFilePath) {


        for (int i = 0; i < partsPaths.size(); i++) {
            File f = new File(partsPaths.get(i));
            try {
                InputStream in = new FileInputStream(f);
                byte bytes[] = new byte[44];
                in.read(bytes);

                for (int j = 0; j < bytes.length; j++) {
                    System.out.println("--------->" + bytes[i]);
                }

            } catch (FileNotFoundException e) {
                return false;
            } catch (IOException e) {
                return false;
            }

        }

        return true;
    }

    private static byte[] getByte(String path) {
        File f = new File(path);
        InputStream in;
        byte bytes[] = null;
        try {
            in = new FileInputStream(f);
            bytes = new byte[(int) f.length()];
            in.read(bytes);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bytes;
    }


    /**
     * merge *.wav files
     *
     * @param target output file
     * @param paths  the files that need to merge
     * @return whether merge files success
     */
//    public static boolean mergeAudioFiles(String target, List<String> paths) {
//
//        return mergeAudioFiles(target, paths, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
//    }

    /**
     * merge *.wav files
     *
     * @param target output file
     * @param paths  the files that need to merge
     * @return whether merge files success
     */
    public static boolean mergeAudioFiles(Handler handler, String target, List<String> paths, int sample, int channel, int encoding) {
        try {
            FileOutputStream fos = new FileOutputStream(target);
            int size = 0;
            byte[] buf = new byte[1024 * 1000];
            int PCMSize = 0;
            for (int i = 0; i < paths.size(); i++) {
                FileInputStream fis = new FileInputStream(paths.get(i));
                size = fis.read(buf);
                while (size != -1) {
                    PCMSize += size;
                    size = fis.read(buf);
                }
                fis.close();
            }
            Log.e(TAG, "== PCMSize == " + PCMSize);
            PCMSize = PCMSize - paths.size() * 44;
            WaveHeader header = new WaveHeader();
            header.fileLength = PCMSize + (44 - 8);
            header.FmtHdrLeth = 16;
            header.BitsPerSample = 16;//位数 AUDIOENCODING
            header.Channels = 1;//音频通道 1，2 channel==AudioFormat.CHANNEL_IN_MONO?1:2;//
            header.FormatTag = 0x0001;
            header.SamplesPerSec = sample;//16000;//采样率 16000
            header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);
            header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
            header.DataHdrLeth = PCMSize;
            byte[] h = header.getHeader();
            assert h.length == 44;
            fos.write(h, 0, h.length);
            for (int j = 0; j < paths.size(); j++) {
                FileInputStream fis = new FileInputStream(paths.get(j));
                size = fis.read(buf);
                boolean isFirst = true;
                while (size != -1) {
                    if (isFirst) {
                        fos.write(buf, 44, size - 44);
                        size = fis.read(buf);
                        isFirst = false;
                    } else {
                        fos.write(buf, 0, size);
                        size = fis.read(buf);
                    }
                }
                fis.close();
            }
            fos.close();
            handler.sendEmptyMessage(2);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "mergeAudioFiles: " + e.getLocalizedMessage());
            handler.sendEmptyMessage(3);
            return false;
        }
        return true;
    }


    /**
     * 文件的部分剪辑（单端删除类型）
     *
     * @param target    目标输出文件
     * @param dest      源文件（wav）
     * @param endPos    剪贴结束点
     * @param pcmTarget 源pcm文件
     * @return
     * @ param startPos  开始开始剪贴点
     */
    public static boolean cutAudioFiles(String target, String dest, int cutPos, int endPos, String pcmTarget, String pamOut) {
        try {

            File file = new File(pcmTarget);
            long fileSize = file.length();
            FileOutputStream fos = new FileOutputStream(target);
            FileOutputStream fos1 = new FileOutputStream(pamOut);

            int size = 0;
            int PCMSize = 0;
            PCMSize = (int) (fileSize - (endPos - cutPos));

            WaveHeader header = new WaveHeader();
            header.fileLength = PCMSize + (44 - 8);
            header.FmtHdrLeth = 16;
            header.BitsPerSample = 16;
            header.Channels = 1;
            header.FormatTag = 0x0001;
            header.SamplesPerSec = 16000;
            header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);
            header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
            header.DataHdrLeth = PCMSize;
            byte[] h = header.getHeader();
            assert h.length == 44;


            fos.write(h, 0, h.length);
            FileInputStream fis = new FileInputStream(dest);
            FileInputStream fis1 = new FileInputStream(file);


            byte[] buf = new byte[cutPos];
            byte[] buf2 = new byte[cutPos];

            //读前半部分
            fis.read(buf);
            fis1.read(buf2);


            //写前半部分
            fos1.write(buf2);
            if (buf.length > 44) {
                fos.write(buf, 44, buf.length - 44);
            }
            //计算第二部分的长度
            byte[] buf1 = new byte[(int) (fileSize - endPos)];
            byte[] buf3 = new byte[(int) (fileSize - endPos)];


            //如果不是末尾点，则进行跳读
            if (buf1.length != 0) {
                //wav 文件格式的读取
                fis.skip(endPos);//流字节跳转到末尾点
                fis.read(buf1);//末尾点到文件的总长度
                fos.write(buf1, 0, buf1.length);


                //pcm 文件的格式的读取
                fis1.skip(endPos);
                fis1.read(buf3);
                fos1.write(buf3);
            }
            fis.close();
            fos.close();
            fos1.close();
            buf1 = null;
            buf2 = null;
            buf3 = null;
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 合成音频
     *
     * @param srcPath
     * @param insertPath
     * @param outPath
     * @param src_insert_time
     * @return
     */
    public static boolean mergeAudioFiles2(String srcPath, String insertPath, String outPath, int src_insert_time) {
        try {
            File file;
            RandomAccessFile src_file, dest_file, insert_file;
            byte[] buffer;
            long data_length, insert_index, src_file_length, insert_file_length;
            long tmp;
            int sample = 0;
            WaveHeader header = new WaveHeader();

            buffer = new byte[1024 * 1024];
            file = new File(outPath);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            src_file = new RandomAccessFile(srcPath, "r");
            dest_file = new RandomAccessFile(file, "rws");
            insert_file = new RandomAccessFile(insertPath, "r");

            src_file.read(buffer, 0, 44);
            dest_file.write(buffer, 0, 44);

            for (int i = 0; i < 4; i++) {
                int num = buffer[27 - i] & 0xff;
                sample = num + (sample << 8);
            }
            header.fileLength = 0;
            header.FmtHdrLeth = 16;
            header.BitsPerSample = 16;//位数 AUDIOENCODING
            header.Channels = buffer[22];//音频通道 1，2 channel==AudioFormat.CHANNEL_IN_MONO?1:2;//
            header.FormatTag = 0x0001;
            header.SamplesPerSec = sample;//16000;//采样率 16000
            header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);
            header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
            header.DataHdrLeth = 0;


            tmp = src_insert_time;
            insert_index = (tmp * header.AvgBytesPerSec / 1000);
            insert_index = (insert_index % 4) != 0 ? insert_index - (insert_index % 4) : insert_index;

            src_file_length = 0;
            for (int i = 0; i < 4; i++) {
                int num = buffer[43 - i] & 0xff;
                src_file_length = num + (src_file_length << 8);
            }

            insert_file.read(buffer, 0, 44);
            insert_file_length = 0;
            for (int i = 0; i < 4; i++) {
                int num = buffer[43 - i] & 0xff;
                insert_file_length = num + (insert_file_length << 8);
            }

            data_length = src_file_length + insert_file_length;

            Log.i(TAG, insert_index + "--" + src_insert_time + "--" + src_file_length + "--" + insert_file_length + "--" + data_length);

            if (getFileMaxSize() < (data_length + 44)) {
                dest_file.close();
                src_file.close();
                insert_file.close();
                file.delete();
                return false;
            }

            for (Long i = insert_index; i > 0; i -= buffer.length) {
                int num = (int) Math.min(i, (long) buffer.length);
                src_file.read(buffer, 0, num);
                dest_file.write(buffer, 0, num);
            }

            for (Long i = insert_file_length; i > 0; i -= buffer.length) {
                int num = (int) Math.min(i, (long) buffer.length);
                insert_file.read(buffer, 0, num);
                dest_file.write(buffer, 0, num);
            }


            for (Long i = src_file_length - insert_index; i > 0; i -= buffer.length) {
                int num = (int) Math.min(i, (long) buffer.length);
                src_file.read(buffer, 0, num);
                dest_file.write(buffer, 0, num);
            }


            dest_file.seek(0);
            header.DataHdrLeth = (int) data_length;
            header.fileLength = header.DataHdrLeth + 36;
            byte[] h = header.getHeader();

            dest_file.write(h, 0, 44);
            dest_file.close();
            src_file.close();
            insert_file.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private static long getFileMaxSize() {
        File datapath = Environment.getDataDirectory();
        StatFs dataFs = new StatFs(datapath.getPath());
        long sizes = (long) dataFs.getFreeBlocks() * (long) dataFs.getBlockSize() - 700 * 1024 * 1024;

        return sizes;

    }

    /**
     * 音频的多段操作（pcm格式音频）
     *
     * @param dstPath 源文件
     * @param outPath 输出文件
     * @ param cutArea  裁剪编辑的区域
     */
    public static void getPcmEdits(String dstPath, String outPath, List<long[]> cutAreas) {

        File dstFile = new File(dstPath);
        File outFile = new File(outPath);
        int dstFileLength = (int) dstFile.length();
        if (dstFile.exists() && cutAreas != null) {
            try {
                FileInputStream in = new FileInputStream(dstFile);
                FileOutputStream out = new FileOutputStream(outFile);
//	    		for(int i=0;i<cutAreas.size();i++){
//	    			int[] cutArea=cutAreas.get(i);
//	    			totalSize=totalSize+(cutArea[1]-cutArea[0]);
//	    		}
//	    		WaveHeader header = new WaveHeader();
//		        header.fileLength = totalSize + (44 - 8);
//		        header.FmtHdrLeth = 16;
//		        header.BitsPerSample = 16;
//		        header.Channels = 1;
//		        header.FormatTag = 0x0001;
//		        header.SamplesPerSec = 16000;
//		        header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);
//		        header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
//		        header.DataHdrLeth = totalSize;
//		        byte[] h = header.getHeader();
//		        assert h.length == 44;
//		        out.write(h, 0, h.length);
                int index = 0;
                while (index < cutAreas.size()) {
                    if (index == 0) {
                        if (cutAreas.size() > 1) {
                            if (cutAreas.get(index)[0] != 0) {
                                byte[] buf = new byte[(int) cutAreas.get(index)[0]];
                                in.read(buf);
                                out.write(buf);
                            }

                        } else {
                            if (cutAreas.get(index)[0] != 0) {
                                byte[] buf = new byte[(int) cutAreas.get(index)[0]];
                                in.read(buf);
                                out.write(buf);
                            }

                            if ((dstFileLength - (int) cutAreas.get(index)[1]) != 0) {
                                byte[] buf1 = new byte[dstFileLength - (int) cutAreas.get(index)[1]];
                                in.skip(cutAreas.get(index)[1]);
                                in.read(buf1);
                                out.write(buf1);
                            }

                        }
                    } else {
                        byte[] buf = new byte[(int) (cutAreas.get(index)[0] - cutAreas.get(index - 1)[1])];
                        in.skip(cutAreas.get(index - 1)[1]);
                        in.read(buf);
                        out.write(buf);
                        if (cutAreas.get(index)[1] < dstFileLength) {
                            byte[] buf1 = new byte[(int) (dstFileLength - cutAreas.get(index)[1])];
                            in.skip(cutAreas.get(index)[1]);
                            in.read(buf1);
                            out.write(buf1);
                        }
                    }
                    index = index + 1;
                }
                //读写完毕
                in.close();
                out.close();
                dstFile.delete();
//		        outFile.renameTo(dstFile);
                pcm2wav(outFile.getAbsolutePath(), outFile.getAbsolutePath().replace(".pcm", ".wav"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * pcm转换成wav
     *
     * @param pcmPath
     * @param wavPath
     */
    public static void pcm2wav(String pcmPath, String wavPath) {
        PcmToWavUtil p2w = new PcmToWavUtil(Constant.sampleRateInHz, Constant.channelConfig, Constant.audioFormat);
        try {
            p2w.pcmToWav(pcmPath, wavPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 根据当前的像素点位置，获取对应的当前数据源的位置（有误差）
     *
     * @param totlePixs   waveView控件的总长度
     * @param currentPixs 当前像素点
     * @param pcmSize     pcm数据的总长
     * @return
     */
    public static long getCurrentPos(int totlePixs, int currentPixs, long pcmSize) {
        long result = 0;
        if (totlePixs != 0) {
            result = pcmSize * currentPixs / totlePixs;
        }
        return result;


    }

    /**
     * 截取wav音频文件
     *
     * @param sourcePath 源文件地址
     * @param targetPath 目标文件地址
     * @param start      截取开始时间（秒）
     * @param end        截取结束时间（秒）
     *                   <p>
     *                   return  截取成功返回true，否则返回false
     */
    public static boolean cutAudioFile(String sourcePath, String targetPath, int start, int end, long duraciton) {
//        try {
//            Log.e("AudioUtils", "sourcePath=="+sourcePath);
//            Log.e("AudioUtils", "targetPath=="+targetPath);
//            Log.e("AudioUtils", "start=="+start);
//            Log.e("AudioUtils", "end=="+end);
//            Log.e("AudioUtils", "duraciton=="+duraciton);
//            long time = duraciton;
////            long t1 = time / 1000;  //总时长(秒)
//            if (start < 0 || end <= 0 || start >= time || end > time || start >= end) {
//                return false;
//            }
//            File file = new File(sourcePath);
//            FileInputStream fis = new FileInputStream(file);
//            long wavSize = file.length() - 44;  //音频数据大小（44为128kbps比特率wav文件头长度）
//            long splitSize = (wavSize / time) * (end - start);  //截取的音频数据大小
//            long skipSize = (wavSize / time) * start;  //截取时跳过的音频数据大小
//            int splitSizeInt = Integer.parseInt(String.valueOf(splitSize));
//            int skipSizeInt = Integer.parseInt(String.valueOf(skipSize));
//
//            ByteBuffer buf1 = ByteBuffer.allocate(4);  //存放文件大小,4代表一个int占用字节数
//            buf1.putInt(splitSizeInt + 36);  //放入文件长度信息
//            byte[] flen = buf1.array();  //代表文件长度
//            ByteBuffer buf2 = ByteBuffer.allocate(4);  //存放音频数据大小，4代表一个int占用字节数
//            buf2.putInt(splitSizeInt);  //放入数据长度信息
//            byte[] dlen = buf2.array();  //代表数据长度
//            flen = reverse(flen);  //数组反转
//            dlen = reverse(dlen);
//            byte[] head = new byte[44];  //定义wav头部信息数组
//            fis.read(head, 0, head.length);  //读取源wav文件头部信息
//            for (int i = 0; i < 4; i++) {  //4代表一个int占用字节数
//                head[i + 4] = flen[i];  //替换原头部信息里的文件长度
//                head[i + 40] = dlen[i];  //替换原头部信息里的数据长度
//            }
//            byte[] fbyte = new byte[splitSizeInt + head.length + 4];  //存放截取的音频数据
//            for (int i = 0; i < head.length; i++) {  //放入修改后的头部信息
//                fbyte[i] = head[i];
//            }
//
//            int oneCount = 1024 * 1024 * 50;
//            if (skipSizeInt <= oneCount) {
//                byte[] skipBytes = new byte[skipSizeInt];  //存放截取时跳过的音频数据
//                fis.read(skipBytes, 0, skipBytes.length);  //跳过不需要截取的数据
//            } else {
//                int loopCount = skipSizeInt / oneCount;
//                for (int i = 0; i < loopCount; i++) {
//                    byte[] skipBytes = new byte[oneCount];  //存放截取时跳过的音频数据
//                    fis.read(skipBytes, 0, skipBytes.length);  //跳过不需要截取的数据
//                }
//
//                if (skipSizeInt % oneCount != 0) {
//                    byte[] skipBytes = new byte[skipSizeInt % oneCount];  //存放截取时跳过的音频数据
//                    fis.read(skipBytes, 0, skipBytes.length);  //跳过不需要截取的数据
//                }
//            }
//
//            fis.read(fbyte, head.length, fbyte.length - head.length);  //读取要截取的数据到目标数组
//            fis.close();
//            byte[] sBytes = hexStringToBytes(END);
//            fbyte[fbyte.length - 4] = sBytes[0];
//            fbyte[fbyte.length - 3] = sBytes[1];
//            fbyte[fbyte.length - 2] = sBytes[2];
//            fbyte[fbyte.length - 1] = sBytes[3];
//
//            File target = new File(targetPath);
//            if (target.exists()) {  //如果目标文件已存在，则删除目标文件
//                target.delete();
//            }
//            FileOutputStream fos = new FileOutputStream(target);
//            fos.write(fbyte);
//            fos.flush();
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;

        try {
            File file;
            RandomAccessFile src_file, dest_file;
            byte[] buffer;
            long data_length, start_index;
            long tmp;
            int sample = 0;
            WaveHeader header = new WaveHeader();

            buffer = new byte[1024 * 1024];
            file = new File(targetPath);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            src_file = new RandomAccessFile(sourcePath, "r");
            dest_file = new RandomAccessFile(file, "rws");

            src_file.read(buffer, 0, 44);
            dest_file.write(buffer, 0, 44);


            for (int i = 0; i < 4; i++) {
                int num = buffer[27 - i] & 0xff;
                sample = num + (sample << 8);
            }
            header.fileLength = 0;
            header.FmtHdrLeth = 16;
            header.BitsPerSample = 16;//位数 AUDIOENCODING
            header.Channels = buffer[22];//音频通道 1，2 channel==AudioFormat.CHANNEL_IN_MONO?1:2;//
            header.FormatTag = 0x0001;
            header.SamplesPerSec = sample;//16000;//采样率 16000
            header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);
            header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
            header.DataHdrLeth = 0;


            tmp = start;
            start_index = (tmp * header.AvgBytesPerSec / 1000);
            start_index = (start_index % 4) != 0 ? start_index - (start_index % 4) : start_index;
            src_file.seek(start_index + 44);


            tmp = end - start;
            tmp = (tmp * header.AvgBytesPerSec);
            data_length = tmp / 1000;
            data_length = (data_length % 4) != 0 ? data_length - (data_length % 4) : data_length;

            Log.i(TAG, start + "--" + end + "--" + start_index + "--" + data_length);

            for (Long i = data_length; i > 0; i -= buffer.length) {
                int num = (int) Math.min(i, (long) buffer.length);
                src_file.read(buffer, 0, num);
                dest_file.write(buffer, 0, num);
                Log.i(TAG, "--" + i);
            }

            dest_file.seek(0);
            header.DataHdrLeth = (int) data_length;
            header.fileLength = header.DataHdrLeth + 36;
            byte[] h = header.getHeader();

            dest_file.write(h, 0, 44);
            dest_file.close();
            src_file.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 数组反转
     *
     * @param array
     */
    public static byte[] reverse(byte[] array) {
        byte temp;
        int len = array.length;
        for (int i = 0; i < len / 2; i++) {
            temp = array[i];
            array[i] = array[len - 1 - i];
            array[len - 1 - i] = temp;
        }
        return array;
    }


    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


}
