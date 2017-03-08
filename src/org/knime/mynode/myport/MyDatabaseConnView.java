/**
 * 
 */
package org.knime.mynode.myport;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.apache.commons.lang3.StringEscapeUtils;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.port.database.DatabaseConnectionSettings;

/**
 * @author thinkpad
 * The database connection port view.
 */
public final class MyDatabaseConnView extends JPanel {
	 /**
     * @param sett the {@link ModelContentRO} with the settings
     */
	    MyDatabaseConnView(final ModelContentRO sett) {
	        super(new GridBagLayout());
	        super.setName("Connection");
	        StringBuilder buf = new StringBuilder("<html><body>");
//	        buf.append("<h2>Database Connection</h2>");
	        buf.append("<strong>Database Driver:</strong>&nbsp;&nbsp;");
	        buf.append("<tt>" + sett.getString("driver", "") + "</tt>");
	        buf.append("<br/><br/>");
	        buf.append("<strong>Database URL:</strong><br/>");
	        final String databaseURL = sett.getString("database", "");
	        buf.append("<tt>" + databaseURL + "</tt>");
	        buf.append("<br/><br/>");
	        boolean useCredential = sett.containsKey("credential_name");
	        if (useCredential) {
	            String credName = sett.getString("credential_name", "");
	            buf.append("<strong>Credential Name:</strong>&nbsp;&nbsp;");
	            buf.append("<tt>" + credName + "</tt>");
	        } else {
	            buf.append("<strong>User Name:</strong>&nbsp;&nbsp;");
	            buf.append("<tt>" + sett.getString("user", "") + "</tt>");
	        }
	        String dbIdentifier = sett.getString("databaseIdentifier", null);
	        if (dbIdentifier == null) {
	            dbIdentifier = DatabaseConnectionSettings.getDatabaseIdentifierFromJDBCUrl(databaseURL);
	        }
	        buf.append("<br/><br/>");
	        buf.append("<strong>Database Type:</strong>&nbsp;&nbsp;");
	        buf.append("<tt>" + dbIdentifier + "</tt>");
	        final String sql = sett.getString("statement", null);
	        if (sql != null) {
	            buf.append("<br/><br/>");
	            buf.append("<strong>SQL Statement:</strong><br/>");
	            final String query = StringEscapeUtils.escapeHtml4(sql);
	            buf.append("<tt>" + query + "</tt>");
	        }
	        buf.append("</body></html>");
	        final JTextPane textArea = new JTextPane();
	        textArea.setContentType("text/html");
	        textArea.setEditable(false);
	        textArea.setText(buf.toString());
	        textArea.setCaretPosition(0);
	        final JScrollPane jsp = new JScrollPane(textArea);
	        jsp.setPreferredSize(new Dimension(300, 300));
	        final GridBagConstraints c = new GridBagConstraints();
	        c.gridx = 0;
	        c.gridy = 0;
	        c.anchor = GridBagConstraints.CENTER;
	        c.fill = GridBagConstraints.BOTH;
	        c.weightx = 1;
	        c.weighty = 1;
	        super.add(jsp, c);
	        c.anchor = GridBagConstraints.CENTER;
	        c.fill = GridBagConstraints.BOTH;
	        c.weightx = 1;
	        c.weighty = 1;
	        final JButton sqlButton = new JButton("Copy SQL to clipboard");
	        sqlButton.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(final ActionEvent e) {
	                try {
	                    final StringSelection stringSelection = new StringSelection(sql);
	                    final Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
	                    clpbrd.setContents(stringSelection, null);
	                } catch (Throwable ex) {
	                    // catch any exception that might occur
	                }
	            }
	        });
	        c.gridy++;
	        c.anchor = GridBagConstraints.LINE_END;
	        c.fill = GridBagConstraints.NONE;
	        c.weightx = 0;
	        c.weighty = 0;
	        c.insets = new Insets(5, 0, 5, 5);
	        if (sql != null) {
	            super.add(sqlButton, c);
	        }
	    }

}
