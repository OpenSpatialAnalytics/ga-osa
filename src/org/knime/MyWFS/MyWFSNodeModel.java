package org.knime.MyWFS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.lang.ObjectUtils.Null;
import org.geotools.data.DataStore;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.geoutils.Constants;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;

import com.vividsolutions.jts.geom.Geometry;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of MyWFS.
 * This is a test node for Web Feature Service.TG
 *
 * @author Gen Tian
 */
public class MyWFSNodeModel extends NodeModel {
        
    private static final NodeLogger logger = NodeLogger
            .getLogger(MyWFSNodeModel.class);
            
    static final String CFGKEY_WFS_URL = "wfs_url";
    static final String CFGKEY_STRSEL = "selected_source";
    private final SettingsModelString m_wfs_url= new SettingsModelString(CFGKEY_WFS_URL,Constants.localWFS);
    final SettingsModelString m_selStr = new SettingsModelString(CFGKEY_STRSEL, "");
    static WFSDataStore dataStore = null;
    static SimpleFeatureCollection featurescollec = null;
    static boolean m_blconnect =false;
    /**
     * Constructor for the node model.
     */
    protected MyWFSNodeModel() {    
        super(0, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
		 
         DataTableSpec outputSpec = null;
		 BufferedDataContainer container = null;
		  
		  try {		  
				String typeNames[] = dataStore.getTypeNames();				
				String typeName = m_selStr.getStringValue();
				
				SimpleFeatureType schema = dataStore.getSchema( typeName );				
				outputSpec = createSpec(schema)[0];
				container = exec.createDataContainer(outputSpec);
				
				SimpleFeatureSource source = dataStore.getFeatureSource(typeName);
				featurescollec = source.getFeatures();
				SimpleFeatureIterator fIterator = featurescollec.features();
				
				int index = 0;
				int count = 0;
				GeometryJSON geojson = new GeometryJSON(Constants.JsonPrecision);
				
				while(fIterator.hasNext()){
					SimpleFeature sf = fIterator.next();
					DataCell[] cells = new DataCell[outputSpec.getNumColumns()];
			      	for (Property p : sf.getProperties()) {
						int column = outputSpec.findColumnIndex(p.getName().toString());
						Object value = p.getValue();
	
						if (value == null) {
							cells[column] = DataType.getMissingCell();
						 } else if (value instanceof Geometry) {
						   cells[column] = new StringCell(geojson.toString((Geometry)(p.getValue())));						
						} else if (value instanceof Integer) {
							cells[column] = new IntCell((Integer) p.getValue());
						} else if (value instanceof Double) {
							cells[column] = new DoubleCell((Double) p.getValue());
						} else if (value instanceof Boolean) {
							cells[column] = BooleanCellFactory.create((Boolean) p.getValue());
						} else if (p.getValue().toString().isEmpty()) {
							cells[column] = DataType.getMissingCell();
						} else {
							cells[column] = new StringCell(p.getValue().toString());
						}
					}
		      
		    	container.addRowToTable(new DefaultRow(String.valueOf(index), cells));
		    	index++;
		    	exec.checkCanceled();
				exec.setProgress((double) count / (double) featurescollec.size());
				count++;		    			     
		      }
		  } catch (IOException ex) {
		      ex.printStackTrace();
		  }	 		         
        container.close();
        BufferedDataTable out = container.getTable();
        return new BufferedDataTable[]{out};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {      
    	if (m_blconnect) {
    		if (m_selStr.getStringValue() != null || m_selStr.getStringValue() != "")
    			m_blconnect = false;
		}
    	else {
    		m_blconnect = true;
		}
		
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
                
         if (!m_blconnect) {
        	 throw new InvalidSettingsException("The WFS not connected! "); 
         }
         return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
   
          m_wfs_url.saveSettingsTo(settings);
          m_selStr.saveSettingsTo(settings);
                    
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	
    	m_wfs_url.loadSettingsFrom(settings);
    	m_selStr.loadSettingsFrom(settings);
    	    	

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
                
          m_wfs_url.validateSettings(settings);
          m_selStr.validateSettings(settings);
                    
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
 /*
  * 
  */
    private static DataTableSpec[] createSpec(SimpleFeatureType type) {
		List<DataColumnSpec> columns = new ArrayList<>();

		for (AttributeType t : type.getTypes()) {		
			String name = t.getName().toString();
			
			if (t.getBinding() == Integer.class) {
				columns.add(new DataColumnSpecCreator(name, IntCell.TYPE).createSpec());
			} else if (t.getBinding() == Double.class) {
				columns.add(new DataColumnSpecCreator(name, DoubleCell.TYPE).createSpec());
			} else if (t.getBinding() == Boolean.class) {
				columns.add(new DataColumnSpecCreator(name, BooleanCell.TYPE).createSpec());
			} else {
				columns.add(new DataColumnSpecCreator(name, StringCell.TYPE).createSpec());
			}
		}
		return new DataTableSpec[] { new DataTableSpec(columns.toArray(new DataColumnSpec[0])) };
	}
}

