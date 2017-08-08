// Decompiled by DJ v3.6.6.79 Copyright 2004 Atanas Neshkov  Date: 2017-08-02 ¿ÀÀü 9:40:05
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   PropertyUtil.java

package waveform;

import java.io.*;
import java.util.*;

public class PropertyUtil
{

    public PropertyUtil()
    {
    }

    public static Properties loadProperty(String filePath)
    {
        Properties prop = new Properties();
        try
        {
            prop = loadProperty(((InputStream) (new FileInputStream(filePath))));
        }
        catch(FileNotFoundException ffe)
        {
            ffe.printStackTrace();
            prop = null;
        }
        return prop;
    }

    public static Properties loadProperty(InputStream filePath)
    {
        Properties prop = new Properties();
        try
        {
            prop.load(filePath);
        }
        catch(FileNotFoundException ffe)
        {
            ffe.printStackTrace();
            prop = null;
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
            prop = null;
        }
        catch(NullPointerException npe)
        {
            npe.printStackTrace();
            prop = null;
        }
        return prop;
    }

    public static String readProperty(Properties property, String key)
    {
        String message = "No Message";
        if(property != null)
            message = property.getProperty(key);
        return message;
    }

    public static String[] readProperties(Properties property, String key)
    {
        String tmpTarget = "";
        String keyString[] = null;
        Vector keyList = new Vector();
        Object objList[] = property.keySet().toArray();
        Arrays.sort(objList);
        Object aobj[] = objList;
        int i = aobj.length;
        for(int j = 0; j < i; j++)
        {
            Object obj = aobj[j];
            if(obj.toString().contains(key))
                keyList.addElement(obj.toString());
        }

        if(keyList.size() != 0)
            keyString = (String[])Arrays.copyOf(keyList.toArray(), keyList.size());
        return keyString;
    }

    public static String[] readPropertiesValues(Properties property, String key)
    {
        String valueString[] = null;
        String keyString[] = null;
        keyString = readProperties(property, key);
        if(keyString != null && keyString.length != 0)
        {
            valueString = new String[keyString.length];
            for(int i = 0; i < keyString.length; i++)
                valueString[i] = readProperty(property, keyString[i]);

        }
        return valueString;
    }

    public static String addPrefix(String rexp, String pre)
    {
        if(rexp.indexOf(pre) != 0)
            rexp = (new StringBuilder()).append(pre).append(rexp).toString();
        return rexp;
    }

    public static void makeFile(File path)
    {
        try
        {
            if(!path.exists())
            {
                File dir = new File(path.getPath().replace(path.getName(), ""));
                if(!dir.exists())
                    dir.mkdirs();
                if(!path.exists())
                    path.createNewFile();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String args[])
    {
        String a = addPrefix("test/run/app", "/");
        System.out.println(a);
        String b = addPrefix("/test/run/app", "/");
        System.out.println(b);
    }
}