package org.msh.utils.reportgen.highchart;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public class ChartCreator {

	private ChartOptions options = new ChartOptions();
	
	
	public String getJsonOptions() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(ChartType.class, new JsonSerializer<ChartType>() {

			@Override
			public JsonElement serialize(ChartType arg0, Type arg1,
					JsonSerializationContext arg2) {
				return new JsonPrimitive(arg0.getJsonName());
			}
		});

		Gson gson = builder.create();
		
		return gson.toJson(options);
	}
	
	/**
	 * @return the options
	 */
	public ChartOptions getOptions() {
		return options;
	}
}
