package org.knime.MyDBTableConn;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * <code>NodeDialog</code> for the "MyDBTableConn" Node.
 * This is a Database Table Connection Test.TG
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author TG
 */
//This class does't use anymore TG.

public class MyDBTableConnNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring MyDBTableConn node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected MyDBTableConnNodeDialog() {
        super();
        
     /*   addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                    MyDBTableConnNodeModel.CFGKEY_COUNT,
                    MyDBTableConnNodeModel.DEFAULT_COUNT,
                    Integer.MIN_VALUE, Integer.MAX_VALUE),
                    "Counter:", step 1, componentwidth 5));*/
                    
    }
}

