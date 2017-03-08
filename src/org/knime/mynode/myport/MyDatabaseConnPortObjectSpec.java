/**
 * 
 */
package org.knime.mynode.myport;

import java.io.IOException;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;
import org.knime.core.node.port.database.DatabaseConnectionSettings;
import org.knime.core.node.workflow.CredentialsProvider;

/**
 * @author thinkpad
 *
 */
public class MyDatabaseConnPortObjectSpec implements PortObjectSpec {

	/**
     * A serializer for {@link MyDatabaseConnPortObjectSpec}.
     */
    public static class Serializer extends PortObjectSpecSerializer<MyDatabaseConnPortObjectSpec> {
        @Override
        public MyDatabaseConnPortObjectSpec loadPortObjectSpec(final PortObjectSpecZipInputStream in)
            throws IOException {
            ModelContentRO modelContent = loadModelContent(in);
            return new MyDatabaseConnPortObjectSpec(modelContent);
        }

        @Override
        public void savePortObjectSpec(final MyDatabaseConnPortObjectSpec portObjectSpec,
            final PortObjectSpecZipOutputStream out) throws IOException {
            saveModelContent(out, portObjectSpec);
        }
    }
	
    /**
     * Reads the model content from the input stream.
     * @param in an input stream
     * @return the model content containing the spec information
     * @throws IOException if an I/O error occurs
     * @since 3.0
     */
    protected static ModelContentRO loadModelContent(final PortObjectSpecZipInputStream in) throws IOException {
        ZipEntry ze = in.getNextEntry();
        if (!ze.getName().equals(KEY_DATABASE_CONNECTION)) {
            throw new IOException("Key \"" + ze.getName() + "\" does not " + " match expected zip entry name \""
                + KEY_DATABASE_CONNECTION + "\".");
        }
        return ModelContent.loadFromXML(new NonClosableInputStream.Zip(in));
    }
	
    /**
     * Saves the given spec object into the output stream.
     * @param os an output stream
     * @param portObjectSpec the port spec
     * @throws IOException if an I/O error occurs
     * @since 3.0
     */
    protected static void saveModelContent(final PortObjectSpecZipOutputStream os,
        final MyDatabaseConnPortObjectSpec portObjectSpec) throws IOException {
        os.putNextEntry(new ZipEntry(KEY_DATABASE_CONNECTION));
        portObjectSpec.m_conn.saveToXML(new NonClosableOutputStream.Zip(os));
    }

    private final ModelContentRO m_conn;
    
    /**
     * Creates a new spec for a database connection port.
     *
     * @param conn connection model
     */
    protected MyDatabaseConnPortObjectSpec(final ModelContentRO conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Database connection model must not be null.");
        }
        m_conn = conn;
    }
    
    /**
     * Creates a new spec for a database connection port.
     *
     * @param connSettings the connection settings
     */
    public MyDatabaseConnPortObjectSpec(final DatabaseConnectionSettings connSettings) {
        if (connSettings == null) {
            throw new IllegalArgumentException("Database connection settings must not be null.");
        }
        ModelContent temp = new ModelContent(this.getClass().getName());
        connSettings.saveConnection(temp);
        m_conn = temp;
    }
    
    /**
     * returns the actual model content. The actual content is defined by the {@link DatabaseConnectionSettings}
     * class (and its potential subclasses).
     *
     * @return a model content
     */
    protected ModelContentRO getConnectionModel() {
        return m_conn;
    }
    
    /**
     * @return the database identifier
     * @since 2.11
     */
    public String getDatabaseIdentifier() {
        try {
            final DatabaseConnectionSettings cs = getConnectionSettings(null);
            return cs.getDatabaseIdentifier();
        } catch (InvalidSettingsException e) {
            // we cannot get the identifier
        }
        return null;
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
        return new DatabaseConnectionSettings(m_conn, credProvider);
    }
    
    private static final String KEY_DATABASE_CONNECTION = "mydatabase_connection.zip";
    
	/* (non-Javadoc)
	 * @see org.knime.core.node.port.PortObjectSpec#getViews()
	 */
	@Override
	public JComponent[] getViews() {
		// TODO Auto-generated method stub
		// return new JComponent[]{new DatabaseConnectionView(m_conn)};
		 return new JComponent[]{new MyDatabaseConnView(m_conn)};
		 
	}
	/**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MyDatabaseConnPortObjectSpec)) {
            return false;
        }
        MyDatabaseConnPortObjectSpec dbSpec = (MyDatabaseConnPortObjectSpec)obj;
        return m_conn.equals(dbSpec.m_conn);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return m_conn.hashCode();
    }

}
