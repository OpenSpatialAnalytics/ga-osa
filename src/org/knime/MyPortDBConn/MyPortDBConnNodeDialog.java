package org.knime.MyPortDBConn;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import org.knime.base.node.io.database.connection.util.DBAdvancedPanel;
import org.knime.base.node.io.database.connection.util.DBAuthenticationPanel;
import org.knime.base.node.io.database.connection.util.DBGenericConnectionPanel;
import org.knime.base.node.io.database.connection.util.DBMiscPanel;
import org.knime.base.node.io.database.connection.util.DBTimezonePanel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.database.DatabaseConnectionSettings;

/**
 * <code>NodeDialog</code> for the "MyPortDBConn" Node.
 * This is a Database Connection using my DatabaseConnPort, TG.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author TG
 */
//public class MyPortDBConnNodeDialog extends DefaultNodeSettingsPane {
public class MyPortDBConnNodeDialog extends NodeDialogPane {

		 private final DatabaseConnectionSettings m_settings = new DatabaseConnectionSettings();

		    private final DBGenericConnectionPanel<DatabaseConnectionSettings> m_connectionPanel =
		        new DBGenericConnectionPanel<>(m_settings);

		    private final DBAuthenticationPanel<DatabaseConnectionSettings> m_authPanel = new DBAuthenticationPanel<>(
		        m_settings);

		    private final DBTimezonePanel<DatabaseConnectionSettings> m_tzPanel = new DBTimezonePanel<>(m_settings);

		    private final DBMiscPanel<DatabaseConnectionSettings> m_miscPanel = new DBMiscPanel<>(m_settings, true);

		    private final DBAdvancedPanel<DatabaseConnectionSettings> m_advancedPanel = new DBAdvancedPanel<>(m_settings);

		    /**
		     * Constructor.
		     */
		    MyPortDBConnNodeDialog() {
		        JPanel p = new JPanel(new GridBagLayout());

		        GridBagConstraints c = new GridBagConstraints();
		        c.gridy = 0;
		        c.weightx = 1;
		        c.fill = GridBagConstraints.HORIZONTAL;
		        c.insets = new Insets(0, 0, 4, 0);

		        p.add(m_connectionPanel, c);
		        c.gridy++;
		        p.add(m_authPanel, c);
		        c.gridy++;
		        p.add(m_tzPanel, c);
		        c.gridy++;
		        c.insets = new Insets(0, 0, 0, 0);
		        p.add(m_miscPanel, c);

		        addTab("Connection settings", p);
		        addTab("Advanced", m_advancedPanel);
		    }

		    /**
		     * {@inheritDoc}
		     */
		    @Override
		    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
		        throws NotConfigurableException {
		        try {
		            m_settings.loadValidatedConnection(settings, getCredentialsProvider());
		        } catch (InvalidSettingsException ex) {
		            // too bad, use default values
		        }

		        m_connectionPanel.loadSettings(specs);
		        m_authPanel.loadSettings(specs, getCredentialsProvider());
		        m_tzPanel.loadSettings(specs);
		        m_miscPanel.loadSettings(specs);
		        m_advancedPanel.loadSettings(specs);
		    }

		    /**
		     * {@inheritDoc}
		     */
		    @Override
		    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		        m_connectionPanel.saveSettings();
		        m_authPanel.saveSettings();
		        m_tzPanel.saveSettings();
		        m_miscPanel.saveSettings(getCredentialsProvider());
		        m_advancedPanel.saveSettings();

		        m_settings.saveConnection(settings);
		    }
	}

