package org.knime.geoutils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.geojson.feature.FeatureJSON;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class ShapeFileFeatureExtractor {
	
	/***
	 * 
	 * @param path of shapefile
	 * @return return the whole shape file as SimpleCollection
	 */
	public static FeatureGeometry getShapeFeature(String fileName)
	{
		SimpleFeatureCollection collection;
		
		try {
    		
    		File file = new File(fileName);
			
			Map<String, URL> map = new HashMap<String, URL>();      
			map.put("url", file.toURI().toURL());
	
			DataStore dataStore = DataStoreFinder.getDataStore(map);
			String typeName = dataStore.getTypeNames()[0];
			
			SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
			
			/* get CRS value */
			SimpleFeatureType schema = featureSource.getSchema();
			CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();
			StringWriter s = new StringWriter();
			FeatureJSON io = new FeatureJSON();
			io.writeCRS(crs,  s);
			Gson gson = new GsonBuilder().create();			
			JsonObject job = gson.fromJson(s.toString(), JsonObject.class);			
			JsonElement entry=job.get("properties");	
			String key = entry.toString();
			
			/* get total collection */
			collection = featureSource.getFeatures();
			FeatureGeometry featureGeometry = new FeatureGeometry(key, collection);
			
			dataStore.dispose();
			
			return featureGeometry;
			
    	}catch (Exception e) {
			e.printStackTrace();
			return null;
		} 	
	}
	
	public static FeatureGeometry getGeometryFeature(String fileName) 
	{
		SimpleFeatureCollection collection;
		
		try {
    		
    		File file = new File(fileName);
			
			Map<String, URL> map = new HashMap<String, URL>();      
			map.put("url", file.toURI().toURL());
	
			DataStore dataStore = DataStoreFinder.getDataStore(map);
			String typeName = dataStore.getTypeNames()[0];
			
			SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
			FeatureType schema = featureSource.getSchema();
			CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();
			StringWriter s = new StringWriter();
			FeatureJSON io = new FeatureJSON();
			io.writeCRS(crs,  s);
			Gson gson = new GsonBuilder().create();			
			JsonObject job = gson.fromJson(s.toString(), JsonObject.class);			
			JsonElement entry=job.get("properties");	
			String key = entry.toString();
			
	        String name = schema.getGeometryDescriptor().getLocalName();
	        Query query = new Query(typeName, Filter.INCLUDE, new String[] { name });
			collection = featureSource.getFeatures(query);
			
			FeatureGeometry featureGeometry = new FeatureGeometry(key, collection);
			
			dataStore.dispose();
			
			return featureGeometry;
			
    	}catch (Exception e) {
			e.printStackTrace();
			return null;
		} 	
	}
	
	public static FeatureGeometry getGeometryFeature(String fileName, String filterQuery)
	{
		SimpleFeatureCollection collection;
		
		try {
    		
    		File file = new File(fileName);
			
			Map<String, URL> map = new HashMap<String, URL>();      
			map.put("url", file.toURI().toURL());
	
			DataStore dataStore = DataStoreFinder.getDataStore(map);
			String typeName = dataStore.getTypeNames()[0];
			
			SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
			FeatureType schema = featureSource.getSchema();
	        String name = schema.getGeometryDescriptor().getLocalName();
	        
			CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();
			StringWriter s = new StringWriter();
			FeatureJSON io = new FeatureJSON();
			io.writeCRS(crs,  s);
			Gson gson = new GsonBuilder().create();			
			JsonObject job = gson.fromJson(s.toString(), JsonObject.class);			
			JsonElement entry=job.get("properties");	
			String key = entry.toString();
	        
	        Filter filter = CQL.toFilter(filterQuery);
	        
	        Query query = new Query(typeName, filter, new String[] { name });
			collection = featureSource.getFeatures(query);
			
			FeatureGeometry featureGeometry = new FeatureGeometry(key, collection);
			
			dataStore.dispose();
			
			return featureGeometry;
			
    	}catch (Exception e) {
			e.printStackTrace();
			return null;
		} 	
	}
	
	
	public static FeatureGeometry getShapeFeature(DataStore dataStore, String typeName)
	{
		SimpleFeatureCollection collection;
		
		try {
			
			SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
			
			/* get CRS value */
			SimpleFeatureType schema = featureSource.getSchema();
			CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();
			StringWriter s = new StringWriter();
			FeatureJSON io = new FeatureJSON();
			io.writeCRS(crs,  s);
			Gson gson = new GsonBuilder().create();			
			JsonObject job = gson.fromJson(s.toString(), JsonObject.class);			
			JsonElement entry=job.get("properties");	
			String key = entry.toString();
			
			/* get total collection */
			collection = featureSource.getFeatures();
			FeatureGeometry featureGeometry = new FeatureGeometry(key, collection);
			
			dataStore.dispose();
			
			return featureGeometry;
			
    	}catch (Exception e) {
			e.printStackTrace();
			return null;
		} 	
	}

}
