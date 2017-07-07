package org.knime.geo.clip;

import javax.swing.JFileChooser;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.gdalutils.Utility;
/**
 * <code>NodeDialog</code> for the "ClipPolygonToRaster" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Forkan
 */
public class ClipPolygonToRasterNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the ClipPolygonToRaster node.
     */
    protected ClipPolygonToRasterNodeDialog() {
    	
    	
    	DialogComponentBoolean tapSelection = 
    			new DialogComponentBoolean ( new SettingsModelBoolean(ClipPolygonToRasterNodeModel.TAP,false), "(Target Aligned Pixels)");
    	
    	DialogComponentLabel wrapOption = new DialogComponentLabel("Wrap option");
    	
    	DialogComponentString xres = new DialogComponentString(
    			new SettingsModelString(ClipPolygonToRasterNodeModel.XRES,""), "X resolution");
    	
    	DialogComponentString yres = new DialogComponentString(
    			new SettingsModelString(ClipPolygonToRasterNodeModel.YRES,""), "Y resolution");
    	
    	DialogComponentString nodata = new DialogComponentString(
    			new SettingsModelString(ClipPolygonToRasterNodeModel.ND,""), "No Data Value");

    	
    	DialogComponentBoolean overWriteSelection = 
    			new DialogComponentBoolean ( new SettingsModelBoolean(ClipPolygonToRasterNodeModel.OR,true), "Overwrite");
    	
    	DialogComponentFileChooser shpFileSelect = new DialogComponentFileChooser(
    		      new SettingsModelString(ClipPolygonToRasterNodeModel.INPATH,""),
    		      ClipPolygonToRasterNodeModel.INPATH,
      		      JFileChooser.OPEN_DIALOG,
      		     	".shp");
    	
    	shpFileSelect.setBorderTitle("Clip shape file location");
    	
    	
    	DialogComponentFileChooser outputPath = 
    			new DialogComponentFileChooser(new SettingsModelString(ClipPolygonToRasterNodeModel.OUTPATH,""), 
    					ClipPolygonToRasterNodeModel.OUTPATH, JFileChooser.SAVE_DIALOG, true);
    	
    	outputPath.setBorderTitle("Output file location");
    	    	
    	DialogComponentColumnNameSelection outfileName = 
    			new DialogComponentColumnNameSelection(new SettingsModelString(ClipPolygonToRasterNodeModel.OUTFILENAME,""),
    					"Output file name",0,false,true, filterColumn);
    	
    	DialogComponentColumnNameSelection shapeFileAttr = 
    			new DialogComponentColumnNameSelection(new SettingsModelString(ClipPolygonToRasterNodeModel.SHPATTR,""),
    					"Shape File Attribute",0,false,true, filterColumn);
    	    	
    	
    	addDialogComponent(tapSelection);
    	addDialogComponent(wrapOption);
    	addDialogComponent(xres);
    	addDialogComponent(yres);
    	addDialogComponent(nodata);
    	addDialogComponent(overWriteSelection);
    	addDialogComponent(shpFileSelect);
    	addDialogComponent(outputPath);
    	addDialogComponent(outfileName);
    	addDialogComponent(shapeFileAttr);
    	
    }
    
    ColumnFilter filterColumn = new ColumnFilter() {
        @Override
        public boolean includeColumn(DataColumnSpec dataColumnSpec) {
            return !(dataColumnSpec.getName().compareTo(Utility.LOC_COLUMN) == 0);
        }

		@Override
		public String allFilteredMsg() {
			return "No column is available";
		}
	};
}

