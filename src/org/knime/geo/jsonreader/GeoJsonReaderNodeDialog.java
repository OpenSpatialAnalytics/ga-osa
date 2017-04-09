package org.knime.geo.jsonreader;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.geo.reader.ShapeFileReaderNodeModel;

/**
 * <code>NodeDialog</code> for the "GeoJsonReader" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Forkan
 */
public class GeoJsonReaderNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the GeoJsonReader node.
     */
    protected GeoJsonReaderNodeDialog() {
    	
    	 DialogComponentFileChooser inputPath = new DialogComponentFileChooser(
   		      new SettingsModelString(GeoJsonReaderNodeModel.JSON_FILE,""),
   		      	GeoJsonReaderNodeModel.JSON_FILE,
     		      JFileChooser.OPEN_DIALOG,
     		     	".geojson");
       
       inputPath.setBorderTitle("Source GeoJSON file");
       
       addDialogComponent(inputPath);

    }
}

