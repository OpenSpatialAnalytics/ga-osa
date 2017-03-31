package org.knime.geo.concave;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.Geometries;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
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
import org.opensphere.geometry.algorithm.ConcaveHull;

import com.vividsolutions.jts.geom.Geometry;

/**
 * This is the model implementation of ConcaveHull.
 * 
 *
 * @author Forkan
 */
public class ConcaveHullNodeModel extends NodeModel {
	
	
	static final String TP = "target_percent";
	public final SettingsModelString targetPercent = new SettingsModelString(TP, "0.0");
	private double target_percent = 0.0;
    
    /**
     * Constructor for the node model.
     */
    protected ConcaveHullNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {

    	BufferedDataTable inTable = inData[0];
    	int geomIndex = inTable.getSpec().findColumnIndex(Constants.GEOM);
    	
    	try{
    		target_percent = Double.parseDouble(targetPercent.getStringValue());      		
    	}
    	catch (NumberFormatException e)
    	{
    		throw new NumberFormatException("Target percent value must be a double value between 0.0 to 1.0");
    	}
    	
    	DataTableSpec outSpec = createSpec(inTable.getSpec());
    	BufferedDataContainer container = exec.createDataContainer(outSpec);
    	
    	RowIterator ri = inTable.iterator();    	
        	    	    	    	    	
    	try{    	
	    	for (int i = 0; i < inTable.size(); i++ ) {
	    		
	    		DataRow r = ri.next();				    		
	    		DataCell geometryCell = r.getCell(geomIndex);
	    		String str = "";
	    		
	    		if ( (geometryCell instanceof StringValue) ){
	    			String geoJsonString = ((StringValue) geometryCell).getStringValue();	    			
	    			Geometry g = new GeometryJSON().read(geoJsonString);
	    			Geometries geomType = Geometries.get(g);
	    			if (geomType == Geometries.GEOMETRYCOLLECTION){
	    				ConcaveHull ch = new ConcaveHull(g, target_percent);
	    				Geometry geo = ch.getConcaveHull();
	    				GeometryJSON json = new GeometryJSON(Constants.JsonPrecision);
	    				str = json.toString(geo);
	    			}
	    			
	    			//Geometry geo = g.convexHull();
	    			//Transform<Geometry, Geometry> algorithm = new SnapHull();
	    			//Transform<Geometry, Geometry> algorithm = new ConcaveHull(target_percent);
	    			//Geometry geo = algorithm.transform(g);
	    			//GeometryJSON json = new GeometryJSON(Constants.JsonPrecision);
    				//String str = json.toString(geo);
	    		
    				DataCell[] cells = new DataCell[outSpec.getNumColumns()];
    				cells[geomIndex] = new StringCell(str);
    					
					for ( int col = 0; col < inTable.getSpec().getNumColumns(); col++ ) {	
						if (col != geomIndex) {
							cells[col] = r.getCell(col);
						}
		    		}
					
					container.addRowToTable(new DefaultRow("Row"+i, cells));
		    		exec.checkCanceled();
					exec.setProgress((double) i / (double) inTable.size());  					
	    		}
	    				    				    				    			    			    			    			    		    							
	    	}
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    		
    	}
    	container.close();
    	return new BufferedDataTable[] { container.getTable() };
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

    	if (targetPercent.getStringValue() == null )
    		throw new InvalidSettingsException( "Must provide a target percent value for producing concave hull");
    	
    	double d = -0.1;
    	try{
    		d = Double.parseDouble(targetPercent.getStringValue());
    	}
    	catch (NumberFormatException e)
    	{
    		throw new NumberFormatException("Target percent value must be a double value between 0.0 to 1.0");
    	}
    	
    	String columNames[] = inSpecs[0].getColumnNames();
    	if (!Arrays.asList(columNames).contains(Constants.GEOM)){
			throw new InvalidSettingsException( "Input table must contains a geometry column (the_geom)");
		}
    	
    	if (!(d >= 0.0 && d <= 1.0))
    		throw new InvalidSettingsException( "Target percent value must be a double value between 0.0 to 1.0");
    	
    	return new DataTableSpec[] { createSpec(inSpecs[0]) };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
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

