package org.knime.geo.concave;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "ConcaveHull" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Forkan
 */
public class ConcaveHullNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the ConcaveHull node.
     */
    protected ConcaveHullNodeDialog() {
    	
    	DialogComponentString targetPercentDialog = new DialogComponentString(
    			new SettingsModelString(ConcaveHullNodeModel.TP,"0.0"), "Target Percent");
    	
    	addDialogComponent(targetPercentDialog);

    }
}

