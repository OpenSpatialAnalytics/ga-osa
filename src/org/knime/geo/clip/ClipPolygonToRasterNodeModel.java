package org.knime.geo.clip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.gdalutils.Utility;
import org.knime.geoutils.Constants;

/**
 * This is the model implementation of ClipPolygonToRaster.
 * 
 *
 * @author Forkan
 */
public class ClipPolygonToRasterNodeModel extends NodeModel {
    
    /**
     * Constructor for the node model.
     */
	
	 static final String OR = "over_write";
	 static final String TAP = "tap";
	 static final String XRES = "xres";
	 static final String YRES = "yres";
	 static final String ND = "nodata_value";
	 static final String CWHERE = "cwhere";
	 static final String INPATH = "inpath";
	 static final String OUTPATH = "outpath";
	 static final String OUTFILENAME = "outut_file_name";
	 static final String SHPATTR = "shapefile_attributes";
	 
	
	 public final SettingsModelBoolean overWrite = new SettingsModelBoolean(OR,true);
	 public final SettingsModelBoolean tap = new SettingsModelBoolean(TAP,false);
	 public final SettingsModelString xRes = new SettingsModelString(XRES,"");
	 public final SettingsModelString yRes = new SettingsModelString(YRES,"");
	 public final SettingsModelString noDataValue = new SettingsModelString(ND,"");
	 public final SettingsModelString cwhere = new SettingsModelString(CWHERE,"");
	 public final SettingsModelString inputShpFile = new SettingsModelString(INPATH,"");
	 public final SettingsModelString outpath = new SettingsModelString(OUTPATH,"");
	 public final SettingsModelString outputFileName = new SettingsModelString(OUTFILENAME,"");
	 public final SettingsModelString shapefileAttribute = new SettingsModelString(SHPATTR,"");

	

    protected ClipPolygonToRasterNodeModel() {
            
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
    	
    	BufferedDataTable inTable = inData[0];    	
    	
    	FileUtils.cleanDirectory(new File(outpath.getStringValue())); 
    	
		
		String overlapShapeFile = inputShpFile.getStringValue();
		int loc = inTable.getSpec().findColumnIndex(Utility.LOC_COLUMN);
		
		
		DataTableSpec outSpec = createSpec(inTable.getSpec(),false, false, false);
		BufferedDataContainer container = exec.createDataContainer(outSpec);
		
		List<String> srcTifFileList = new ArrayList<String>(); 
		List<String> destFileList = new ArrayList<String>(); 
		List<String> exprList = new ArrayList<String>(); 
		List<DataCell []> cellList = new ArrayList<DataCell []>(); 
				
		for (DataRow r : inTable){
			
 		StringCell inPathCell = (StringCell)r.getCell(loc);
    	String srcTifFile = inPathCell.getStringValue();
    	srcTifFile = srcTifFile.replace("\\", "/");
		String filenames[] = srcTifFile.split("/");
		String outFolder = outpath.getStringValue().replace("\\", "/");
    	String destFile = outFolder+"/"+filenames[filenames.length-1];
    	
    	/*
    	 * Bugs with Clip Raster Node was fixed by Arezou.
    	 */
    	String OutName = outputFileName.getStringValue();
    	System.out.println(destFile);
    	if(OutName != null){
    		int index = inTable.getSpec().findColumnIndex(OutName);
    		destFile = outFolder+"/"+ r.getCell(index).toString() + ".tif";
    	}
    	

    	String attribute = shapefileAttribute.getStringValue();
    	if(attribute != null){
    		int index_att = inTable.getSpec().findColumnIndex(attribute);
    		String expr = attribute + "=" + r.getCell(index_att).toString();
    		exprList.add(expr);
    	}
    	
    	srcTifFileList.add(srcTifFile);
    	destFileList.add(destFile);
    	
    	DataCell[] cells = new DataCell[outSpec.getNumColumns()];
		cells[0] = new StringCell(destFile);
		cellList.add(cells);
	    	
			exec.checkCanceled();
			//exec.setProgress((double) i / (double) s);    	
		}
		
		Utility.ClipRaster(overlapShapeFile, srcTifFileList, destFileList, 
    			overWrite.getBooleanValue(), tap.getBooleanValue(), 
    			xRes.getStringValue(), yRes.getStringValue(),
    			noDataValue.getStringValue(),
    			"","",exprList, exec);
		
		int i = 1;
		for (DataCell [] cells : cellList){
			container.addRowToTable(new DefaultRow("Row"+i, cells));
			exec.checkCanceled();
			exec.setProgress(0.9 + (0.1 * ((double) i / (double) inTable.size())));	  
			i++;
		}
		
		container.close();
		return new BufferedDataTable[] { container.getTable() };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

    	if (inputShpFile.getStringValue() == null) {
			throw new InvalidSettingsException("Input shape file must be specified");
		}
    	
    	if (outpath.getStringValue() == null) {
			throw new InvalidSettingsException("Output path must be specified");
		}

        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         //this.outFile.saveSettingsTo(settings);      
    	this.overWrite.saveSettingsTo(settings);
    	this.tap.saveSettingsTo(settings);
    	this.xRes.saveSettingsTo(settings);
    	this.yRes.saveSettingsTo(settings);
    	//this.woName.saveSettingsTo(settings);
    	//this.woValue.saveSettingsTo(settings);
    	this.noDataValue.saveSettingsTo(settings);
    	//this.cwhere.saveSettingsTo(settings);
    	this.inputShpFile.saveSettingsTo(settings);
    	//this.srcPath.saveSettingsTo(settings);
    	this.outpath.saveSettingsTo(settings);
    	this.outputFileName.saveSettingsTo(settings);
    	this.shapefileAttribute.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	this.overWrite.loadSettingsFrom(settings);
    	this.tap.loadSettingsFrom(settings);
    	this.xRes.loadSettingsFrom(settings);
    	this.yRes.loadSettingsFrom(settings);
    	this.noDataValue.loadSettingsFrom(settings);
    	this.inputShpFile.loadSettingsFrom(settings);
    	this.outpath.loadSettingsFrom(settings);
    	this.outputFileName.loadSettingsFrom(settings);
    	this.shapefileAttribute.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	this.overWrite.validateSettings(settings);
    	this.tap.validateSettings(settings);
    	this.xRes.validateSettings(settings);
    	this.yRes.validateSettings(settings);
    	this.noDataValue.validateSettings(settings);
    	this.inputShpFile.validateSettings(settings);
    	this.outpath.validateSettings(settings);
    	this.outputFileName.validateSettings(settings);
    	this.shapefileAttribute.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
 private static DataTableSpec createSpec(DataTableSpec inSpec, boolean useOverlap, boolean useRank, boolean useOverlapOnly) 
		 throws InvalidSettingsException {
		
    	List<DataColumnSpec> columns = new ArrayList<>();
		columns.add(new DataColumnSpecCreator(Utility.LOC_COLUMN, StringCell.TYPE).createSpec());
		if (useOverlap) {
			columns.add(new DataColumnSpecCreator(Utility.LOC_COLUMN+"_1", StringCell.TYPE).createSpec());
			int ovIndex = inSpec.findColumnIndex(Constants.OVID);
			columns.add(new DataColumnSpecCreator(Constants.OVID, inSpec.getColumnSpec(ovIndex).getType()).createSpec());
		}
		else if (useRank) {
			int rnkIndex = inSpec.findColumnIndex(Constants.RANK);
    		if (rnkIndex == -1)
    			rnkIndex = inSpec.findColumnIndex(Constants.RANK+"_1");
    		
			columns.add(new DataColumnSpecCreator(Constants.RANK, inSpec.getColumnSpec(rnkIndex).getType()).createSpec());
		}
		else if (useOverlapOnly){
			int ovIndex = inSpec.findColumnIndex(Constants.OVID);
			columns.add(new DataColumnSpecCreator(Constants.OVID, inSpec.getColumnSpec(ovIndex).getType()).createSpec());
		}
		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}

}

