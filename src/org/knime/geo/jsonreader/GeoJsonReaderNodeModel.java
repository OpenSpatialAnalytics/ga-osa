package org.knime.geo.jsonreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
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
import org.knime.geoutils.FeatureGeometry;
import org.knime.geoutils.ShapeFileFeatureExtractor;
import org.knime.geoutils.ShapeToKnime;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * This is the model implementation of GeoJsonReader.
 * 
 *
 * @author Forkan
 */
public class GeoJsonReaderNodeModel extends NodeModel {
	
	 static final String JSON_FILE = "GeoJsonFile";
	 public final SettingsModelString geoJsonFile = new SettingsModelString(JSON_FILE,"");

    
    /**
     * Constructor for the node model.
     */
    protected GeoJsonReaderNodeModel() {
        super(0, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

    	String fname=geoJsonFile.getStringValue();
    	File jsonFile = new File(fname);
    	CoordinateReferenceSystem crs = new FeatureJSON().readCRS( new FileInputStream(jsonFile));
    	StringWriter s = new StringWriter();
		FeatureJSON io = new FeatureJSON();
		io.writeCRS(crs, s);
		Gson gson = new GsonBuilder().create();			
		JsonObject job = gson.fromJson(s.toString(), JsonObject.class);			
		JsonElement entry=job.get("properties");	
		String crsStr = entry.toString();
        
        SimpleFeatureCollection collection = (SimpleFeatureCollection) new FeatureJSON().readFeatureCollection(new FileInputStream(jsonFile));
        		
        
        DataTableSpec outputSpec = ShapeToKnime.createSpec(collection)[0];
       
        BufferedDataContainer container = exec.createDataContainer(outputSpec);
        
        int size = collection.size();
        
        ArrayList<DataCell []> cellList = ShapeToKnime.createCell(crsStr,collection);
        
    
        for (int i=0; i < cellList.size(); i++ ) {
            int index = i + 1;
            DataCell[] cells = cellList.get(i);
            container.addRowToTable(new DefaultRow("Row"+index, cells));
            exec.checkCanceled();
            exec.setProgress(index / (double)size, "Adding row " + index);
        }
        
        // once we are done, we close the container and return its table
        container.close();
        BufferedDataTable out = container.getTable();
        return new BufferedDataTable[]{out};
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

    	if (geoJsonFile.getStringValue() == null) {
			throw new InvalidSettingsException("No GeoJSON file name specified");
		}
    	
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	geoJsonFile.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	geoJsonFile.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	geoJsonFile.validateSettings(settings);
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

