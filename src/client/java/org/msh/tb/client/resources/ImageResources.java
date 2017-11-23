package org.msh.tb.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface ImageResources extends ClientBundle {

	@Source("../resources/area_32x32.png")
	ImageResource imgChartArea();
	
	@Source("../resources/area_spline_32x32.png")
	ImageResource imgChartAreaSpline();
	
	@Source("../resources/bar_horiz_32x32.png")
	ImageResource imgChartBar();
	
	@Source("../resources/line_32x32.png")
	ImageResource imgChartLine();
	
	@Source("../resources/pie_32x32.png")
	ImageResource imgChartPie();
	
	@Source("../resources/spline_32x32.png")
	ImageResource imgChartSpline();

	@Source("../resources/column_32x32.png")
	ImageResource imgChartColumn();
}
