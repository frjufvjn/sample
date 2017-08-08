// Decompiled by DJ v3.6.6.79 Copyright 2004 Atanas Neshkov  Date: 2017-08-02 오전 9:13:57
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   WaveData.java

package waveform;

// import com.hansol.audio.util.PropertyUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

// Referenced classes of package com.hansol.audio.extract:
//            SimpleWaveformExtractor


class LogWrite {
	
    private static PrintWriter pw = null;
    private static String logDir = "";
    
    private static String getLogFileName()
    {
        String strLogFile = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        
        logDir = "C:\\temp";
        
        strLogFile = logDir + "\\" + sdf.format(new Date()) + ".log";
        return strLogFile;
    }
    
    public static void putLog(String logStr)
    {
        String strLogFile = getLogFileName();
        
        if( strLogFile != null )
        {
            try
            {
                pw = new PrintWriter(new FileWriter(strLogFile, true), true);
                //pw.print(strLogTime);
                pw.print(" ");
                pw.println(logStr);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if( pw != null )
                {
                    pw.close();
                    pw = null;
                }
            }
        }

        System.out.println(logStr);
    }    

}

public class WaveData
{

    public WaveData()
    {
    }

    private static void initialize()
    {
        try
        {
//            String path = (new StringBuilder(String.valueOf(System.getProperty("jedi.home")))).append("/webapps/conf.properties").toString();
//            prop = PropertyUtil.loadProperty(path);
//            if(prop != null)
//            {
//                iPeriod = Integer.parseInt(PropertyUtil.readProperty(prop, "waveform_period") != null ? PropertyUtil.readProperty(prop, "waveform_period").trim() : "1000");
//                iPeak = Integer.parseInt(PropertyUtil.readProperty(prop, "waveform_peak") != null ? PropertyUtil.readProperty(prop, "waveform_peak").trim() : "3");
//                iMinFileSize = Integer.parseInt(PropertyUtil.readProperty(prop, "waveform_MinFileSize") != null ? PropertyUtil.readProperty(prop, "waveform_MinFileSize").trim() : "-1");
//                iMaxFileSize = Integer.parseInt(PropertyUtil.readProperty(prop, "waveform_MaxFileSize") != null ? PropertyUtil.readProperty(prop, "waveform_MaxFileSize").trim() : "-1");
//                iMaxPeriod = Integer.parseInt(PropertyUtil.readProperty(prop, "waveform_MaxPeriod") != null ? PropertyUtil.readProperty(prop, "waveform_MaxPeriod").trim() : "-1");
//            } else
//            {
                iPeriod = 1000;
                iPeak = 3;
                iMinFileSize = 1000;
                iMaxFileSize = -1;
                iMaxPeriod = -1;
            //}
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String extractData(String filePath)
    {
        File audioFile = null;
        int max = 0;
        int min = 0;
        int std = 0;
        try
        {
            initialize();
            audioFile = new File(filePath);
            AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile);
            extractedData = (new SimpleWaveformExtractor()).extract(ais);
            tempArray = Arrays.copyOf(extractedData, extractedData.length);
            Arrays.sort(tempArray);
            max = tempArray[tempArray.length - 1];
            min = tempArray[0];
            std = Math.abs(max) <= Math.abs(min) ? Math.abs(min) : Math.abs(max);
            int cnt = 0;
            if(iMinFileSize != -1 && (long)iMinFileSize >= audioFile.length() / 1000L)
                iPeriod = 10;
            if(iMaxFileSize != -1 && (long)iMaxFileSize <= audioFile.length() / 1000L)
                iPeriod = iMaxPeriod;
            if(buffer != null && buffer.length() != 0)
                buffer.delete(0, buffer.length());
            for(int i = 0; i < extractedData.length; i += iPeriod)
            {
                double b = (double)extractedData[i] / (double)(std / iPeak);
                double parse = Double.parseDouble(String.format("%.2f",b)); // PJW 소숫점 절삭
                if(i < extractedData.length)
                    buffer.append((new StringBuilder(String.valueOf(parse))).append(",").toString());
                else
                    buffer.append(b);
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public static void main(String args[])
    {
//        for(int i = 0; i < 50; i++)
//        {
//            extractData("C:\\Users\\PJW\\Downloads\\cnvu\\sox-14-4-2\\result.wav");
//            System.out.println((new StringBuilder("Count: ")).append(i).toString());
//        }
    	
    	String res = extractData("C:\\Users\\PJW\\Downloads\\cnvu\\sox-14-4-2\\result-16.wav");
    	LogWrite.putLog(res);

    }

    private static Properties prop;
    private static int iPeriod = 0;
    private static int iPeak = 0;
    private static int iMinFileSize = 0;
    private static int iMaxFileSize = 0;
    private static int iMaxPeriod = 0;
    private static int extractedData[] = null;
    private static int tempArray[] = null;
    private static StringBuffer buffer = new StringBuffer();

}