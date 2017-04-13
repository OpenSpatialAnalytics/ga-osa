package org.knime.geo.linetopoly;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geotools.geometry.jts.Geometries;
import org.geotools.geometry.jts.JTSFactoryFinder;
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
import org.knime.geoutils.Constants;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * This is the model implementation of LineToPolygon.
 * 
 *
 * @author Forkan
 */
public class LineToPolygonNodeModel extends NodeModel {
    
    /**
     * Constructor for the node model.
     */
    protected LineToPolygonNodeModel() {
    
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
    	
    	DataTableSpec outSpec = createSpec(inTable.getSpec());
    	BufferedDataContainer container = exec.createDataContainer(outSpec);
    	
    	RowIterator ri = inTable.iterator();
    	    	        	    	    	    	    	
    	try{    	
	    	for (int i = 0; i < inTable.size(); i++ ) {
	    		
	    		DataRow r = ri.next();				    		
	    		DataCell geometryCell = r.getCell(geomIndex);
	    			    		
	    		if ( (geometryCell instanceof StringValue) ){
	    			String geoJsonString = ((StringValue) geometryCell).getStringValue();	    			
	    			Geometry g = Constants.FeatureToGeometry(geoJsonString);
	    			String crs = Constants.GetCRS(geoJsonString);
	    			Geometries geomType = Geometries.get(g);		
	    			GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();
	    			LinearRing ring = null;
	    			Polygon polygon = null;
	    			
	    			if (geomType == Geometries.LINESTRING){
	    				LineString lineString = (LineString)g;
	    				if( lineString.isClosed() ){
	 	    			   ring = factory.createLinearRing( lineString.getCoordinateSequence() );
	    				}
	 	    			else {
	 	    			   CoordinateSequence sequence = lineString.getCoordinateSequence();
	 	    			   Coordinate array[] = new Coordinate[ sequence.size() + 1 ];
	 	    			   for( int j=0; j<sequence.size();j++){
	 	    				   array[j] = sequence.getCoordinate(j);
	 	    				   array[array.length-1] = sequence.getCoordinate(0);
	 	    				   ring = factory.createLinearRing( array );
	 	    			   }
	 	    			}
	 	    			polygon = factory.createPolygon( ring, null );
	    			}
	    			else{
	    				throw new Exception( "Error in row " + i + "Geometry must be of a linestring");
	    			}
	    		
	    			
    				String str = Constants.GeometryToGeoJSON(polygon, crs);
  
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

    	String columNames[] = inSpecs[0].getColumnNames();
    	if (!Arrays.asList(columNames).contains(Constants.GEOM)){
			throw new InvalidSettingsException( "Input table must contain a geometry column (the_geom)");
		}
        return new DataTableSpec[]{null};
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
