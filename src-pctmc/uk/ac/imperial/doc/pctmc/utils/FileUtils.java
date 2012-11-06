package uk.ac.imperial.doc.pctmc.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;

import org.jfree.data.xy.XYDataset;

public class FileUtils {

	public static void writeGeneralFile(String contents, String fileName) {
		createNeededDirectories(fileName);
		File file = new File(fileName);
		try {
			Writer out = new BufferedWriter(new FileWriter(file));
			out.write(contents);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static String stripFileName(String filename) {
		String[] tmp = filename.split("/");
		return tmp[tmp.length-1];
	}

	public static void createNeededDirectories(String filename) {
		String[] pathTmp = filename.split("/");
		String path = "";
		for (int i = 0; i < pathTmp.length - 1; i++) {
			path += pathTmp[i] + "/";
		}
		new File(path).mkdirs();
	}

	public static void writeGnuplotFile(String filename, String model,
			List<String> labels, String xlabel, String ylabel) {
		FileUtils.createNeededDirectories(filename);
		String[] pathTmp = filename.split("/");
		File file = new File(filename + ".gnuplot");

		String strippedFilename = pathTmp[pathTmp.length - 1];
		try {
			Writer out = new BufferedWriter(new FileWriter(file));
			out.write("set terminal postscript eps enhanced color \n");
			out.write("set output \"" + strippedFilename + ".eps" + "\"\n");
			// out.write("set style line 1 lc rgb \"#53be42\" lw 4\n");
			// out.write("set style line 2 lc rgb \"#69b8ca\" lw 4\n");
			// out.write("set style line 3 lc rgb \"#cb7163\" lw 4\n");
			// out.write("set style increment user\n");
			out.write("set title \"" + model + "\" \n");
			out.write("set xlabel \"" + xlabel + "\"\n");
			out.write("set ylabel \"" + ylabel + "\"\n");
			out.write("plot");

			for (int i = 0; i < labels.size(); i++) {
				out.write(" \"" + strippedFilename + "\" using 1:" + (i + 2)
						+ " title \"" + labels.get(i) + "\" with lines lw 2");
				if (i != labels.size() - 1) {
					out.write(",\\\n");
				}
			}
			out.close();
		} catch (Exception e) {

		}
	}

	public static void writeCSVfile(String filename, XYDataset dataset) {
		File file = new File(filename);
		Writer out;
		try {
			out = new BufferedWriter(new FileWriter(file));

			for (int dataPoint = 0; dataPoint < dataset.getItemCount(0); dataPoint++) {
				out.write(dataset.getX(0, dataPoint) + "");
				for (int series = 0; series < dataset.getSeriesCount(); series++) {
					out.write(" " + dataset.getY(series, dataPoint));
				}
				out.write("\n");
			}
			out.close();
		} catch (Exception e) {
			PCTMCLogging
					.error("Could not write to the file - please make sure the given path exists.");
		}
	}
		
	public static String gnuplotEscape(String str){
		String ret = str.replace("\"", "\\\"");
		return ret;
	}
	
	public static void write3DGnuplotFile(String filename, String xlabel, String ylabel, String zlabel) {
		FileUtils.createNeededDirectories(filename);
		String[] pathTmp = filename.split("/");
		File file = new File(filename + ".gnuplot");

		String strippedFilename = pathTmp[pathTmp.length - 1];
		try {
			Writer out = new BufferedWriter(new FileWriter(file));
			out.write("set terminal postscript eps enhanced color \n");
			out.write("set output \"" + strippedFilename + ".eps" + "\"\n");
			out.write("set title \"" + gnuplotEscape(zlabel) + "\" \n");
			out.write("set xlabel \"" + xlabel + "\"\n");
			out.write("set ylabel \"" + ylabel + "\"\n");
			out.write("splot '" + strippedFilename +"' with pm3d\n");
			out.close();
		} catch (Exception e) {

		}
	}

	public static String readFile(String filename) throws IOException {
		FileInputStream stream = new FileInputStream(new File(filename));
		FileChannel fc = stream.getChannel();
		MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		String ret =  Charset.defaultCharset().decode(buffer).toString();
		stream.close();
		return ret;
	}
	
	
	public static void write3Dfile(String filename,double[][] data,double minx,double dx,double miny,double dy) {
		File file = new File(filename);
		Writer out;
		try {
			out = new BufferedWriter(new FileWriter(file));
		
			for (int i = 0; i<data.length; i++){
				if (i>0) out.write("\n");
				for (int j = 0; j<data[i].length; j++){
					out.write((minx+i*dx) + " " + (miny + j*dy) + " " + data[i][j]+"\n");
				}
			}
			
			out.close();
		} catch (Exception e) {
			PCTMCLogging
					.error("Could not write to the file - please make sure the given path exists.");
		}
	}

}
