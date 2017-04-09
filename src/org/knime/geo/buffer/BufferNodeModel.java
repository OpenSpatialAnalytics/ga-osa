package org.knime.geo.buffer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.geotools.geojson.geom.GeometryJSON;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.geoutils.Constants;
import com.vividsolutions.jts.geom.Geometry;


/**
 * This is the model implementation of Buffer.
 * 
 *
 * @author Forkan
 */
public class BufferNodeModel extends NodeModel {
	
	private static final NodeLogger logger = NodeLogger.getLogger(BufferNodeModel.class);
		
	static final String DISTANCE = "distance";
	public final SettingsModelString bufferDistance = new SettingsModelString(DISTANCE, "0.0");
	private double distance = 0.0;
	
		
    /**
     * Constructor for the node model.
     */
    protected BufferNodeModel() {
        super(1,1);        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

    	BufferedDataTable geometryTable = (BufferedDataTable)inData[0];
    	DataTableSpec outSpec = createSpec(geometryTable.getSpec());
    	BufferedDataContainer container = exec.createDataContainer(outSpec); 
    	int geomIndex = geometryTable.getSpec().findColumnIndex(Constants.GEOM); 
    	int numColumns = geometryTable.getSpec().getNumColumns();
    	long tableSize = geometryTable.size();
    
    	int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService es = Executors.newFixedThreadPool(threads);
		List<Future<DataCell []>> futures = new ArrayList<Future<DataCell []>>();
	
		try{
    		distance = Double.parseDouble(bufferDistance.getStringValue());  
    		logger.info("Read buffer disntance"+distance);
    	}
    	catch (NumberFormatException e)
    	{
    		throw new NumberFormatException("Buffer distance must be a double value");
    	}
		
    	int i = 1;
    	for (DataRow row : geometryTable) {
    		  Callable<DataCell []> callable = new Callable<DataCell []>() {
    			  public DataCell [] call() throws Exception {
    				  	DataCell[] cells = new DataCell[outSpec.getNumColumns()];
    				  	try{
    				  		DataCell geometryCell = row.getCell(geomIndex);
    				  		String geoJsonString = ((StringValue) geometryCell).getStringValue();
    				  		String crs = Constants.GetCRS(geoJsonString);
    				  		Geometry geo = Constants.FeatureToGeometry(geoJsonString);
    				  		Geometry gf = geo.buffer(distance);
    				  		String str = Constants.GeometryToGeoJSON(gf, crs);
    				  		cells[geomIndex] = new StringCell(str);
    				  		for ( int col = 0; col < numColumns; col++ ) {
    				  			if (col != geomIndex) {
    				  				cells[col] = row.getCell(col);
    				  			}
    			    		}    	
    				  	}
    				  	catch(Exception e){
    				  		e.printStackTrace();
    				  		logger.error("Something wrong in Geometry cell: "+ row.getKey());
    				  	}
  	    				return cells;
    			  }
    		  };
    		  futures.add(es.submit(callable));
    		  exec.checkCanceled();
    		  exec.setProgress( 0.9 * ((double) i / (double)tableSize ), "Creating Row " + (i+1));
    		  i++;
    	}
    	
    	es.shutdown();
		
		try {
			while(!es.awaitTermination(24, TimeUnit.HOURS));
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Buffer operation could not be completed.");
		}
		   	
		i = 1;
    	for (Future<DataCell []> future : futures) {	 
    		container.addRowToTable(new DefaultRow("Row"+i, future.get()));
	    	exec.checkCanceled();
	    	exec.setProgress( 0.9 + (0.1 * ((double) i / (double)tableSize )), "Adding Row " + i);
	    	i++;
    	}
    	container.close();
    	return new BufferedDataTable[] { container.getTable() };
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
    	
    	if (bufferDistance.getStringValue() == null )
    		throw new InvalidSettingsException( "Must provide a buffer distance value");
    	
    	try{
    		Double.parseDouble(bufferDistance.getStringValue());
    	}
    	catch (NumberFormatException e)
    	{
    		throw new NumberFormatException("Buffer distance must be a double value");
    	}
    	
    	String columNames[] = inSpecs[0].getColumnNames();
    	if (!Arrays.asList(columNames).contains(Constants.GEOM)){
			throw new InvalidSettingsException( "Input table must contains a geometry column (the_geom)");
		}
    	
    	return new DataTableSpec[] { createSpec(inSpecs[0]) };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	bufferDistance.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	bufferDistance.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	bufferDistance.validateSettings(settings);
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
    
    private static DataTableSpec createSpec(DataTableSpec inSpec) throws InvalidSettingsException {
		
		List<DataColumnSpec> columns = new ArrayList<>();

		for (DataColumnSpec column : inSpec) {
			columns.add(column);
		}
		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}

}

