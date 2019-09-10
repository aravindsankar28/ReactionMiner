package tool;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Vector;

public class FileUtils {

	public static File deleteFiles(String path)
	{
		File f=new File(path);
		File[] flist=f.listFiles();

		for (File fl : flist)
		{
			fl.delete();
		}
		return f;
	}
	
	public static void deleteFolder(String path)
	{
		File f=new File(path);
		File[] flist=f.listFiles();
		for (File fl : flist)
		{
			if(fl.isDirectory())
				deleteFolder(fl.getAbsolutePath());
			else
				fl.delete();
		}
		f.delete();
	}

	public static String[] getFilesWithExt(String ext, String path) {
		// TODO Auto-generated method stub
		Vector<String> names=new Vector<String>();
		for(File fl : new File(path).listFiles())
		{
			String name=fl.getName();
			if(name.endsWith(ext))
				names.add(name);
		}
		return names.toArray(new String[1]);
	}

	public static void writeToFile(String fileName, String contents) throws FileNotFoundException {
		// TODO Auto-generated method stub
		PrintWriter out=new PrintWriter(fileName);
		out.print(contents);
		out.close();
	}
	  public static String getFileContents(String fileName)
	  { File file = new File(fileName);
	    DataInputStream inputDataStream;
	    String results = null;

	    try
	    { int length = (int)file.length(), bytesRead;
	      byte byteArray[] = new byte[length];

	      ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(length);
	      FileInputStream       inputStream = new FileInputStream(file);
	      bytesRead = inputStream.read(byteArray);
	      bytesBuffer.write(byteArray, 0, bytesRead);
	      inputStream.close();

	      results = bytesBuffer.toString();
	    }
	    catch(Exception e)
	    {
	      System.out.println("Exception in getFileContents(" + fileName + "), msg=" + e);
	    }
	 
	    return results;
	  }
}
