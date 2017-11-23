package org.msh.utils;

import org.jboss.seam.annotations.Name;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

@Name("textFileReader")
public class TextFileReader {
	  
	public String readFile() throws IOException {
    	FileInputStream fileStream = (FileInputStream) getClass().getClassLoader().getResourceAsStream("\\META-INF\\ula_en.txt");
    	  try {
    	    FileChannel fc = fileStream.getChannel();
    	    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
    	    return Charset.defaultCharset().decode(bb).toString();
    	  }
    	  finally {
    		  fileStream.close();
    	  }
    	}
}