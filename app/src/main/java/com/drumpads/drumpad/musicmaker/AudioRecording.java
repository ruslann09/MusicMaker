package com.drumpads.drumpad.musicmaker;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.SequenceInputStream;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AudioRecording implements Serializable {
    private Map<String, Long> recordMap;
    private String recordName = "test.wav";

    public AudioRecording () {
        recordMap = new HashMap<>();
    }

    public void setRecordName (String recordName) {
        this.recordName = recordName;
    }

    public void addSound (String id, Long time) {
        recordMap.put(id, time);
    }

    public void loadSoundFile (Context context) {
        try {
            ArrayList<String> sources = new ArrayList<String>();

            for (Map.Entry<String, Long> recordInit : recordMap.entrySet()) {
                sources.add((String)(recordInit.getKey()));
            }

            final String[] sourceFiles = new String[sources.size()];

            for (int i = 0; i < sources.size(); i++)
                sourceFiles[i] = (String) (sources.get(i));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (sourceFiles.length > 3) {
                        merge2WavFiles(sourceFiles[0], sourceFiles[1], recordName + ".wav");
                        merge2WavFiles(recordName + ".wav", sourceFiles[2], recordName + ".wav");
                        merge2WavFiles(recordName + ".wav", sourceFiles[3], recordName + ".wav");
                    }
                }
            }).start();
        } catch (Exception e) {
            Toast.makeText(context, e.toString() + "", Toast.LENGTH_SHORT).show();
        }
    }

    private void merge2WavFiles(String file1, String file2, String newWavFilePath) {
        try {
            FileInputStream fis1 = new FileInputStream(file1);
            FileInputStream fis2 = new FileInputStream(file2);
            SequenceInputStream sis = new SequenceInputStream(fis1,fis2);

            FileOutputStream fos = new FileOutputStream(new File(newWavFilePath));

            int temp;

            try {
                while ((temp = sis.read())!= -1){

                    fos.write(temp);

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

//    private void merge2WavFiles(String file1, String file2, String outName) {
//        FileInputStream in1 = null, in2 = null;
//        FileOutputStream out = null;
//        long totalAudioLen = 0;
//        long totalDataLen = totalAudioLen + 36;
//        long longSampleRate = 44100;
//        int channels = 2;
//        long byteRate = 16 * 44100 * channels / 8;
//
//        byte[] data = new byte[2048];
//
//        try {
//            in1 = new FileInputStream(file1);
//            in2 = new FileInputStream(file2);
//
//            out = new FileOutputStream(outName);
//
//            totalAudioLen = in1.getChannel().size() + in2.getChannel().size();
//            totalDataLen = totalAudioLen + 36;
//
//            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
//                    longSampleRate, channels, byteRate);
//
//            while (in1.read(data) != -1) {
//
//                out.write(data);
//
//            }
//            while (in2.read(data) != -1) {
//
//                out.write(data);
//            }
//
//            out.close();
//            in1.close();
//            in2.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
//                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
//            throws IOException {
//
//        byte[] header = new byte[44];
//
//        header[0] = 'R';
//        header[1] = 'I';
//        header[2] = 'F';
//        header[3] = 'F';
//        header[4] = (byte)(totalDataLen & 0xff);
//        header[5] = (byte)((totalDataLen >> 8) & 0xff);
//        header[6] = (byte)((totalDataLen >> 16) & 0xff);
//        header[7] = (byte)((totalDataLen >> 24) & 0xff);
//        header[8] = 'W';
//        header[9] = 'A';
//        header[10] = 'V';
//        header[11] = 'E';
//        header[12] = 'f';
//        header[13] = 'm';
//        header[14] = 't';
//        header[15] = ' ';
//        header[16] = 16;
//        header[17] = 0;
//        header[18] = 0;
//        header[19] = 0;
//        header[20] = 1;
//        header[21] = 0;
//        header[22] = (byte) channels;
//        header[23] = 0;
//        header[24] = (byte)(longSampleRate & 0xff);
//        header[25] = (byte)((longSampleRate >> 8) & 0xff);
//        header[26] = (byte)((longSampleRate >> 16) & 0xff);
//        header[27] = (byte)((longSampleRate >> 24) & 0xff);
//        header[28] = (byte)(byteRate & 0xff);
//        header[29] = (byte)((byteRate >> 8) & 0xff);
//        header[30] = (byte)((byteRate >> 16) & 0xff);
//        header[31] = (byte)((byteRate >> 24) & 0xff);
//        header[32] = (byte)(2 * 16 / 8);
//        header[33] = 0;
//        header[34] = 16;
//        header[35] = 0;
//        header[36] = 'd';
//        header[37] = 'a';
//        header[38] = 't';
//        header[39] = 'a';
//        header[40] = (byte)(totalAudioLen & 0xff);
//        header[41] = (byte)((totalAudioLen >> 8) & 0xff);
//        header[42] = (byte)((totalAudioLen >> 16) & 0xff);
//        header[43] = (byte)((totalAudioLen >> 24) & 0xff);
//
//        out.write(header, 0, 44);
//    }
}
