package org.msh.utils.reportgen.highchart;

import com.google.gson.*;

import java.lang.reflect.Type;


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
