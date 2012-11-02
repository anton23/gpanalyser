package uk.ac.imperial.doc.gpa.forecasting.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Provide simplified file I/O functions
 * 
 * @author mcg05
 */
public class FileExtra
{
	/**
	 * Write {@code text} to {@code filename}
	 * and create missing directories described
	 * in {@code filename}
	 * 
	 * @param filename
	 * @param text
	 * @return true iff write operation was successful
	 */
	public static boolean writeToTextFile(String filename, String text)
	{
		// Write data to file
		try {
			File file = new File(filename);
			file.getParentFile().mkdirs();
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write(text);
			out.close();
			return true;
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/** 
	 * @param filename
	 * @return content of filename as a string.
	 * 		   On error null is returned.
	 */
	public static String readFromTextFile(String filename)
	{
		// Read data from file
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
			StringBuffer fileData = new StringBuffer(1000);
	        char[] buf = new char[1024];
	        int numRead=0;
	        while((numRead=reader.read(buf)) != -1) {
	        	String readData = String.valueOf(buf, 0, numRead);
	        	fileData.append(readData);
			}
			reader.close();
			return fileData.toString();
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	public static String[] getFileInDirectory(String path, String match)
	{
		  ArrayList<String> files = new ArrayList<String>();
		  File folder = new File(path);
		  File[] listOfFiles = folder.listFiles(); 
		 
		  for (int i = 0; i < listOfFiles.length; i++) 
		  {
			  if (listOfFiles[i].isFile()) 
			  {
				  String file = listOfFiles[i].getPath();
				  if (match == null || file.contains(match)) {
					  files.add(file);
				  }
		      }
		  }
		  Collections.sort(files);
		  return files.toArray(new String[files.size()]);
	}
}
