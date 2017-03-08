/**
 * 
 */
package org.knime.mynode.myport;

import java.io.IOException;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.port.database.DatabaseConnectionSettings;
import org.knime.core.node.workflow.CredentialsProvider;

/**
 * @author thinkpad
 *
 */
public class MyDatabaseConnPortObject implements PortObject {
	
	/**
     * The spec for this port object.
     */
    protected final MyDatabaseConnPortObjectSpec m_spec;

    /**
     * Database port type.
     */
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(MyDatabaseConnPortObject.class);

    /**
     * Database type for optional ports.
     */
    public static final PortType TYPE_OPTIONAL =
        PortTypeRegistry.getInstance().getPortType(MyDatabaseConnPortObject.class, true);

    /**
     * {@inheritDoc}
     */
    @Override
    public MyDatabaseConnPortObjectSpec getSpec() {
        return m_spec;
    }

    
	/* (non-Javadoc)
	 * @see org.knime.core.node.port.PortObject#getSummary()
	 */
	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		String jdbcUrl = "<unknown>";
        String dbId = null;
        try {
            DatabaseConnectionSettings cs = getConnectionSettings(null);
            jdbcUrl = cs.getJDBCUrl();
            dbId = cs.getDatabaseIdentifier();
        } catch (InvalidSettingsException ex) {
            // jo mei...
        }
        StringBuilder buf = new StringBuilder();
        if (dbId != null) {
            buf.append("DB: ").append(dbId).append(" ");
        }
        buf.append("URL: ").append(jdbcUrl);
        return buf.toString();
	}

	/**
     * Creates a new database port object.
     *
     * @param spec database port object spec, must not be <code>null</code>
     */
    public MyDatabaseConnPortObject(final MyDatabaseConnPortObjectSpec spec) {
        if (spec == null) {
            throw new IllegalArgumentException("DatabaseConnectionPortObjectSpec must not be null!");
        }
        m_spec = spec;
    }

    /**
     * Returns the connection settings for this object.
     *
     * @param credProvider a credentials provider, may be <code>null</code>
     * @return a connection settings object
     * @throws InvalidSettingsException if the spec is missing required information for the connection
     */
    public DatabaseConnectionSettings getConnectionSettings(final CredentialsProvider credProvider)
        throws InvalidSettingsException {
        return m_spec.getConnectionSettings(credProvider);
    }

    /**
     * Serializer used to save {@link MyDatabaseConnPortObject}.
     *
     * 
     * 
     */
    public static final class Serializer extends PortObjectSerializer<MyDatabaseConnPortObject> {
        /**
         * {@inheritDoc}
         */
        @Override
        public void savePortObject(final MyDatabaseConnPortObject portObject,
            final PortObjectZipOutputStream out, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
            // nothing to save
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MyDatabaseConnPortObject loadPortObject(final PortObjectZipInputStream in,
            final PortObjectSpec spec, final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
            return new MyDatabaseConnPortObject((MyDatabaseConnPortObjectSpec)spec);
        }
    }
    
	/* (non-Javadoc)
	 * @see org.knime.core.node.port.PortObject#getViews()
	 */
	@Override
	public JComponent[] getViews() {
		// TODO Auto-generated method stub
	//	return null;
		return m_spec.getViews();
	}
	
	 /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MyDatabaseConnPortObject)) {
            return false;
        }
        MyDatabaseConnPortObject dbPort = (MyDatabaseConnPortObject) obj;
        return m_spec.equals(dbPort.m_spec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return m_spec.hashCode();
    }

}
