package waveform;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Log
{
    private static PrintWriter pw = null;
    private static String logDir = "";
    
    public Log() {
    	
    }
    
    /**
     * 占싸깍옙 占쏙옙占싹몌옙占쏙옙 占쏙옙환占싼댐옙.
     */    
    private static String getLogFileName()
    {
        String strLogFile = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        
        logDir = "C:\\temp";
        
        strLogFile = logDir + "\\" + sdf.format(new Date()) + ".log";
        return strLogFile;
    }
    
    /**
     * 占싸깍옙 占쏙옙占쏙옙 占시곤옙占쏙옙 占쏙옙환占싼댐옙.
     */    
    private static String getLogTime()
    {
        String strLogTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        return sdf.format(new Date());
    }

    /**
     * 占싸깍옙 占쏙옙占싹울옙 Exception 占싸그몌옙 占쏙옙占쏙옙磯占�
     */    
    public static void putLog(Exception ex)
    {
        String strLogFile = getLogFileName();
        String strLogTime = getLogTime();
    
        if( strLogFile != null )
        {
            try
            {
                pw = new PrintWriter(new FileWriter(strLogFile, true), true);
                pw.print(strLogTime);
                pw.print(" ");
                ex.printStackTrace(pw);
                pw.println("");
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

        ex.printStackTrace();
    }

    /**
     * 占싸깍옙 占쏙옙占싹울옙 占싸그몌옙 占쏙옙占쏙옙磯占�
     */    
    public static void putLog(String logStr)
    {
        String strLogFile = getLogFileName();
        String strLogTime = getLogTime();
        
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

        System.out.println(strLogTime + " " + logStr);
    }    

}

