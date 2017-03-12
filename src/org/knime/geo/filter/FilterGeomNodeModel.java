package org.knime.geo.filter;

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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import jdk.nashorn.internal.runtime.regexp.joni.ast.ConsAltNode;

/**
 * This is the model implementation of FilterGeom.
 * 
 *
 * @author 
 */
public class FilterGeomNodeModel extends NodeModel {
    
    /**
     * Constructor for the node model.
     */
	
	static final String GT = "geom_type";
	public final SettingsModelString geomType = new SettingsModelString(GT,Geometries.POLYGON.toString());
	
    protected FilterGeomNodeModel() {
    
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
    	DataTableSpec outSpec = createSpec(inTable.getSpec());
    	BufferedDataContainer container = exec.createDataContainer(outSpec);
    	
    	int geomIndex = inTable.getSpec().findColumnIndex(Constants.GEOM);	
    	int numberOfColumns = inTable.getSpec().getNumColumns();
    	
    	int index = 0;
    	for (DataRow row : inTable) {	    		   		
    		DataCell geometryCell = row.getCell(geomIndex);
    		if (geometryCell instanceof StringValue){
    			String geoJsonString = ((StringValue) geometryCell).getStringValue();
    			Geometry geo = new GeometryJSON().read(geoJsonString);
    			Geometries gType = Geometries.get(geo);
    			if (gType.toString().equals(geomType.getStringValue())){
    				GeometryJSON json = new GeometryJSON(Constants.JsonPrecision);
					String str = json.toString(geo);
					DataCell[] cells = new DataCell[outSpec.getNumColumns()];	  
					cells[geomIndex] = new StringCell(str);
					for ( int col = 0; col < numberOfColumns; col++ ) {	
						if (col != geomIndex ) {
		    				cells[col] = row.getCell(col);
						}
		    		}
					container.addRowToTable(new DefaultRow("Row"+index, cells));
					exec.checkCanceled();
					exec.setProgress((double) index / (double) inTable.size());
					index++;
    			}
    			else{
    				if ( gType.toString().contains("Multi") ){
    					if ( gType.toString().equals("Multi"+geomType.getStringValue())){
    						GeometryCollection  gc = (GeometryCollection)geo;
    	    				for (int i = 0; i < gc.getNumGeometries(); i++ ){
    	    					Geometry g = (Geometry) gc.getGeometryN(i);	    					
    	    					GeometryJSON json = new GeometryJSON(Constants.JsonPrecision);
    	    					String str = json.toString(g);
    	    					DataCell[] cells = new DataCell[outSpec.getNumColumns()];	
    	    					cells[geomIndex] = new StringCell(str);
    	    					for ( int col = 0; col < numberOfColumns; col++ ) {	
    	    						if (col != geomIndex ) {
    	    		    				cells[col] = row.getCell(col);
    	    						}
    	    		    		}
    	    					container.addRowToTable(new DefaultRow("Row"+index, cells));
    	    					exec.checkCanceled();
    	    					exec.setProgress((double) index / (double) inTable.size());
    	    					index++;
    	    				}		
    					}
    				}
    				else if (gType == Geometries.GEOMETRYCOLLECTION){
    					GeometryCollection  gc = (GeometryCollection)geo;
	    				for (int i = 0; i < gc.getNumGeometries(); i++ ){
	    					Geometry g = (Geometry) gc.getGeometryN(i);	  
	    					Geometries gTypeChild = Geometries.get(g);
	    					
	    					if (gTypeChild.toString().equals(geomType.getStringValue())){
	    						GeometryJSON json = new GeometryJSON(Constants.JsonPrecision);
		    					String str = json.toString(g);
		    					DataCell[] cells = new DataCell[outSpec.getNumColumns()];	
		    					cells[geomIndex] = new StringCell(str);
		    					for ( int col = 0; col < numberOfColumns; col++ ) {	
		    						if (col != geomIndex ) {
		    		    				cells[col] = row.getCell(col);
		    						}
		    		    		}
		    					container.addRowToTable(new DefaultRow("Row"+index, cells));
		    					exec.checkCanceled();
		    					exec.setProgress((double) index / (double) inTable.size());
		    					index++;
	    					}
	    				}
    				}
    			}
    		}
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
         this.geomType.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        this.geomType.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        this.geomType.validateSettings(settings);
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
