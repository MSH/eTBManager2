/**
 * 
 */
package org.msh.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.StreamContextFactory;
import com.rmemoria.datastream.StreamFileTypeXML;

/**
 * Utilities functions to make it easier to handle the Data Stream library
 * 
 * @author Ricardo Memoria
 *
 */
public class DataStreamUtils {

	/**
	 * Create a new context from a XML schema file name assuming that schemas are
	 * located in the META-INF/datastream folder
	 * @param schemaFileName the name of the file name relative to the META-INF/datastream folder
	 * @return implementation of {@link StreamContext} interface to the given schema
	 */
	public static StreamContext createContext(String schemaFileName) {
		URL schema = DataStreamUtils.class.getClassLoader().getResource("META-INF/datastream/" + schemaFileName);
		StreamContext context = StreamContextFactory.createContext(schema);
		return context;
	}
	
	/**
	 * Create an XML marshaller from the given schema file located in the META-INF/datastream folder  
	 * @param schemaFileName name of the schema file name
	 * @return implementation of the {@link DataMarshaller} interface for XML generation of the given schema
	 */
	public static DataMarshaller createXMLMarshaller(String schemaFileName) {
		StreamContext context = createContext(schemaFileName);
		return context.createMarshaller(StreamFileTypeXML.class);
	}
	
	/**
	 * Create an XML unmarshaller from the given schema file located in the META-INF/datastream folder
	 * @param schemaFileName name of the schema file name
	 * @return implementation of the {@link DataUnmarshaller} interface for XML generation of the given schema
	 */
	public static DataUnmarshaller createXMLUnmarshaller(String schemaFileName) {
		StreamContext context = createContext(schemaFileName);
		return context.createUnmarshaller(StreamFileTypeXML.class);
	}
	
	/**
	 * Create an XML marshaller from the given schema file located in the META-INF/datastream folder  
	 * @param context instance of {@link StreamContext}
	 * @return implementation of the {@link DataMarshaller} interface for XML generation of the given schema
	 */
	public static DataMarshaller createXMLMarshaller(StreamContext context) {
		return context.createMarshaller(StreamFileTypeXML.class);
	}
	
	
	/**
	 * Create an XML unmarshaller from the given schema file located in the META-INF/datastream folder
	 * @param context instance of {@link StreamContext}
	 * @return implementation of the {@link DataUnmarshaller} interface for XML generation of the given schema
	 */
	public static DataUnmarshaller createXMLUnmarshaller(StreamContext context) {
		return context.createUnmarshaller(StreamFileTypeXML.class);
	}
	
	/**
	 * Create an instance of {@link OutputStream} that stores its data in a string buffer.
	 * When it is finished, just use the method {@link OutputStream#toString()} to return 
	 * the generated string
	 * @return instance of {@link OutputStream}
	 */
	public static OutputStream createStringOutputStream() {
		return new OutputStream() {
			private StringBuilder builder = new StringBuilder();
			@Override
			public void write(int b) throws IOException {
				builder.append((char)b);
			}
			
			@Override
			public String toString() {
				return builder.toString();
			}
		}; 
	}
	
	/**
	 * Create an input stream from a string. Make it easier to unmarshall object
	 * from a string containing the data stream
	 * @param s string to be converted to an {@link InputStream}
	 * @return instance of {@link InputStream}
	 */
	public static InputStream createStringInputStream(String s) {
		return new ByteArrayInputStream(s.getBytes());
	}
}
