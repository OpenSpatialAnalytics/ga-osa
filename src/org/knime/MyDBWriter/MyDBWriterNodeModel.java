package org.knime.MyDBWriter;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.date.DateAndTimeValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabaseConnectionPortObject;
import org.knime.core.node.port.database.DatabaseConnectionPortObjectSpec;
import org.knime.core.node.port.database.DatabaseConnectionSettings;
import org.knime.core.node.port.database.writer.DBWriter;
import org.knime.core.node.port.database.writer.DBWriterImpl;
import org.knime.core.node.streamable.DataTableRowInput;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.MergeOperator;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.node.streamable.StreamableOperatorInternals;
import org.knime.core.node.streamable.simple.SimpleStreamableOperatorInternals;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of MyDBWriter.
 * This is a Database Writer supporting Postgres spatial data. GT.
 * My Database writer model which creates a new table and adds the entire table to
 * it.
 * @author Gen TIAN
 */
public class MyDBWriterNodeModel extends NodeModel {
    
    // the logger instance
  //  private static final NodeLogger logger = NodeLogger
  //          .getLogger(MyDBWriterNodeModel.class);
        
    private final DatabaseConnectionSettings m_conn = new DatabaseConnectionSettings();
    DatabaseConnectionPortObjectSpec m_OutConnSpec=null;//tg add this line
    DatabaseConnectionPortObject m_OutConnObject =null; //tg add this line
    
    /** Config key for the table name. */
    static final String KEY_TABLE_NAME = "table";
    private String m_tableName;

    /** Config key for the batch size. */
    static final String KEY_BATCH_SIZE = "batch_size";
    private int m_batchSize = DatabaseConnectionSettings.BATCH_WRITE_SIZE;

    /** Config key for the append data. */
    static final String KEY_APPEND_DATA = "append_data";
    private boolean m_append = true;
   // private boolean m_append = false;//tg

    /** Config key for the insert null for missing columns. */
    static final String KEY_INSERT_NULL_FOR_MISSING_COLS = "insert_null_for_missing_cols";
    private boolean m_insertNullForMissingCols = false;

    private final Map<String, String> m_types =
        new LinkedHashMap<String, String>();

    /** Default SQL-type for Strings. */
    static final String SQL_TYPE_STRING = "varchar(255)";

    /** Default SQL-type for Booleans. */
    static final String SQL_TYPE_BOOLEAN = "boolean";

    /** Default SQL-type for Integers. */
    static final String SQL_TYPE_INTEGER = "integer";

    /** Default SQL-type for Doubles. */
    static final String SQL_TYPE_DOUBLE = "numeric(30,10)";

    /** Default SQL-type for Timestamps. */
    static final String SQL_TYPE_DATEANDTIME = "timestamp";

    /** Default SQL-type for Date. */
    static final String SQL_TYPE_BLOB = "blob";

    /** Config key for column to SQL-type mapping. */
    static final String CFG_SQL_TYPES = "sql_types";

    /* error message during streaming execution */
    private String m_errorMessage = null;  
   
    

    /**
     * Constructor for the node model.
     */
    protected MyDBWriterNodeModel() {
    
        // TODO one incoming port and one outgoing port is assumed
      //  super(1, 1);
      //  super(new PortType[]{BufferedDataTable.TYPE, DatabaseConnectionPortObject.TYPE_OPTIONAL}, new PortType[0]);
       // super(new PortType[]{BufferedDataTable.TYPE, DatabaseConnectionPortObject.TYPE_OPTIONAL}, new PortType[0]);
        super(new PortType[]{BufferedDataTable.TYPE, DatabaseConnectionPortObject.TYPE_OPTIONAL}, 
        		new PortType[]{DatabaseConnectionPortObject.TYPE});
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
//    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
//            final ExecutionContext exec) throws Exception {
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws CanceledExecutionException,
            Exception {
        // TODO do something here
       // logger.info("Node Model Stub... this is not yet implemented !");
    	exec.setProgress("Opening database connection to write data...");
        
    	 DatabaseConnectionSettings connSettings;
         if ((inData.length > 1) && (inData[1] instanceof DatabaseConnectionPortObject)) {
             connSettings = ((DatabaseConnectionPortObject) inData[1]).getConnectionSettings(getCredentialsProvider());
             //TG S
             m_OutConnObject = (DatabaseConnectionPortObject)inData[1];//tg add 
             //TG E
         } else {
             connSettings = m_conn;
             //TG S
             //TG ,this output DatabaseconnObject
             DatabaseConnectionSettings s = new DatabaseConnectionSettings(m_conn);
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
             //TG E
         }

        // DBWriter writer = connSettings.getUtility().getWriter(connSettings);  //original code,tg comment
         //tg S
        // connSettings.getUtility();  // tg not sure this line is necessary
         DBWriter writer = new MyDBWriterImpl(connSettings); 
         //tg E
         BufferedDataTable inputTable = (BufferedDataTable)inData[0];
         DataTableRowInput rowInput = new DataTableRowInput(inputTable);
         // write entire data
         final String error = writer.writeData(m_tableName, rowInput, inputTable.size(),
             m_append, exec, m_types, getCredentialsProvider(), m_batchSize, m_insertNullForMissingCols);
         // set error message generated during writing rows
         if (error != null) {
             super.setWarningMessage(error);
         }
         //TG S
         return new PortObject[] {m_OutConnObject};  //if the inData[] is NUll, fix it,TG
         //TG E
        // return new BufferedDataTable[0];
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public InputPortRole[] getInputPortRoles() {
        return new InputPortRole[]{InputPortRole.NONDISTRIBUTED_STREAMABLE, InputPortRole.NONDISTRIBUTED_NONSTREAMABLE};
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public StreamableOperatorInternals createInitialStreamableOperatorInternals() {
        return new SimpleStreamableOperatorInternals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo,
        final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

        return new StreamableOperator() {

            @Override
            public void runFinal(final PortInput[] inputs, final PortOutput[] outputs, final ExecutionContext exec)
                throws Exception {
            	System.out.println("createStreamableOperator tg test");
                exec.setProgress("Opening database connection to write data...");

                DatabaseConnectionSettings connSettings;
                PortObject portObj = ((PortObjectInput)inputs[1]).getPortObject();
                if (portObj != null && (portObj instanceof DatabaseConnectionPortObject)) {
                    connSettings =
                        ((DatabaseConnectionPortObject)portObj).getConnectionSettings(getCredentialsProvider());
                    //TG S
                    m_OutConnObject = (DatabaseConnectionPortObject)portObj;//tg add 
                    //TG E
                } else {
                    connSettings = m_conn;
                  //TG S
                    //TG ,this output DatabaseconnObject
                    DatabaseConnectionSettings s = new DatabaseConnectionSettings(m_conn);
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
                    //TG E
                    
                }
               // DBWriter writer = connSettings.getUtility().getWriter(connSettings);
                //tg S
                // connSettings.getUtility();  // tg not sure this line is necessary
                 DBWriter writer = new MyDBWriterImpl(connSettings); 
                // write entire data
                m_errorMessage =
                    writer.writeData(m_tableName, (RowInput) inputs[0], -1,
                        m_append, exec, m_types, getCredentialsProvider(), m_batchSize, m_insertNullForMissingCols);
            }

        };
    }

    /**
     * {@inheritDoc}
     *
     * NB: needs to be overwritten to enforce the
     * {@link DBWriterNodeModel#finishStreamableExecution(StreamableOperatorInternals, ExecutionContext, PortOutput[])}
     * to be called in order to set an error message.
     */
    @Override
    public MergeOperator createMergeOperator() {
        return new MergeOperator() {

            @Override
            public StreamableOperatorInternals mergeFinal(final StreamableOperatorInternals[] operators) {
                 return operators[0];
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishStreamableExecution(final StreamableOperatorInternals internals, final ExecutionContext exec,
        final PortOutput[] output) throws Exception {
        if (m_errorMessage != null) {
            setWarningMessage(m_errorMessage);
        }
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
//    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
//            throws InvalidSettingsException {
      protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
              throws InvalidSettingsException {  
        // TODO: check if user settings are available, fit to the incoming
        // table structure, and the incoming types are feasible for the node
        // to execute. If the node can execute in its current state return
        // the spec of its output data table(s) (if you can, otherwise an array
        // with null elements), or throw an exception with a useful user message
    	
    	 DataTableSpec tableSpec = (DataTableSpec)inSpecs[0];
         // check optional incoming connection
         if ((inSpecs.length > 1) && (inSpecs[1] instanceof DatabaseConnectionPortObjectSpec)) {
             DatabaseConnectionSettings connSettings =
                     ((DatabaseConnectionPortObjectSpec)inSpecs[1]).getConnectionSettings(getCredentialsProvider());

             if ((connSettings.getJDBCUrl() == null) || connSettings.getJDBCUrl().isEmpty()
                     || (connSettings.getDriver() == null) || connSettings.getDriver().isEmpty()) {
                 throw new InvalidSettingsException("No valid database connection provided via second input port");
             }
             if (!connSettings.getUtility().supportsInsert()) {
                 throw new InvalidSettingsException("Connected database does not support insert operations");
             }
             //TG S
           //  m_OutConnSpec = (DatabaseConnectionPortObjectSpec)inSpecs[getNrInPorts() - 1];; //tg add 
             m_OutConnSpec = (DatabaseConnectionPortObjectSpec)inSpecs[1]; //tg add 
             //TG E
         } else {
             if (!m_conn.getUtility().supportsInsert()) {
                 throw new InvalidSettingsException("Selected database does not support insert operations");
             }
           //TG S
             m_OutConnSpec = new DatabaseConnectionPortObjectSpec(m_conn);//tg
            //TG E
         }

//         // check table name
//         if ((m_tableName == null) || m_tableName.trim().isEmpty()) {
//             throw new InvalidSettingsException(
//                 "Configure node and enter a valid table name.");
//         }

         // throw exception if no data provided
         if (tableSpec.getNumColumns() == 0) {
             throw new InvalidSettingsException("No columns in input data.");
         }
         
         // copy map to ensure only columns which are with the data
         Map<String, String> map = new LinkedHashMap<String, String>();
         // check that each column has a assigned type
         for (int i = 0; i < tableSpec.getNumColumns(); i++) {
             final String name = tableSpec.getColumnSpec(i).getName();
             String sqlType = m_types.get(name);
             if (sqlType == null) {
                 final DataType type = tableSpec.getColumnSpec(i).getType();
                 if (type.isCompatible(IntValue.class)) {
                     sqlType = MyDBWriterNodeModel.SQL_TYPE_INTEGER;
                 } else if (type.isCompatible(DoubleValue.class)) {
                     sqlType = MyDBWriterNodeModel.SQL_TYPE_DOUBLE;
                 } else if (type.isCompatible(DateAndTimeValue.class)) {
                     sqlType = MyDBWriterNodeModel.SQL_TYPE_DATEANDTIME;
                 } else {
                	// System.out.println(name);
                	 //tg
                	 if ((name.equals("geom"))||(name.equals("the_geom"))){
                		 sqlType = "GEOMETRY"; 
					}
                	 else {
                		 sqlType = MyDBWriterNodeModel.SQL_TYPE_STRING; //the original
					}
                	 //tg
                 }
             }
             map.put(name, sqlType);
         }
         m_types.clear();
         m_types.putAll(map);
         // check table name
         if ((m_tableName == null) || m_tableName.trim().isEmpty()) {
             throw new InvalidSettingsException(
                 "Configure node and enter a valid table name.");
         }
         if (!m_append) {
             super.setWarningMessage("Existing table \"" + m_tableName + "\" will be dropped!");
         }
         return new PortObjectSpec[]{m_OutConnSpec}; //tg
        // return new DataTableSpec[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

        // TODO save user settings to the config object.
    	m_conn.saveConnection(settings);
        settings.addString(KEY_TABLE_NAME, m_tableName);
        settings.addBoolean(KEY_APPEND_DATA, m_append);
        settings.addBoolean(KEY_INSERT_NULL_FOR_MISSING_COLS, m_insertNullForMissingCols);
        // save SQL Types mapping
        NodeSettingsWO typeSett = settings.addNodeSettings(CFG_SQL_TYPES);
        for (Map.Entry<String, String> e : m_types.entrySet()) {
            typeSett.addString(e.getKey(), e.getValue());
        }
        // save batch size
        settings.addInt(KEY_BATCH_SIZE, m_batchSize);

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
        
    	 loadSettings(settings, true);
         m_conn.loadValidatedConnection(settings, getCredentialsProvider());

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
    	 loadSettings(settings, false);
         m_conn.validateConnection(settings, getCredentialsProvider());
    }
    
    private void loadSettings(
            final NodeSettingsRO settings, final boolean write)
            throws InvalidSettingsException {
        boolean append = settings.getBoolean(KEY_APPEND_DATA, true);
        final String table = settings.getString(KEY_TABLE_NAME);
        if (table == null || table.trim().isEmpty()) {
            throw new InvalidSettingsException(
                "Configure node and enter a valid table name.");
        }
        // read and validate batch size
        final int batchSize = settings.getInt(KEY_BATCH_SIZE, m_batchSize);
        if (batchSize <= 0) {
            throw new InvalidSettingsException("Batch size must be greater than 0, is " + batchSize);
        }
        // write settings or skip it
        if (write) {
            m_tableName = table;
            m_append = append;
            // load SQL Types for each column
            m_types.clear();
            try {
                NodeSettingsRO typeSett =
                    settings.getNodeSettings(CFG_SQL_TYPES);
                for (String key : typeSett.keySet()) {
                    m_types.put(key, typeSett.getString(key));
                }
            } catch (InvalidSettingsException ise) {
                // ignore, will be determined during configure
            }
            // load batch size
            m_batchSize = batchSize;
        }
        //introduced in KNIME 2.11 default behavior before was inserting null
        m_insertNullForMissingCols = settings.getBoolean(KEY_INSERT_NULL_FOR_MISSING_COLS, true);
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

