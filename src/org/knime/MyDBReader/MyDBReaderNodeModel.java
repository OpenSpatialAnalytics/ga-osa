package org.knime.MyDBReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.sql.SQLException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;


import org.apache.commons.lang.exception.ExceptionUtils;
import org.knime.base.util.flowvariable.FlowVariableProvider;
import org.knime.base.util.flowvariable.FlowVariableResolver;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabaseConnectionPortObject;
import org.knime.core.node.port.database.DatabaseConnectionPortObjectSpec;
import org.knime.core.node.port.database.DatabaseConnectionSettings;
import org.knime.core.node.port.database.DatabaseQueryConnectionSettings;
import org.knime.core.node.port.database.reader.DBReader;
import org.knime.core.node.streamable.StreamableOperatorInternals;
import org.knime.core.node.workflow.CredentialsProvider;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of MyDBReader.
 * This is a Database Reader, TG.
 *
 * @author TG
 */
//public class MyDBReaderNodeModel extends NodeModel {
public class MyDBReaderNodeModel extends NodeModel implements FlowVariableProvider {
    
    // the logger instance
   /* private static final NodeLogger logger = NodeLogger
            .getLogger(MyDBReaderNodeModel.class);*/
    private static final NodeLogger LOGGER = NodeLogger.getLogger("MyDBReader");
    
    protected final DatabaseQueryConnectionSettings m_settings = new DatabaseQueryConnectionSettings();

    private DataTableSpec m_lastSpec = null;
    DatabaseConnectionPortObjectSpec m_OutConnSpec=null;//tg add this line
    DatabaseConnectionPortObject m_OutConnObject =null; //tg add this line
    
    /* Flag that is true if the node is just about to be executed in streaming mode.
     * It is set true in the computeFinalOutputSpecs-method to guarantee that the
     * configure-method returns a non-null data table spec
     * (i.e. it annuls the m_settings..getValidateQuery() for a moment).*/
    private boolean m_isInStreamingExecution = false;
    
    /**
     * Creates a new model with the given number (and types!) of input and output types.
     *
     * @param inPortTypes an array of non-null in-port types
     * @param outPortTypes an array of non-null out-port types
     */
    protected MyDBReaderNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes) {
        super(inPortTypes, outPortTypes);
    }
    
    /**
     * Creates a new database reader with one data out-port.
     * @param ins number data input ports
     * @param outs number data output ports
     */
    MyDBReaderNodeModel(final int ins, final int outs) {
        super(ins, outs);
    }
    
   
    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) 
            throws CanceledExecutionException,Exception {

        // TODO do something here
    	
    	 exec.setProgress("Opening database connection...");
         try {
             exec.setProgress("Reading data from database...");
             DBReader load = loadConnectionSettings(inData[getNrInPorts()-1]);
             final BufferedDataTable result = getResultTable(exec, inData, load);
             setLastSpec(result.getDataTableSpec());
             //TG S
             return new PortObject[] {result,m_OutConnObject};  //if the inData[] is NUll,it have errors,need fit it
            
             // return new PortObject[] {inData[getNrInPorts()-1],result};
             //TG E
            // return new BufferedDataTable[]{result};
         } catch (CanceledExecutionException cee) {
             throw cee;
         } catch (Exception e) {
             setLastSpec(null);
             throw e;
         }
    }
    
    /**
     * @param exec {@link ExecutionContext} to create the table
     * @param inData
     * @param load
     * @return the result table for the
     * @throws CanceledExecutionException
     * @throws SQLException
     * @throws InvalidSettingsException
     */
    protected BufferedDataTable getResultTable(final ExecutionContext exec, final PortObject[] inData, final DBReader load)
        throws CanceledExecutionException, SQLException, InvalidSettingsException {
        CredentialsProvider cp = getCredentialsProvider();
        final BufferedDataTable result = load.createTable(exec, cp);
        return result;
    }
    
    /**
     * @param dbPortObject
     * @return
     * @throws InvalidSettingsException
     */
    protected DBReader loadConnectionSettings(final PortObject dbPortObject) throws InvalidSettingsException,Exception{
        String query = parseQuery(m_settings.getQuery());
        DatabaseQueryConnectionSettings connSettings;
        if ((dbPortObject instanceof DatabaseConnectionPortObject)) {
            DatabaseConnectionPortObject dbObj = (DatabaseConnectionPortObject)dbPortObject;
            connSettings =
                new DatabaseQueryConnectionSettings(dbObj.getConnectionSettings(getCredentialsProvider()), query);
            m_OutConnObject = dbObj;//tg add 
        } else {
            connSettings = new DatabaseQueryConnectionSettings(m_settings, query);
            //TG ,this output DatabaseconnObject
            DatabaseConnectionSettings s = new DatabaseConnectionSettings(m_settings);
            s.setRowIdsStartWithZero(true); // new behavior since 2.10
            m_OutConnObject = new DatabaseConnectionPortObject(new DatabaseConnectionPortObjectSpec(s));
            try {
            	m_OutConnObject.getConnectionSettings(getCredentialsProvider()).createConnection(getCredentialsProvider());
            } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidSettingsException
                    | SQLException | IOException ex) {
                Throwable cause = ExceptionUtils.getRootCause(ex);
                if (cause == null) {
                    cause = ex;
                }
                throw new SQLException("Could not create connection to database: " + cause.getMessage(), ex);
            }   
            //TG
        }
        final DBReader load =
                connSettings.getUtility().getReader(new DatabaseQueryConnectionSettings(connSettings, query));
        return load;
    }

   
    /** (non-Javadoc)
	 * @see org.knime.core.node.NodeModel#computeFinalOutputSpecs(org.knime.core.node.streamable.StreamableOperatorInternals, org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public PortObjectSpec[] computeFinalOutputSpecs(final StreamableOperatorInternals internals, final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		// TODO Auto-generated method stub
		//return super.computeFinalOutputSpecs(internals, inSpecs);
		m_isInStreamingExecution = true;
        try {
            return super.computeFinalOutputSpecs(internals, inSpecs);
        } finally {
            m_isInStreamingExecution = false;
        }
	}

	 protected String parseQuery(final String query) {
	        return FlowVariableResolver.parse(query, this);
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
    //   System.out.println("configure Begin");
    	
    	final DataTableSpec lastSpec = getLastSpec();
        if (lastSpec != null) {
          //  return new DataTableSpec[]{lastSpec};
           // return new DataTableSpec[]{lastSpec,inSpecs[0]};
            return new PortObjectSpec[]{lastSpec,m_OutConnSpec};//tg
        }
        try {
        	
            if ((m_settings.getQuery() == null) || m_settings.getQuery().isEmpty()) {
                throw new InvalidSettingsException("No query configured.");
            }
            
            if (!m_isInStreamingExecution && !m_settings.getValidateQuery()) {
               // return new DataTableSpec[] {null};  
            	
                return new PortObjectSpec[]{lastSpec,m_OutConnSpec};//tg,not sure
            }
          
            DataTableSpec resultSpec = getResultSpec(inSpecs);
            setLastSpec(resultSpec);
           // return new DataTableSpec[]{resultSpec};
            return new PortObjectSpec[]{lastSpec,m_OutConnSpec}; //tg
        } catch (InvalidSettingsException e) {
            setLastSpec(null);
            throw e;
        } catch (SQLException ex) {
            setLastSpec(null);
            Throwable cause = ExceptionUtils.getRootCause(ex);
            if (cause == null) {
                cause = ex;
            }

            throw new InvalidSettingsException("Could not determine table spec from database query: "
                + cause.getMessage(), ex);
        }
       
    }

    private boolean hasDatabaseInputConnection(final PortObjectSpec[] inSpecs) {
        return (inSpecs.length > getNrInPorts() - 1)
                && (inSpecs[getNrInPorts() - 1] instanceof DatabaseConnectionPortObjectSpec);
    }
    
    /**
     * @param inSpecs input spec
     * @return the {@link DataTableSpec} of the result table
     * @throws InvalidSettingsException if the connection settings are invalid
     * @throws SQLException if the query is invalid
     */
    protected DataTableSpec getResultSpec(final PortObjectSpec[] inSpecs)
        throws InvalidSettingsException, SQLException {
        String query = parseQuery(m_settings.getQuery());
        DatabaseQueryConnectionSettings connSettings;
        if (hasDatabaseInputConnection(inSpecs)) {
            DatabaseConnectionPortObjectSpec connSpec =
                (DatabaseConnectionPortObjectSpec)inSpecs[getNrInPorts() - 1];
            m_OutConnSpec = connSpec; //tg add 
            connSettings = new DatabaseQueryConnectionSettings(
                connSpec.getConnectionSettings(getCredentialsProvider()), query);
        } else {
            connSettings = new DatabaseQueryConnectionSettings(m_settings, query);
            //TG
            m_OutConnSpec = new DatabaseConnectionPortObjectSpec(m_settings);//tg
            //TG
        }
        DBReader reader = connSettings.getUtility().getReader(connSettings);
        final DataTableSpec resultSpec = reader.getDataTableSpec(getCredentialsProvider());
        return resultSpec;
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
     * @param newQuery the new query to set
     */
    final void setQuery(final String newQuery) {
        m_settings.setQuery(newQuery);
    }

    /**
     * @return current query
     */
    final String getQuery() {
        return m_settings.getQuery();
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
        
    	 boolean settingsChanged = m_settings.loadValidatedConnection(settings, getCredentialsProvider());

         if (settingsChanged || (m_settings.getQuery() == null) || m_settings.getQuery().isEmpty()) {
             setLastSpec(null);
         }

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

    	 String query = settings.getString(
                 DatabaseConnectionSettings.CFG_STATEMENT);
         if (query != null && query.contains(
                 DatabaseQueryConnectionSettings.TABLE_PLACEHOLDER)) {
             throw new InvalidSettingsException(
                     "Database table place holder ("
                     + DatabaseQueryConnectionSettings.TABLE_PLACEHOLDER
                     + ") not replaced.");
         }

         DatabaseQueryConnectionSettings s = new DatabaseQueryConnectionSettings();
         s.validateConnection(settings, getCredentialsProvider());

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
  /*  protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {*/
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException {
        
        // TODO load internal data. 
        // Everything handed to output ports is loaded automatically (data
        // returned by the execute method, models loaded in loadModelContent,
        // and user settings set through loadSettingsFrom - is all taken care 
        // of). Load here only the other internals that need to be restored
        // (e.g. data used by the views).
    	
    	File specFile = null;
        specFile = new File(nodeInternDir, "spec.xml");
        if (!specFile.exists()) {
            IOException ioe = new IOException("Spec file (\""
                    + specFile.getAbsolutePath() + "\") does not exist "
                    + "(node may have been saved by an older version!)");
            throw ioe;
        }
        NodeSettingsRO specSett =
            NodeSettings.loadFromXML(new FileInputStream(specFile));
        try {
            setLastSpec(DataTableSpec.load(specSett));
        } catch (InvalidSettingsException ise) {
            IOException ioe = new IOException("Could not read output spec.");
            ioe.initCause(ise);
            throw ioe;
        }

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
   /* protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {*/
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException {
    	 // TODO save internal models. 
        // Everything written to output ports is saved automatically (data
        // returned by the execute method, models saved in the saveModelContent,
        // and user settings saved through saveSettingsTo - is all taken care 
        // of). Save here only the other internals that need to be preserved
        // (e.g. data used by the views).
        final DataTableSpec lastSpec = getLastSpec();
        assert (lastSpec != null) : "Spec must not be null!";
        NodeSettings specSett = new NodeSettings("spec.xml");
        lastSpec.save(specSett);
        File specFile = new File(nodeInternDir, "spec.xml");
        specSett.saveToXML(new FileOutputStream(specFile));    
       

    }
    /**
     * @return the cached result spec
     */
    protected DataTableSpec getLastSpec() {
        return m_lastSpec;
    }
    
    /**
     * @param spec the {@link DataTableSpec} of the result table to cache
     */
    protected void setLastSpec(final DataTableSpec spec) {
        m_lastSpec = spec;
    }
    

}

