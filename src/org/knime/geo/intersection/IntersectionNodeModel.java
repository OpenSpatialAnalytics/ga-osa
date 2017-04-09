package org.knime.geo.intersection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
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
import org.knime.geoutils.Constants;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;


/**
 * This is the model implementation of Intersection.
 * 
 *
 * @author 
 */
public class IntersectionNodeModel extends NodeModel {
	
	private static final NodeLogger logger = NodeLogger.getLogger(IntersectionNodeModel.class);
	private boolean needTransform = false;
	private MathTransform transform = null;
	
    
    /**
     * Constructor for the node model.
     */
    protected IntersectionNodeModel() {
    
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

    	BufferedDataTable inTable = inData[0];
    	String columNames[] = inTable.getSpec().getColumnNames();
    	int numColumns = inTable.getSpec().getNumColumns();
    	int geomIndexs[] = new int[2];
    	long tableSize = inTable.size();
    	
    	/*
    	 * find 2 index of geometry column 
    	 */
    	int j = 0;
    	for (int i = 0; i < numColumns; i++) {
    		if (columNames[i].contains(Constants.GEOM) ){
    			geomIndexs[j] = i;
    			j++;
    			if (j==2)
    				break;
    		}
    	}
    	
    	DataTableSpec outSpec = createSpec(inTable.getSpec(), geomIndexs);
    	BufferedDataContainer container = exec.createDataContainer(outSpec);
    	
    	DataRow firstRow =  inTable.iterator().next();
    	String featureStr1 = ((StringValue) firstRow.getCell(geomIndexs[0])).getStringValue();
    	String featureStr2 = ((StringValue) firstRow.getCell(geomIndexs[1])).getStringValue();
    	
    	String crsJSON = Constants.GetCRS(featureStr1);
    	String crsStr1 = Constants.GetCRSCode(crsJSON);
    	String crsStr2 = Constants.GetCRSCode(Constants.GetCRS(featureStr2));
    	
    	if (crsStr1.compareTo(crsStr2) != 0){
    		transform = Constants.FindMathTransform(crsStr1, crsStr2);
    		needTransform = true;
    	}
    	
    	int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService es = Executors.newFixedThreadPool(threads);
		List<Future<DataCell []>> futures = new ArrayList<Future<DataCell []>>();
		
		int i = 1;
    	for (DataRow r : inTable) {
    		  Callable<DataCell []> callable = new Callable<DataCell []>() {
    			  public DataCell [] call() throws Exception {
    				  	DataCell[] cells = new DataCell[outSpec.getNumColumns()];
    				  	try{
    				  		DataCell geometryCell1 = r.getCell(geomIndexs[0]);
    			    		DataCell geometryCell2 = r.getCell(geomIndexs[1]);
    			    		String geoJsonString1 = ((StringValue) geometryCell1).getStringValue();	    			
    		    			Geometry geo1 =  Constants.FeatureToGeometry(geoJsonString1);
    		    			String geoJsonString2 = ((StringValue) geometryCell2).getStringValue();	    			
    		    			Geometry geo2 =  Constants.FeatureToGeometry(geoJsonString2);	  
    		    			if (needTransform)
    		    				geo2 = JTS.transform(geo2, transform);
    		    			Geometry geo = geo1.intersection(geo2);
    	    				String str = Constants.GeometryToGeoJSON(geo, crsJSON);
    	    				cells[geomIndexs[0]] = new StringCell(str);
    	    				int k = 0;
    	    				for ( int col = 0; col < numColumns; col++ ) {	
    	    					if (col == geomIndexs[0]) {
    	    	    				k++;
    	    					}else if(col == geomIndexs[1]){
    	    						
    	    					}else{
    	    						cells[k] = r.getCell(col);
    	    	    				k++;
    	    					}
    	    	    		}
    				  	}
    				  	catch(Exception e){
    				  		e.printStackTrace();
    				  		logger.error("Something wrong in Intersection operation at row: "+ r.getKey());
    				  	}
  	    				return cells;
    			  }
    		  };
    		  futures.add(es.submit(callable));
    		  exec.checkCanceled();
    		  exec.setProgress( 0.9 * ((double) i / (double)tableSize ), "Creating Row " + i);
    		  i++;
    	}
    	
    	es.shutdown();
		
		try {
			while(!es.awaitTermination(24, TimeUnit.HOURS));
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Intersection operation could not be completed.");
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
    	
    	int j = 0;
    	int numColumns = inSpecs[0].getNumColumns();
    	String columNames[] = inSpecs[0].getColumnNames();
    	for (int i = 0; i < numColumns; i++) {
    		if (columNames[i].contains(Constants.GEOM) ){
    			j++;
    			if (j==2)
    				break;
    		}
    	}
    	
    	if ( j < 2 )
    		throw new InvalidSettingsException( "Input table must contain 2 geometry columns "
                    + "and those column headers must start with the_geom");

        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	
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
    
    private static DataTableSpec createSpec(DataTableSpec inSpec, int geomIndexs[]) throws InvalidSettingsException {
		
		List<DataColumnSpec> columns = new ArrayList<>();

		int k = 0;
		for (DataColumnSpec column : inSpec) {
			if (k != geomIndexs[1]){
				columns.add(column);
			}
			k++;
		}
		
		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}

}

