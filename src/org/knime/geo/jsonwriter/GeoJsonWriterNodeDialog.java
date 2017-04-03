package org.knime.geo.jsonwriter;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.geo.writer.ShapeFileWriterNodeModel;

/**
 * <code>NodeDialog</code> for the "GeoJsonWriter" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author 
 */
public class GeoJsonWriterNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the GeoJsonWriter node.
     */
    protected GeoJsonWriterNodeDialog() {
    	
    	DialogComponentString projDialog = new DialogComponentString(
    			new SettingsModelString(GeoJsonWriterNodeModel.PROJ,""), "Projection srid value");
    	
    	addDialogComponent(projDialog);
    	
    	DialogComponentFileChooser outPath = new DialogComponentFileChooser(
  		      new SettingsModelString(GeoJsonWriterNodeModel.CFG_LOC,""),
  		      GeoJsonWriterNodeModel.CFG_LOC,
  		      JFileChooser.SAVE_DIALOG,
  		     	".geojson");
    	
    	outPath.setBorderTitle("Output GeoJSON file");
    	
    	addDialogComponent(outPath);

    }
}

