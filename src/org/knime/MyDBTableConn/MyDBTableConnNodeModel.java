package org.knime.MyDBTableConn;

import java.io.File;
import java.io.IOException;

import org.knime.base.node.io.database.DBNodeModel;
import org.knime.base.util.flowvariable.FlowVariableProvider;
import org.knime.base.util.flowvariable.FlowVariableResolver;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.core.node.port.database.DatabasePortObjectSpec;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.port.database.reader.DBReader;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of MyDBTableConn.
 * This is a Database Table Connection Test.TG
 *
 * @author TG
 */
//public class MyDBTableConnNodeModel extends NodeModel {
final class MyDBTableConnNodeModel extends DBNodeModel implements FlowVariableProvider {
	 private DatabaseQueryConnectionSettings m_conn = new DatabaseQueryConnectionSettings();  
    /**
     * Constructor for the node model.
     */
    protected MyDBTableConnNodeModel() {
    
        // TODO one incoming port and one outgoing port is assumed
    //    super(1, 1);
        super(new PortType[0], new PortType[]{DatabasePortObject.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, 
			final ExecutionContext exec) 
			throws CanceledExecutionException, Exception {
    	// TODO Auto-generated method stub
		DatabaseQueryConnectionSettings conn = new DatabaseQueryConnectionSettings(m_conn,
				parseQuery(m_conn.getQuery()));
		DBReader load = conn.getUtility().getReader(conn);
		DataTableSpec spec = load.getDataTableSpec(getCredentialsProvider());
		DatabasePortObject dbObj = new DatabasePortObject(
				new DatabasePortObjectSpec(spec, conn.createConnectionModel()));
		return new PortObject[] { dbObj };      			
    }

  /*  *//**
     * {@inheritDoc}
     *//*
    @Override
    protected void reset() {
        // TODO Code executed on reset.
        // Models build during execute are cleared here.
        // Also data handled in load/saveInternals will be erased here.
    }*/

    /**
     * {@inheritDoc}
     */
    @Override
//    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
//            throws InvalidSettingsException {
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) 
			throws InvalidSettingsException {
        
        // TODO: check if user settings are available, fit to the incoming
        // table structure, and the incoming types are feasible for the node
        // to execute. If the node can execute in its current state return
        // the spec of its output data table(s) (if you can, otherwise an array
        // with null elements), or throw an exception with a useful user message
    //	System.out.println("configure");
    	
    	 try {
	            // try to create database connection
//	            DatabaseQueryConnectionSettings conn = m_load.getQueryConnection();
	            if (m_conn == null) {
	                throw new InvalidSettingsException("No database connection available.");
	            }
	            final DatabaseQueryConnectionSettings conn =
	                    new DatabaseQueryConnectionSettings(m_conn, parseQuery(m_conn.getQuery()));

	            if (!conn.getRetrieveMetadataInConfigure()) {
	                return new PortObjectSpec[1];
	            }

	            DBReader load = conn.getUtility().getReader(conn);
	            DataTableSpec spec = load.getDataTableSpec(getCredentialsProvider());
	            if (spec == null) {
	                throw new InvalidSettingsException("No database connection available.");
	            }
	            DatabasePortObjectSpec dbSpec = new DatabasePortObjectSpec(spec, conn.createConnectionModel());
	            return new PortObjectSpec[]{dbSpec};
	        } catch (InvalidSettingsException ise) {
	            throw ise;
	        } catch (Throwable t) {
	            throw new InvalidSettingsException(t);
	        }
		//return super.configure(inSpecs);
    }
    
    private String parseQuery(String query) {
		// TODO Auto-generated method stub
		return FlowVariableResolver.parse(query, this);
		//return null;
	}
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

        // TODO save user settings to the config object.
        
    	 if (m_conn != null) {
             m_conn.saveConnection(settings);
         }	

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
        
    	 super.loadValidatedSettingsFrom(settings);
         m_conn =
             new DatabaseQueryConnectionSettings(settings,
                 getCredentialsProvider());	

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

    	 super.validateSettings(settings);
         new DatabaseQueryConnectionSettings(settings, getCredentialsProvider());	

    }
    
   /* *//**
     * {@inheritDoc}
     *//*
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
    
    *//**
     * {@inheritDoc}
     *//*
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

    }*/

}

