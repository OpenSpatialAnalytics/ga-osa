package org.knime.geo.shapetojson;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.geo.jsonwriter.GeoJsonWriterNodeModel;
import org.knime.geo.reader.ShapeFileReaderNodeModel;

/**
 * <code>NodeDialog</code> for the "ShapeToGeoJson" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Forkan
 */
public class ShapeToGeoJsonNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the ShapeToGeoJson node.
     */
    protected ShapeToGeoJsonNodeDialog() {
    	
    	 super();
         
         DialogComponentFileChooser inputPath = new DialogComponentFileChooser(
     		      new SettingsModelString(ShapeToGeoJsonNodeModel.CFG_SHP_FILE,""),
     		     ShapeToGeoJsonNodeModel.CFG_SHP_FILE,
       		      JFileChooser.OPEN_DIALOG,
       		     	".shp");
         
         inputPath.setBorderTitle("Source Shapefile");
         
         addDialogComponent(inputPath);
         
         DialogComponentFileChooser outPath = new DialogComponentFileChooser(
     		      new SettingsModelString(ShapeToGeoJsonNodeModel.CFG_LOC,""),
     		     ShapeToGeoJsonNodeModel.CFG_LOC,
     		      JFileChooser.SAVE_DIALOG,
     		     	".geojson");
       	
       	outPath.setBorderTitle("Output GeoJSON file");

    }
}

