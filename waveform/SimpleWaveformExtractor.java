// Decompiled by DJ v3.6.6.79 Copyright 2004 Atanas Neshkov  Date: 2017-08-02 ¿ÀÀü 9:21:50
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   SimpleWaveformExtractor.java

package waveform;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import org.apache.commons.lang3.ArrayUtils;

// Referenced classes of package com.hansol.audio.extract:
//            WaveformExtractor

public class SimpleWaveformExtractor
    implements WaveformExtractor
{

    public SimpleWaveformExtractor()
    {
    }

    public int[] extract(AudioInputStream in)
    {
        long start = System.currentTimeMillis();
        AudioFormat format = in.getFormat();
        byte audioBytes[] = readBytes(in);
        int result[] = (int[])null;
        int cnt = 0;
        System.out.println("format.getSampleSizeInBits() : " + format.getSampleSizeInBits());
        if(format.getSampleSizeInBits() == 16)
        {
            int samplesLength = audioBytes.length / 2;
            int sampleStep = 2;
            result = new int[samplesLength];
            if(format.isBigEndian())
            {
                System.out.println("BigEndian");
                for(int i = 0; i < samplesLength; i++)
                {
                    byte MSB = audioBytes[i * 2];
                    byte LSB = audioBytes[i * 2 + 1];
                    result[i] = MSB << 8 | 0xff & LSB;
                    cnt++;
                }

            } else
            {
                System.out.println("BigEndian False");
                for(int i = 0; i < samplesLength; i += 2)
                {
                    byte LSB = audioBytes[i * 2];
                    byte MSB = audioBytes[i * 2 + 1];
                    result[i / 2] = MSB << 8 | 0xff & LSB;
                    cnt++;
                }

            }
        } else
        {
            int samplesLength = audioBytes.length;
            result = new int[samplesLength];
            System.out.println("encoding text : " + format.getEncoding().toString());
            if(format.getEncoding().toString().startsWith("PCM_SIGN"))
            {
                for(int i = 0; i < samplesLength; i++)
                    result[i] = audioBytes[i];

            } else
            {
                for(int i = 0; i < samplesLength; i++)
                    result[i] = audioBytes[i] - 128;

            }
        }
        System.out.println((new StringBuilder("Audio Bytes: ")).append(audioBytes.length).toString());
        System.out.println((new StringBuilder("Count: ")).append(cnt).toString());
        System.out.println((new StringBuilder("length: ")).append(result.length).toString());
        System.out.println((new StringBuilder("Before Return of Array: ")).append(result[result.length - 1]).toString());
        int arr_int[] = Arrays.copyOf(result, cnt);
        long end = System.currentTimeMillis();
        long elasped = end - start;
        return arr_int;
    }

    private byte[] readBytes(AudioInputStream in)
    {
        byte result[] = new byte[0];
        byte buffer[] = new byte[32768];
        try
        {
            int bytesRead = 0;
            do
            {
                bytesRead = in.read(buffer);
                result = ArrayUtils.addAll(result, buffer);
            } while(bytesRead != -1);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    private static final int DEFAULT_BUFFER_SIZE = 32768;
}