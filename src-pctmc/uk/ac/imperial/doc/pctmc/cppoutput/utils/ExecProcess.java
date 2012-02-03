package uk.ac.imperial.doc.pctmc.cppoutput.utils;

import java.io.*;

public class ExecProcess {

    static class StreamGobbler extends Thread
    {
        InputStream is;
        String type;

        StreamGobbler(InputStream is, String type)
        {
            this.is = is;
            this.type = type;
        }

        public void run()
        {
            try
            {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ( (line = br.readLine()) != null)
                    System.out.println(type + " > " + line);
            } catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }

    public static void main(String command, int i)
    {
        try
        {
            String[] cmd;
            if (System.getProperty("os.name").toLowerCase().contains("win"))
            {
                cmd = new String[3];
                cmd[0] = "cmd" ;
                cmd[1] = "/C" ;
                cmd[2] = command;
            }
            else
            {
                cmd = new String[3];
                cmd[0] = "/bin/bash";
                cmd[1] = "-c";
                cmd[2] = command;
            }

            System.out.println("executing: " + command);
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmd);
            // any error message?
            StreamGobbler errorGobbler = new
                    StreamGobbler(proc.getErrorStream(), "ERROR" + i);

            // any output?
            StreamGobbler outputGobbler = new
                    StreamGobbler(proc.getInputStream(), "OUTPUT" + i);

            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            // any error???
            proc.waitFor();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
}
