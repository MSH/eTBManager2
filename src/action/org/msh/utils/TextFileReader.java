package org.msh.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.annotations.Name;

@Name("textFileReader")
public class TextFileReader {
	  
	public String readFile() throws IOException {
    	FileInputStream fileStream = (FileInputStream) getClass().getClassLoader().getResourceAsStream("\\WEB-INF\\classes\\org\\msh\\utils\\ula_en.txt");
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