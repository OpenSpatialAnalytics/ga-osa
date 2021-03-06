package org.knime.geo.transform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.factory.Hints;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
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
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * This is the model implementation of Transform.
 * 
 *
 * @author 
 */
public class TransformNodeModel extends NodeModel {
    
	 //static final String SRC_SRS = "src_srid";
	 static final String DEST_SRS = "dest_srid";
	 //public final SettingsModelString srcSRID = new SettingsModelString(SRC_SRS,"");
	 public final SettingsModelString destSRID = new SettingsModelString(DEST_SRS,"");
	
	
    /**
     * Constructor for the node model.
     */
    protected TransformNodeModel() {
    
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
    	
    	//CoordinateReferenceSystem targetCRS = factory.createCoordinateReferenceSystem("urn:y-ogc:def:crs:EPSG:"+destSRID.getStringValue());
    	CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:"+destSRID.getStringValue());
 
    	
    	Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
    	CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", hints);
    	
    	if (CRS.getAxisOrder(targetCRS) == CRS.AxisOrder.NORTH_EAST || CRS.getAxisOrder(targetCRS) == CRS.AxisOrder.LAT_LON){
    		targetCRS = factory.createCoordinateReferenceSystem("EPSG:"+destSRID.getStringValue());    		
    	}
    	    	   	
    	String targetCrsJSON = Constants.GetCrsJson(destSRID.getStringValue());
        	    	    	    	    	
    	try{    	
	    	for (int i = 0; i < inTable.size(); i++ ) {
	    		
	    		DataRow r = ri.next();				    		
	    		DataCell geometryCell = r.getCell(geomIndex);
	    		
	    		if ( (geometryCell instanceof StringValue) ){
	    			String geoJsonString = ((StringValue) geometryCell).getStringValue();	    			
	    			Geometry g = Constants.FeatureToGeometry(geoJsonString);
	    			String scrCrsCode = Constants.GetCRSCode(Constants.GetCRS(geoJsonString));
	    			CoordinateReferenceSystem srcCRS = CRS.decode(scrCrsCode);	    			
    				if (CRS.getAxisOrder(srcCRS) == CRS.AxisOrder.NORTH_EAST || CRS.getAxisOrder(srcCRS) == CRS.AxisOrder.LAT_LON){
    					srcCRS = factory.createCoordinateReferenceSystem(scrCrsCode);
    				}	       				
	    			MathTransform transform = CRS.findMathTransform(srcCRS, targetCRS, true);	    			
	    			Geometry geo = JTS.transform(g, transform);				    			
    				String str = Constants.GeometryToGeoJSON(geo, targetCrsJSON);
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

    	if (destSRID.getStringValue() == null) {
			throw new InvalidSettingsException("You must have a destination srid number for projection");
		}

        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	destSRID.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	destSRID.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	destSRID.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
      
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
       
    }
    
    private static DataTableSpec createSpec(DataTableSpec inSpec) throws InvalidSettingsException {
		
		List<DataColumnSpec> columns = new ArrayList<>();

		for (DataColumnSpec column : inSpec) {
			columns.add(column);
		}
		
		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}

}

