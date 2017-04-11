package org.knime.geoutils;

import java.io.IOException;

import org.geotools.factory.Hints;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class Constants {
	
	public static String GEOM = "the_geom";
	public static String RANK = "rank";
	public static String INDEX = "index";
	public static String OVID = "ovid";
	public static int JsonPrecision = 16;
	public static String localWFS = "http://127.0.0.1:8080/geoserver/wfs";
	
	public static boolean isGeometry(String str)
	{
		try {
			Geometry g = new GeometryJSON().read(str);
			if ( g instanceof Geometry )
				return true;
			else
				return false;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	public static String GeometryToGeoJSON(Geometry geo)
	{
		GeometryJSON json = new GeometryJSON(Constants.JsonPrecision);
		String str = json.toString(geo);
		return str;
	}
	
	public static String GeometryToGeoJSON(Geometry geo, String crs)
	{
		GeometryJSON json = new GeometryJSON(Constants.JsonPrecision);
		String str = json.toString(geo);
		return Constants.AppendCRS(crs, str);
	}
	
	public static String GeometryToGeoJSON(MultiPolygon geo)
	{
		GeometryJSON json = new GeometryJSON(Constants.JsonPrecision);
		String str = json.toString(geo);
		return str;
	}
	
	public static String GeometryToGeoJSON(Polygon geo)
	{
		GeometryJSON json = new GeometryJSON(Constants.JsonPrecision);
		String str = json.toString(geo);
		return str;
	}
	
	public static Geometry GeoJsonToGeometry(String geoJsonString) throws IOException
	{
		Geometry geo = new GeometryJSON().read(geoJsonString);
		return geo;
	}
	
	public static Geometry FeatureToGeometry(String featureStr) throws IOException
	{
		Geometry geo = new GeometryJSON().read(Constants.GetGeoJsonStr(featureStr));
		return geo;
	}
	
	public static String AppendCRS (String crs, String jsonString)
	{
		if (crs.isEmpty() || crs == "")
			return jsonString;
		else{
			String featureStr = crs + "|" + jsonString;
			return featureStr;
		}
	}
	
	public static String GetCRS (String featureStr)
	{
		String[] parts = featureStr.split("\\|");
		if (parts.length > 1 )
			return parts[0]; //first part is the CRS value
		else
			return "";
	}
	
	public static String GetGeoJsonStr (String featureStr)
	{
		String[] parts = featureStr.split("\\|");
		if (parts.length > 1 )
			return parts[1];
		else
			return parts[0];
	}
	
	/* parse crs string by : and get the last part which have EPSG value */
	/*extract 28353 from {"name":"EPSG:28353"}*/
	public static String GetSRID(String crs)
	{
		String[] parts = crs.split(":");
		return  parts[2].substring(0, parts[2].length()-2);
	}
	
	public static String GetCRSCode(String crs)
	{
		String[] parts = crs.split("\"");
		return  parts[3];
	}
	
	public static String GetCrsJson(String srid)
	{
		return "{\"name\":\"EPSG:"+srid+"\"}";
	}
	
	public static MathTransform FindMathTransform(String crsStr1, String crsStr2) throws NoSuchAuthorityCodeException, FactoryException 
	{
		CoordinateReferenceSystem srcCRS = CRS.decode(crsStr2);
		CoordinateReferenceSystem targetCRS = CRS.decode(crsStr1);				 
		
		Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
    	CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", hints);    	
    	if (CRS.getAxisOrder(targetCRS) == CRS.AxisOrder.NORTH_EAST || CRS.getAxisOrder(targetCRS) == CRS.AxisOrder.LAT_LON){	
    		targetCRS = factory.createCoordinateReferenceSystem(crsStr1);
    	}    	    	
    	if (CRS.getAxisOrder(srcCRS) == CRS.AxisOrder.NORTH_EAST || CRS.getAxisOrder(srcCRS) == CRS.AxisOrder.LAT_LON){		
			srcCRS = factory.createCoordinateReferenceSystem(crsStr2);
		}						
		MathTransform transform = CRS.findMathTransform(srcCRS, targetCRS, true);
		return transform;
	}
	

}
