package org.knime.MyDBWriter;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.base.node.io.database.DBSQLTypesPanel;
import org.knime.base.node.io.database.util.DBDialogPane;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.database.DatabaseConnectionPortObjectSpec;
import org.knime.core.node.port.database.DatabaseConnectionSettings;

/**
 * <code>NodeDialog</code> for the "MyDBWriter" Node.
 * This is a Database Writer supporting Postgres spatial data. GT.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Gen TIAN
 */

//public class MyDBWriterNodeDialog extends DefaultNodeSettingsPane {
public class MyDBWriterNodeDialog extends NodeDialogPane {

    /**
     * New pane for configuring MyDBWriter node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
	 private final DBDialogPane m_loginPane = new DBDialogPane(false);

	    private final JTextField m_table = new JTextField("");

	    private final JCheckBox m_append = new JCheckBox("... to existing table (if any!)");

	    private final JCheckBox m_insertNullForMissing = new JCheckBox("Insert null for missing columns");

	    private final DBSQLTypesPanel m_typePanel;

	    private final JTextField m_batchSize;

	    /**
	     * Creates new dialog.
	     */
	    MyDBWriterNodeDialog() {
	// add login and table name tab
	        JPanel tableAndConnectionPanel = new JPanel(new GridBagLayout());

	        GridBagConstraints c = new GridBagConstraints();
	        c.gridx = 0;
	        c.gridy = 0;
	        c.anchor = GridBagConstraints.NORTHWEST;
	        c.fill = GridBagConstraints.HORIZONTAL;
	        c.weightx = 1;

	        tableAndConnectionPanel.add(m_loginPane, c);

	        c.gridy++;
	        JPanel p = new JPanel(new GridLayout());
	        p.add(m_table);
	        p.setBorder(BorderFactory.createTitledBorder(" Table Name "));
	        tableAndConnectionPanel.add(p, c);

	        c.gridy++;
	        p = new JPanel(new GridLayout());
	        p.add(m_append);
	        m_append.addChangeListener(new ChangeListener() {

	            @Override
	            public void stateChanged(final ChangeEvent e) {
	                m_insertNullForMissing.setEnabled(m_append.isSelected());
	            }
	        });
	        m_append.setToolTipText("Data columns from input and database table must match!");
	        p.add(m_insertNullForMissing);
	        p.setBorder(BorderFactory.createTitledBorder(" Append Data "));
	        tableAndConnectionPanel.add(p, c);

	        super.addTab("Settings", tableAndConnectionPanel);

	// add SQL Types tab
	        m_typePanel = new DBSQLTypesPanel();
	        final JScrollPane scroll = new JScrollPane(m_typePanel);
	        scroll.setPreferredSize(m_loginPane.getPreferredSize());
	        super.addTab("SQL Types", scroll);

	// advanced tab with batch size
	        final JPanel batchSizePanel = new JPanel(new FlowLayout());
	        batchSizePanel.add(new JLabel("Batch Size: "));
	        m_batchSize = new JTextField();
	        m_batchSize.setPreferredSize(new Dimension(100, 20));
	        batchSizePanel.add(m_batchSize);
	        super.addTab("Advanced", batchSizePanel);
	    }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    protected void loadSettingsFrom(final NodeSettingsRO settings,
	            final PortObjectSpec[] specs) throws NotConfigurableException {
	        // get workflow credentials
	        m_loginPane.loadSettingsFrom(settings, specs, getCredentialsProvider());
	        // table name
	        m_table.setText(settings.getString(MyDBWriterNodeModel.KEY_TABLE_NAME, ""));
	        // append data flag
	        m_append.setSelected(settings.getBoolean(MyDBWriterNodeModel.KEY_APPEND_DATA, true));

	        m_insertNullForMissing.setSelected(
	            settings.getBoolean(MyDBWriterNodeModel.KEY_INSERT_NULL_FOR_MISSING_COLS, false));
	        m_insertNullForMissing.setEnabled(m_append.isSelected());

	        // load SQL Types for each column
	        try {
	            NodeSettingsRO typeSett = settings.getNodeSettings(MyDBWriterNodeModel.CFG_SQL_TYPES);
	            m_typePanel.loadSettingsFrom(typeSett, (DataTableSpec)specs[0]);
	        } catch (InvalidSettingsException ise) {
	            m_typePanel.loadSettingsFrom(null, (DataTableSpec)specs[0]);
	        }

	        // load batch size
	        final int batchSize = settings.getInt(MyDBWriterNodeModel.KEY_BATCH_SIZE,
	                                              DatabaseConnectionSettings.BATCH_WRITE_SIZE);
	        m_batchSize.setText(Integer.toString(batchSize));

	        if ((specs.length > 1) && (specs[1] instanceof DatabaseConnectionPortObjectSpec)) {
	            m_loginPane.setVisible(false);
	        } else {
	            m_loginPane.setVisible(true);
	        }
	    }

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    protected void saveSettingsTo(final NodeSettingsWO settings)
	            throws InvalidSettingsException {
	        m_loginPane.saveSettingsTo(settings, getCredentialsProvider());

	        settings.addString(MyDBWriterNodeModel.KEY_TABLE_NAME, m_table.getText().trim());
	        settings.addBoolean(MyDBWriterNodeModel.KEY_APPEND_DATA, m_append.isSelected());
	        settings.addBoolean(MyDBWriterNodeModel.KEY_INSERT_NULL_FOR_MISSING_COLS, m_insertNullForMissing.isSelected());

	        // save SQL Types for each column
	        NodeSettingsWO typeSett = settings.addNodeSettings(MyDBWriterNodeModel.CFG_SQL_TYPES);
	        m_typePanel.saveSettingsTo(typeSett);

	        // save batch size
	        final String strBatchSite = m_batchSize.getText().trim();
	        if (strBatchSite.isEmpty()) {
	            throw new InvalidSettingsException("Batch size must not be empty.");
	        }
	        try {
	            final int intBatchSize = Integer.parseInt(strBatchSite);
	            settings.addInt(MyDBWriterNodeModel.KEY_BATCH_SIZE, intBatchSize);
	        } catch (final NumberFormatException nfe) {
	            throw new InvalidSettingsException("Can't parse batch size \"" + strBatchSite
	                                               + "\", reason: " + nfe.getMessage(), nfe);
	        }
	    }
}

