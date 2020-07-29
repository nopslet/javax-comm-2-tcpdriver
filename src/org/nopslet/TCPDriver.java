package org.nopslet;

import javax.comm.CommDriver;
import javax.comm.CommPort;
import javax.comm.CommPortIdentifier;
import java.io.*;
import java.util.Vector;

public class TCPDriver implements CommDriver {
    private static String[] parsePropsFile(InputStream paramInputStream)
    {
        Vector localVector = new Vector();
        int i;
        try
        {
            byte[] arrayOfByte = new byte[4096];
            i = 0;
            int j = 0;
            int k;
            while ((k = paramInputStream.read()) != -1)
            {
                String str;
                switch (k)
                {
                    case 10:
                    case 13:
                        if (i > 0)
                        {
                            str = new String(arrayOfByte, 0, 0, i);
                            localVector.addElement(str);
                        }
                        i = 0;
                        j = 0;
                        break;
                    case 35:
                        j = 1;
                        if (i > 0)
                        {
                            str = new String(arrayOfByte, 0, 0, i);
                            localVector.addElement(str);
                        }
                        i = 0;
                        break;
                    default:
                        if ((j == 0) && (i < 4096)) {
                            arrayOfByte[(i++)] = ((byte)k);
                        }
                        break;
                }
            }
        }
        catch (Throwable localThrowable)
        {
            System.err.println("Caught " + localThrowable + " parsing prop file.");
        }
        if (localVector.size() > 0)
        {
            String[] arrayOfString = new String[localVector.size()];
            for (i = 0; i < localVector.size(); i++) {
                arrayOfString[i] = ((String)localVector.elementAt(i));
            }
            return arrayOfString;
        }
        return null;
    }

    private void addAddresses(String propertiesFileName)
            throws IOException
    {
        File propertiesFile = new File(propertiesFileName);

        BufferedInputStream is = new BufferedInputStream(new FileInputStream(propertiesFile));

        String[] arrayOfString = parsePropsFile(is);
        if (arrayOfString != null) {
            for (int i = 0; i < arrayOfString.length; i++) {
                if (arrayOfString[i].regionMatches(true, 0, "tcpaddr=", 0, 8))
                {
                    String str = arrayOfString[i].substring(8);
                    str.trim();
                    CommPortIdentifier.addPortName(str, 1, /*serial*/ this);
                }
            }
        }
    }

    private static String findPropFile()
    {
        String str1 = System.getProperty("java.class.path");

        StreamTokenizer localStreamTokenizer = new StreamTokenizer(new StringReader(str1));

        localStreamTokenizer.whitespaceChars(File.pathSeparatorChar, File.pathSeparatorChar);
        localStreamTokenizer.wordChars(File.separatorChar, File.separatorChar);
        localStreamTokenizer.ordinaryChar(46);
        localStreamTokenizer.wordChars(46, 46);
        try
        {
            while (localStreamTokenizer.nextToken() != -1)
            {
                int i = -1;
                if ((localStreamTokenizer.ttype == -3) &&
                        ((i = localStreamTokenizer.sval.indexOf("comm.jar")) != -1))
                {
                    String str2 = localStreamTokenizer.sval;

                    File localFile = new File(str2);
                    if (localFile.exists())
                    {
                        String str3 = str2.substring(0, i);
                        if (str3 != null) {
                            str3 = str3 + "." + File.separator + "javax.comm.properties";
                        } else {
                            str3 = "." + File.separator + "javax.comm.properties";
                        }
                        localFile = new File(str3);
                        if (localFile.exists()) {
                            return str3;
                        }
                        return null;
                    }
                }
            }
        }
        catch (IOException localIOException) {}
        return null;
    }


    @Override
    public void initialize() {
        try
        {
            System.loadLibrary("javaxcommstubs");
        }
        catch (SecurityException localSecurityException)
        {
            System.err.println("Security Exception win32com: " + localSecurityException);
            return;
        }
        catch (UnsatisfiedLinkError localUnsatisfiedLinkError)
        {
            System.err.println("Error loading win32com: " + localUnsatisfiedLinkError);
            return;
        }


        String str1;
        if ((str1 = System.getProperty("javax.comm.properties")) != null) {
            System.err.println("Comm Drivers: " + str1);
        }
        String str2 = System.getProperty("java.home") +
                File.separator +
                "lib" +
                File.separator +
                "javax.comm.properties";
        String propfilename;
        try
        {
            addAddresses(str2);
        }
        catch (IOException localIOException2)
        {
            propfilename = findPropFile();
            try
            {
                if (propfilename != null) {
                    addAddresses(propfilename);
                }
            }
            catch (IOException localIOException1)
            {
                System.err.println(localIOException1);
            }
        }
    }

    @Override
    public CommPort getCommPort(String addresscolonport, int shouldbe1) {
        try {
            return new TCPSerialPort(addresscolonport);
        } catch (IOException e) {
            return null;
        }
    }
}
