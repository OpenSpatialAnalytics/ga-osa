package org.knime.MyPortDBConn;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.sql.SQLException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.lang.exception.ExceptionUtils;
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
import org.knime.core.node.port.database.DatabaseConnectionSettings;
import org.knime.mynode.myport.MyDatabaseConnPortObject;
import org.knime.mynode.myport.MyDatabaseConnPortObjectSpec;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of MyPortDBConn.
 * This is a Database Connection using my DatabaseConnPort, TG.
 *
 * @author TG
 */
public class MyPortDBConnNodeModel extends NodeModel {
    
	 private DatabaseConnectionSettings m_settings = new DatabaseConnectionSettings();
	    

	    /**
	     * Constructor for the node model.
	     */
	    protected MyPortDBConnNodeModel() {
	    
	        // TODO one incoming port and one outgoing port is assumed
	      //  super(1, 1);
	        super(new PortType[0], new PortType[]{MyDatabaseConnPortObject.TYPE});
	    }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {

	        // TODO do something here
	    	 DatabaseConnectionSettings s = new DatabaseConnectionSettings(m_settings);
	         s.setRowIdsStartWithZero(true); // new behavior since 2.10


	         MyDatabaseConnPortObject dbPort =
	                 new MyDatabaseConnPortObject(new MyDatabaseConnPortObjectSpec(s));
	         try {
	             dbPort.getConnectionSettings(getCredentialsProvider()).createConnection(getCredentialsProvider());
	         } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidSettingsException
	                 | SQLException | IOException ex) {
	             Throwable cause = ExceptionUtils.getRootCause(ex);
	             if (cause == null) {
	                 cause = ex;
	             }

	             throw new SQLException("Could not create connection to database: " + cause.getMessage(), ex);
	         }

	         return new PortObject[]{dbPort};
	     }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    protected void reset() {
	        // TODO Code executed on reset.
	        // Models build during execute are cleared here.
	        // Also data handled in load/saveInternals will be erased here.
	    }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
	            throws InvalidSettingsException {
	        
	        // TODO: check if user settings are available, fit to the incoming
	        // table structure, and the incoming types are feasible for the node
	        // to execute. If the node can execute in its current state return
	        // the spec of its output data table(s) (if you can, otherwise an array
	        // with null elements), or throw an exception with a useful user message

	    	if ((m_settings.getDriver() == null) || m_settings.getDriver().isEmpty()) {
	            throw new InvalidSettingsException("No JDBC driver selected");
	        }

	        if ((m_settings.getJDBCUrl() == null) || m_settings.getJDBCUrl().isEmpty()) {
	            throw new InvalidSettingsException("No database URL provided");
	        }

	        MyDatabaseConnPortObjectSpec spec = new MyDatabaseConnPortObjectSpec(m_settings);

	        return new PortObjectSpec[]{spec};
	    }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    protected void saveSettingsTo(final NodeSettingsWO settings) {

	        // TODO save user settings to the config object.
	    	m_settings.saveConnection(settings);
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
	        
	    	 m_settings.loadValidatedConnection(settings, getCredentialsProvider());

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

	    	 DatabaseConnectionSettings s = new DatabaseConnectionSettings();
	         s.validateConnection(settings, getCredentialsProvider());
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

}

