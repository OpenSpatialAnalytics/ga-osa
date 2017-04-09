package org.knime.geo.reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.geoutils.FeatureGeometry;
import org.knime.geoutils.ShapeFileFeatureExtractor;
import org.knime.geoutils.ShapeToKnime;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of ShapeFileReader.
 * Read a shapefile
 *
 * @author Forkan
 */
public class ShapeFileReaderNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(ShapeFileReaderNodeModel.class);
       
    static final String CFG_SHP_FILE = "ShpFile";
    public final SettingsModelString shpFile = new SettingsModelString(CFG_SHP_FILE,"");

    /**
     * Constructor for the node model.
     */
    protected ShapeFileReaderNodeModel() {
        super(0, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

        logger.info("Inside Shapefile redeader model");

        String fname=shpFile.getStringValue();
        FeatureGeometry featureGeometry = ShapeFileFeatureExtractor.getShapeFeature(fname);
        String crs = featureGeometry.crs;
        SimpleFeatureCollection collection = featureGeometry.collection;
        		
        
        DataTableSpec outputSpec = ShapeToKnime.createSpec(collection)[0];
       
        BufferedDataContainer container = exec.createDataContainer(outputSpec);
        
        int size = collection.size();
        
        ArrayList<DataCell []> cellList = ShapeToKnime.createCell(crs,collection);
        
    
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
    	
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

        // TODO save user settings to the config object.
        
    	this.shpFile.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // TODO load (valid) settings from the config object.
        // It can be safely assumed that the settings are valided by the 
        // method below.
        
    	this.shpFile.loadSettingsFrom(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // TODO check if the settings could be applied to our model
        // e.g. if the count is in a certain range (which is ensured by the
        // SettingsModel).
        // Do not actually set any values of any member variables.

    	this.shpFile.validateSettings(settings);

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        
        // TODO load internal data. 
        // Everything handed to output ports is loaded automatically (data
        // returned by the execute method, models loaded in loadModelContent,
        // and user settings set through loadSettingsFrom - is all taken care 
        // of). Load here only the other internals that need to be restored
        // (e.g. data used by the views).

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
       
        // TODO save internal models. 
        // Everything written to output ports is saved automatically (data
        // returned by the execute method, models saved in the saveModelContent,
        // and user settings saved through saveSettingsTo - is all taken care 
        // of). Save here only the other internals that need to be preserved
        // (e.g. data used by the views).

    }
    
    protected String getShapeFileName()
    {
    	
    	return this.shpFile.getStringValue();
    }

}

