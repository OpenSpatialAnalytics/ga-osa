package org.knime.MyWFS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.geotools.data.DataStoreFinder;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButton;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.geoutils.Constants;

/**
 * <code>NodeDialog</code> for the "MyWFS" Node.
 * This is a test node for Web Feature Service.TG
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Gen Tian
 */
public class MyWFSNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring MyWFS node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected MyWFSNodeDialog() {
        super();
        SettingsModelString wfs_url = new SettingsModelString(MyWFSNodeModel.CFGKEY_WFS_URL,Constants.localWFS);
        addDialogComponent(new DialogComponentString(wfs_url,"WFS connection url:"));
        DialogComponentButton mButton=new DialogComponentButton("Connect");
        addDialogComponent(mButton);
        SettingsModelString selStr = new SettingsModelString(MyWFSNodeModel.CFGKEY_STRSEL, "");
        DialogComponentStringSelection dlgcombox = new DialogComponentStringSelection(
        		selStr,       //select the first one
		        "Select Source","none");
        addDialogComponent(dlgcombox);	
        Collection<String> strc = new ArrayList<String>();
        
        mButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				 String getCapabilities = null;
			        if (wfs_url.getStringValue().equals("")) {
			             getCapabilities = Constants.localWFS ;	
					}
			        else {
			        	 getCapabilities = wfs_url.getStringValue(); 
					}
			        
			        getCapabilities += "?REQUEST=GetCapabilities&version=1.0.0";					
			        Map connectionParameters = new HashMap();					
					connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities);					
					WFSDataStoreFactory  dsf = new WFSDataStoreFactory();
					String strtmp = "";
					  try {
						  MyWFSNodeModel.dataStore = dsf.createDataStore(connectionParameters);					
						  MyWFSNodeModel.m_blconnect=true;
					      String typeNames[] = MyWFSNodeModel.dataStore.getTypeNames();
					      for (int i = 0; i < typeNames.length; i++) {
					    	  strc.add(typeNames[i]);												    	
					      }   					      
					      dlgcombox.replaceListItems(strc, selStr.getStringValue());					      					      
					  }
					  catch (IOException ex) {
						  MyWFSNodeModel.m_blconnect = false;
					      ex.printStackTrace();
					  }
					  
			}
		} );        
                    
    }
}

