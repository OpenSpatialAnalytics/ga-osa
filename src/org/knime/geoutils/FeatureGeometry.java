package org.knime.geoutils;

import org.geotools.data.simple.SimpleFeatureCollection;

public class FeatureGeometry {
	
	public final String crs;
	public final SimpleFeatureCollection collection;

	public FeatureGeometry(String s, SimpleFeatureCollection c) {
	    this.crs = s;
	    this.collection = c;
	}
}
