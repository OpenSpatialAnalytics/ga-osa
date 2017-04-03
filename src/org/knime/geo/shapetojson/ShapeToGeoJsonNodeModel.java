package org.knime.geo.shapetojson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.geoutils.Constants;
import org.knime.geoutils.ShapeFileFeatureExtractor;
import org.knime.geoutils.ShapeToKnime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * This is the model implementation of ShapeToGeoJson.
 * 
 *
 * @author Forkan
 */
public class ShapeToGeoJsonNodeModel extends NodeModel {
	
	static final String CFG_SHP_FILE = "ShpFile";
    public final SettingsModelString shpFile = new SettingsModelString(CFG_SHP_FILE,"");
    static final String CFG_LOC = "FilePath";
    public final SettingsModelString jsonFileLoc =new SettingsModelString(CFG_LOC,"");
    
    /**
     * Constructor for the node model.
     */
    protected ShapeToGeoJsonNodeModel() {
    
        super(0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

    	try{
	    	String fname=shpFile.getStringValue();
	        SimpleFeatureCollection collection = ShapeFileFeatureExtractor.getShapeFeature(fname);
	        String writeFname=jsonFileLoc.getStringValue().concat(".geojson");
	        GeometryJSON gjson = new GeometryJSON(Constants.JsonPrecision);
    		FeatureJSON io = new FeatureJSON(gjson);
    		StringWriter s = new StringWriter();
    		io.writeFeatureCollection(collection, s);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(s.toString());
			String prettyJsonString = gson.toJson(je);
			FileWriter writer = new FileWriter(writeFname);
			writer.write(prettyJsonString);
			writer.close();
    	}
    	catch (Exception e)
		{
			e.printStackTrace();
			
		}
        
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

    	if (shpFile.getStringValue() == null) {
			throw new InvalidSettingsException("No shape file name specified");
		}
    	
    	if (jsonFileLoc.getStringValue() == null) {
			throw new InvalidSettingsException("No GeoJSON file name specified");
		}
    	
    	
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	this.shpFile.saveSettingsTo(settings);
    	this.jsonFileLoc.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	
    	this.shpFile.loadSettingsFrom(settings);
    	this.jsonFileLoc.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	this.shpFile.validateSettings(settings);
    	this.jsonFileLoc.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

}

