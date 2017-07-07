package org.knime.gdalutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext ;
import org.knime.geo.resample.DirectoryFormat;
import org.knime.geoutils.Constants;


public class Utility {
	
	private final static String hdrFormat = "hdr.adf";
	private final static String metaDataFormat = "metadata.xml";
	public static String LOC_COLUMN = "Location";
	public static String outputFormat = ".tif";
	public static String shapeFormat = ".shp";
	public static String mergedFileName = "merged.shp";
	
	/***
	 * return a list of zip files in directory
	 * @param dirPath - the directory containing zip files of surveys
	 * @return - List of zip files in the directory
	 */
	public static List<String> zipFiles(String dirPath)
	{
		dirPath = dirPath.replace("\\", "/");
		List<String> zipFiles = new ArrayList<String>();
		File dir = new File(dirPath);
		  for (File file : dir.listFiles()) {
		    if (file.getName().toLowerCase().endsWith((".zip"))) {
		    	zipFiles.add(dirPath + "/" + file.getName());
		    }
		  }		  		  
		  return zipFiles;
	}
	
	/***
	 * return all hdr.adf files inside a zip file or a folder
	 * @param zipFileName
	 * @return List of .hdr files with full path
	 */
	public static List<String> readHdrFiles(String location)
	{
		location = location.replace("\\", "/");
		List<String> hdrFiles = new ArrayList<String>();
		
		boolean isZip = false;
		if (location.toLowerCase().contains(".zip"))
    		isZip = true;
		
		if (isZip) {
			try{
				 ZipFile zipFile = new ZipFile(location);
				 Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
				 while (zipEntries.hasMoreElements()) {
						String name = ((ZipEntry) zipEntries.nextElement()).getName();
						if (name.endsWith(hdrFormat)) {
							name = name.replace("\\", "/");
							String hdrFile = location + "/" + name;
							hdrFiles.add(hdrFile);
						}
				 }
				 zipFile.close();
			}
			catch (Exception e){
				System.out.println("Error reading hdr files in zip file");
				e.printStackTrace();
			}
		}
		else{
			try{
				File folder = new File(location);				
				File[] listOfFiles = folder.listFiles(new FileFilter() {
				    @Override
				    public boolean accept(File f) {
				        return f.isDirectory();
				    }
				});											
				
				for (int i = 0; i < listOfFiles.length; i++) {
					File subFolder = listOfFiles[i];
					File[] adfFiles = subFolder.listFiles(new FileFilter() {
					    @Override
					    public boolean accept(File f) {
					        return f.getName().endsWith(hdrFormat);
					    }
					});		
					
					if( adfFiles.length > 0 ) {					
						String hdrFile = adfFiles[0].getAbsolutePath();																						
						hdrFile = hdrFile.replace("\\", "/");							
						hdrFiles.add(hdrFile);
					}
				 }				
			}
			catch (Exception e){
				System.out.println("Error reading hdr files in folder");
				e.printStackTrace();
			}
			
		}
		
		return hdrFiles;
	}
	
	/***
	 * return a rankedList of surveys
	 * @param zipFileList or folderList
	 * @return return the rank and corresponding zip file/folder location
	 */
	public static Map<Integer,String> RankZipFilesByTime(List<String> zipFileList)
	{
		Map<Date,String> myMap = new HashMap<Date,String>();
		
		String  pathName = zipFileList.get(0);
		boolean isZip = false;
		if (pathName.toLowerCase().contains(".zip"))
    		isZip = true;
		
		if(isZip) {
		
			for (int i=0; i< zipFileList.size(); i++ ){
				String zipFileName = zipFileList.get(i);
				
				try{
					 ZipFile zipFile = new ZipFile(zipFileName);
					 Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
					 while (zipEntries.hasMoreElements()) {
						 	ZipEntry ze = (ZipEntry) zipEntries.nextElement();
							String name = ze.getName();
							if (name.endsWith(metaDataFormat)) {
								name = name.replace("\\", "/");
								//String metaDataXML = zipFileName + "/" + name;
								//metaDataList.add(metaDataXML);
								InputStreamReader zin =  new InputStreamReader(zipFile.getInputStream(ze));
								BufferedReader br = new BufferedReader(zin);
								String line;
								
					            while ((line = br.readLine()) != null) {
					            	if (line.startsWith("<metadata")){
					            		String dateStr = line.substring(
					            				line.indexOf("<CreaDate>")+("<CreaDate>").length(), 
					            				line.indexOf("</CreaDate>"));
					            		String timeStr = line.substring(
					            				line.indexOf("<CreaTime>")+("<CreaTime>").length(), 
					            				line.indexOf("</CreaTime>")-2);
					            		String dateTimeStr = dateStr + timeStr;
					            		
					            		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
					            		try
					                    {
					                        Date date = simpleDateFormat.parse(dateTimeStr);
					                        myMap.put(date, zipFileName);
					                    }
					                    catch (ParseException ex)
					                    {
					                        System.out.println("Exception "+ex);
					                    }
					            			
					            	}
					            }
					            br.close();
								
								break;
							}
					 }
					 zipFile.close();
				}
				catch (Exception e){
					System.out.println("Error reading " + zipFileName );
					e.printStackTrace();
				}
				
			}		
		}
		
		else{ // the files are inside a folder
						
			for (int i=0; i< zipFileList.size(); i++ ){				
				String zipFileName = zipFileList.get(i);
				
				try{
					File folder = new File(zipFileName);
					File[] listOfFiles = folder.listFiles(new FileFilter() {
					    @Override
					    public boolean accept(File f) {
					        return f.isDirectory();
					    }
					});									
						  
					File[] metaDataFiles = listOfFiles[0].listFiles(new FileFilter() {
					    public boolean accept(File f) {
					        return  f.getName().endsWith(metaDataFormat);
					    }
					});	
													
					String metaDataFileName = metaDataFiles[0].getAbsolutePath();
					metaDataFileName.replace("\\", "/");
					BufferedReader br = new BufferedReader( new FileReader(metaDataFileName));
								
					String line;								
		            while ((line = br.readLine()) != null) {
		            	if (line.startsWith("<metadata")){
		            		String dateStr = line.substring(
		            				line.indexOf("<CreaDate>")+("<CreaDate>").length(), 
		            				line.indexOf("</CreaDate>"));
		            		String timeStr = line.substring(
		            				line.indexOf("<CreaTime>")+("<CreaTime>").length(), 
		            				line.indexOf("</CreaTime>")-2);
		            		String dateTimeStr = dateStr + timeStr;
		            		
		            		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		            		try
		                    {
		                        Date date = simpleDateFormat.parse(dateTimeStr);
		                        myMap.put(date, zipFileName);
		                    }
		                    catch (ParseException ex)
		                    {
		                        System.out.println("Exception "+ex);
		                    }
		            			
		            	}
		            }
		            br.close();
					           				    	  
				}
				catch (Exception e){
					System.out.println("Error reading hdr files in folder");
					e.printStackTrace();
				}
								
			}
			
		}
		
		Map<Date, String> treeMap = new TreeMap<Date, String>(myMap);
		Map<Integer,String> rankedList = new HashMap<Integer,String>();
		
		int i = 1;
		for (String value : treeMap.values() ) {
			Integer key = new Integer(i);
			rankedList.put(key, value);
			i++;
		}
		
		return rankedList;
	}
		
	/***
	 * prepare and execute gdalwrap command for resampling
	 * @param inPathList
	 * @param outDir
	 * @param directoryFormat
	 * @param selectedColumn
	 * @param columnValueNames
	 * @param columnValueNos
	 * @param overWrite
	 * @param tap
	 * @param resample
	 * @param workingMemory
	 * @param oFormat
	 * @param s_srs
	 * @param t_srs
	 * @param xRes
	 * @param yRes
	 * @param isRun
	 * @param isZip
	 * @param exec
	 * @return
	 */
	public static List<String> ReSampleRaster(List<String> inPathList, String outDir, String directoryFormat,
			String selectedColumn, List<String> columnValueNames, List<String> columnValueNos,
			boolean overWrite, boolean tap,
			String resample, String workingMemory, String oFormat, String s_srs, String t_srs,
			String xRes, String yRes, boolean isRun, boolean isZip, ExecutionContext exec)
	{
	
		outDir = outDir.replace("\\", "/");
		List<List<String>>  listOfCommands = new ArrayList<List<String>>(inPathList.size());
		
		int index = 0;
		List <String> outputFiles = new ArrayList<String>();
	
		for (String inPath: inPathList) {
			List<String> commandList = new ArrayList<String>();
			
			inPath = inPath.replace("\\", "/");
			commandList.add("gdalwarp");
			if (tap)
				commandList.add("-tap");
			if (overWrite)
				commandList.add("-overwrite");
			if(!s_srs.isEmpty()){
				commandList.add("-s_srs");
				commandList.add(s_srs);
			}
			if(!t_srs.isEmpty()){
				commandList.add("-t_srs");
				commandList.add(t_srs);
			}
			commandList.add("-r");
			commandList.add(resample);
			commandList.add("-wm");
			commandList.add(workingMemory);
			commandList.add("-of");
			commandList.add(oFormat);
			commandList.add("-tr");
			commandList.add(xRes);
			commandList.add(yRes);		
			commandList.add(BuildInputPath(inPath,isZip));
		
			String outputSubFolder = "";
			String outFileName = "";
			String[] inPaths = inPath.split("/");
		
			if (directoryFormat.compareTo(DirectoryFormat.MainDir.toString())==0){
			
				outputSubFolder = outDir;
				outFileName = inPaths[inPaths.length-1];  //take the source file name from input path
			}
			else if (directoryFormat.compareTo(DirectoryFormat.SubDir.toString())==0){
			
				String parentFolder  = outDir+"/"+selectedColumn;  //column name as subdirectory
				File directory = new File(parentFolder);
				if (! directory.exists()){
					directory.mkdir();
				}
				outputSubFolder = parentFolder + "/" + columnValueNames.get(index);
				outFileName = columnValueNos.get(index) + outputFormat;
			
			}
	
			File directory = new File(outputSubFolder);
			if (! directory.exists()){
				directory.mkdir();
			}
				
			String createdFile = outputSubFolder+"/"+outFileName;
			commandList.add(pathBuilder(createdFile));
			listOfCommands.add(commandList);
			index++;
			outputFiles.add(createdFile);
		}
		
		/*
		String outputStr = "";
			
		String outputStringFile = outDir +"/resample_log.txt";
    	String outputCommandFile = outDir +"/commands.";
    	
    	if (isWindows())
    		outputCommandFile += "bat";
    	else
    		outputCommandFile += "sh";
    		
    	
    	commandList.add("\n");
    	commandList.add("exit");
    	String command = toCommand(commandList); 
    	writeOutputCommand(outputCommandFile, command);
   	
    	if (isRun)
			outputStr = executeBatch(outputCommandFile, exec);
		else
			outputStr = "commands";*/
		
		String outputCommandFile = outDir +"/commands.txt";
		writeListCommand(outputCommandFile, listOfCommands);
		
		String outputStr = "";
		if (isRun)
			outputStr = executeListCommand(listOfCommands, exec);
		else
			outputStr = "commands";
		
		String outputStringFile = outDir +"/resample_log.txt";
    	writeLog(outputStringFile, outputStr);
    	
		return outputFiles;	
	}
	
	
	/***
	 * prepare and execute gdal_merge command
	 * @param allInList
	 * @param allInPathList
	 * @param allMergedFileList
	 * @param outputType
	 * @param noDataValue
	 * @param oFormat
	 * @param isRun
	 * @param exec
	 * @return
	 */
	public static List<String> MergeRasters(List<List<String>> allInList, List<String> allInPathList, List<String> allMergedFileList, 
			String outputType, String noDataValue, String oFormat, boolean isRun, ExecutionContext exec)
	{
		int numList = 0;
		String gdalPath = getGdalPath();
		List<List<String>>  listOfCommands = new ArrayList<List<String>>(allMergedFileList.size());
		List<List<String>>  listOfPrintCommands = new ArrayList<List<String>>();
		List<String> executionPathList = new ArrayList<String>();
		List<Boolean> largeSamples = new ArrayList<Boolean>();
		List<String> newMergedFileList = new ArrayList<String>();
		
		for (String mergedFile : allMergedFileList) {
		
			String inPath = allInPathList.get(numList);
			mergedFile = mergedFile.replace("\\", "/");
			List<String> commandList = new ArrayList<String>();
			List<String> commandList1 = new ArrayList<String>();

			if (gdalPath.length() != 0)
				commandList.add("python");
			commandList.add(pathBuilder(gdalPath+"gdal_merge.py"));
			commandList.add("-ot");
			commandList.add(outputType);
			commandList.add("-a_nodata");
			commandList.add(noDataValue);
			commandList.add("-o");
		
			if(!mergedFile.endsWith(".tif"))
				mergedFile = mergedFile + ".tif";
			
			newMergedFileList.add(mergedFile);
		
			commandList.add(pathBuilder(mergedFile));
			commandList.add("-of");
			commandList.add(oFormat);
			commandList.add("-co");
			commandList.add("SPARSE_OK=TRUE");
	
			boolean isLargeResamples = false;
			List<String> inList = allInList.get(numList);
		
			if ( inList.size() > 1500 )
				isLargeResamples = true;
		
			if(!isLargeResamples){
				for (int i = 0; i < inList.size(); i++ ){
					commandList.add(inList.get(i));
				}
				largeSamples.add(new Boolean(false));
			}
			
			else{
				largeSamples.add(new Boolean(true));
				commandList1.addAll(commandList.subList(0, commandList.size()));
				File folder = new File(inPath+"temp");
				if (! folder.exists()){
					folder.mkdir();
				}
			
				for (int i = 0; i < inList.size(); i++ ){
				
					String inFile = inList.get(i);
					File oldFile = new File(inPath+"/"+inFile);
					String fName = inFile.substring(0,inFile.indexOf(outputFormat));
					File newFile = new File(folder+"/"+fName);
					try{		
						FileUtils.copyFile(oldFile, newFile); 
						commandList.add(fName);
						commandList1.add(inFile);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		
			String executionPath = inPath;
			if (isLargeResamples)
				executionPath = inPath+"temp";
			
			listOfCommands.add(commandList);
			executionPathList.add(executionPath);
			
			
			if(isLargeResamples)
				listOfPrintCommands.add(commandList1);
			else
				listOfPrintCommands.add(commandList);
				
			numList++;
			
			try {
				exec.checkCanceled();
				exec.setProgress(0.1 * (numList/allMergedFileList.size()));
			} catch (CanceledExecutionException e) {
				e.printStackTrace();
			}
		}
		
		String mergedFile1 = newMergedFileList.get(0);
		String folderLoc = mergedFile1.substring(0, mergedFile1.lastIndexOf("/"));
		String outputCommandFile = folderLoc +"/merge.txt";
		writeListCommand(outputCommandFile,listOfPrintCommands);
	
		String outputStr = "";
		if (isRun)
			outputStr = executeMergeCommand(listOfCommands,executionPathList,exec);
		else
			outputStr = "Error";
		
		String outputStringFile = folderLoc +"/merge_log.txt";
		writeLog(outputStringFile, outputStr);
		
		int k = 0;
		for (Boolean b : largeSamples){
			boolean isLargeResamples = b.booleanValue();
			if(isLargeResamples){
				String inPath = allInPathList.get(k);
				try{
					FileUtils.cleanDirectory(new File(inPath+"temp"));
					FileUtils.deleteDirectory(new File(inPath+"temp"));
				}
				catch (Exception e)
				{
					
				}
			}
			k++;
		}		
		
		return newMergedFileList;		
	}
	
	/***
	 * prepare and execute gdal_calc command
	 * @param sourceFileList
	 * @param varNameList
	 * @param destFiles
	 * @param type
	 * @param expression
	 * @param exec
	 * @return
	 */
	public static List<String> GetGdalCalc(List<List<String>> sourceFileList, List<List<String>> varNameList, 
			List<String> destFiles,  String type, String expression, ExecutionContext exec)
	{
	
		String gdalPath = getGdalPath();
		List<List<String>>  listOfCommands = new ArrayList<List<String>>(sourceFileList.size());
		List<String> newDestFiles = new ArrayList<String>();
		
		for(int index = 0; index < sourceFileList.size(); index++ ) {
		
			List<String> sourceFiles = sourceFileList.get(index);
			List<String> varNames = varNameList.get(index);
			String destFile = destFiles.get(index);
			
			destFile = destFile.replace("\\", "/");
			
			List<String> commandList = new ArrayList<String>();
			if (gdalPath.length() != 0)
				commandList.add("python");
			commandList.add(pathBuilder(gdalPath+"gdal_calc.py"));
					 
			for (int i=0; i< sourceFiles.size(); i++ ){			
				commandList.add("-"+varNames.get(i));		
				String sourceFile = sourceFiles.get(i);
				sourceFile = sourceFile.replace("\\", "/");
				commandList.add(pathBuilder(sourceFile));
			}
		
			if(!destFile.endsWith(outputFormat))
				destFile = destFile + outputFormat;
			
			newDestFiles.add(destFile);
		
			commandList.add("--outfile="+pathBuilder(destFile));
			//commandList.add("--type=Byte");
			if ( type != null )
				commandList.add("--type="+type);
			
			commandList.add("--calc="+pathBuilder(expression));
			//commandList.add("--NoDataValue="+noDataVlue);
			commandList.add("--overwrite");	
			
			listOfCommands.add(commandList);
		}
		
		String folderLoc = newDestFiles.get(0).substring(0, newDestFiles.get(0).lastIndexOf("/"));		
		String outputCommandFile = folderLoc +"/calc.txt";
		String outputLogFile = folderLoc +"/calc_log.txt";
		
		writeListCommand(outputCommandFile,listOfCommands);
		String outputStr = executeListCommand(listOfCommands, exec);
		writeLog(outputLogFile, outputStr);
		
		return newDestFiles;				
	}
	
	
	/***
	 * combine gdal_calc and gdal_polygonize to execute mask raster
	 * @param sourceFileList
	 * @param outPath
	 * @param type
	 * @param noDataVlue
	 * @param exec
	 * @return
	 */
	public static List<String> MaskRaster(List<String> sourceFileList, String outPath, String type, String noDataVlue, ExecutionContext exec)
	{
		
		outPath = outPath.replace("\\", "/");
		String gdalPath = getGdalPath();
		List<String> outFiles = new ArrayList<String>();
		List<List<String>>  listOfCommands = new ArrayList<List<String>>(sourceFileList.size());
		List<String> destFiles = new ArrayList<String>();
		
		
		for(String sourceFile :  sourceFileList) {
			
			List<String> commandList = new ArrayList<String>();
			if (gdalPath.length() != 0)
				commandList.add("python");
			commandList.add(pathBuilder(gdalPath+"gdal_calc.py"));	
			
			commandList.add("-A");
			sourceFile = sourceFile.replace("\\", "/");
			commandList.add(pathBuilder(sourceFile));
		
			String[] inPaths = sourceFile.split("/");
			String inFileName = inPaths[inPaths.length-1];
	    	String outFile = outPath + "/" + inFileName;       	    	
	    	String rank = inFileName.substring(0, inFileName.indexOf(".tif"));
    	
	    	String expression = "(A>-1000)*"+rank;				
			commandList.add("--outfile="+pathBuilder(outFile));		
			commandList.add("--type="+type);
			commandList.add("--calc="+pathBuilder(expression));
			commandList.add("--NoDataValue="+noDataVlue);
			commandList.add("--overwrite");	
			
			listOfCommands.add(commandList);
			outFiles.add(outFile);
			
			String destFile = outPath + "/" + rank + shapeFormat;
			destFiles.add(destFile);
		}
		
		String outputCommandFile = outPath +"/mask.txt";
		writeListCommand(outputCommandFile,listOfCommands);
		
		String outputStr = executeListCommand(listOfCommands, exec);
		String outputLogFile = outPath +"/mask_log.txt";
		writeLog(outputLogFile, outputStr);
		
		GetGdalPolygonize(outFiles,destFiles,"ESRI Shapefile", exec);
		
		return destFiles;				
	}
	
	
	/***
	 * prepare gdal_polygonize command and execute it
	 * @param sourceFiles
	 * @param destFiles
	 * @param format
	 * @param exec
	 */
	public static void GetGdalPolygonize(List<String> sourceFiles, List<String> destFiles, String format, ExecutionContext exec)
	{
		String gdalPath = getGdalPath();
		List<List<String>>  listOfCommands = new ArrayList<List<String>>(sourceFiles.size());
		
		for(int i = 0; i < sourceFiles.size(); i++ ) {
			String sourceFile = sourceFiles.get(i);
			String destFile = destFiles.get(i);
			sourceFile = sourceFile.replace("\\", "/");
			destFile = destFile.replace("\\", "/");
		
			List<String> commandList = new ArrayList<String>();
			if (gdalPath.length() != 0)
				commandList.add("python");
			commandList.add(pathBuilder(gdalPath+"gdal_polygonize.py"));
			commandList.add(pathBuilder(sourceFile));
			commandList.add("-f");
			commandList.add(pathBuilder(format));
			
			if(!destFile.endsWith(".shp"))
				destFile = destFile + ".shp";
			
			commandList.add(pathBuilder(destFile));
			commandList.add("fieldname");
			commandList.add(Constants.RANK);
			
			listOfCommands.add(commandList);
		}
		
		String folderLoc = destFiles.get(0).substring(0, destFiles.get(0).lastIndexOf("/"));
		String outputCommandFile = folderLoc +"/polygolize.txt";
		writeListCommand(outputCommandFile,listOfCommands);
		
		String outputStr = executeListCommand(listOfCommands, exec);
		String outputLogFile = folderLoc +"/polygolize_log.txt";
		
		writeLog(outputLogFile, outputStr);
				
	}
	
	/***
	 * prepare gdalwarp coomand for clipping and execute it
	 * @param srcClipFile
	 * @param srcTifFiles
	 * @param destTifFiles
	 * @param overWrite
	 * @param tap
	 * @param xRes
	 * @param yRes
	 * @param nData
	 * @param woName
	 * @param woValue
	 * @param exprList
	 * @param exec
	 * @return
	 */
	public static List<String> ClipRaster(String srcClipFile, List<String> srcTifFiles, List<String> destTifFiles, 
			boolean overWrite, boolean tap, String xRes, String yRes, String nData, String woName, String woValue,
			List<String> exprList, ExecutionContext exec)
	{
		
		List<List<String>>  listOfCommands = new ArrayList<List<String>>(srcTifFiles.size());
		
		srcClipFile = srcClipFile.replace("\\", "/");
		
		
		
		for(int i = 0; i < srcTifFiles.size(); i++ ) {
			
			String srcTifFile = srcTifFiles.get(i);
			String destTifFile = destTifFiles.get(i);
			
		
			srcTifFile = srcTifFile.replace("\\", "/");
			destTifFile = destTifFile.replace("\\", "/");
		
			List<String> commandList = new ArrayList<String>();
			commandList.add("gdalwarp");
			if(!woName.isEmpty() && !woValue.isEmpty()){
				commandList.add("-wo");
				commandList.add(pathBuilder(woName+"="+woValue));
			}
			if (tap)
				commandList.add("-tap");
			if (overWrite)
				commandList.add("-overwrite");
			if( !xRes.isEmpty() && !yRes.isEmpty() ){
				commandList.add("-tr");
				commandList.add(xRes);
				commandList.add(yRes);
			}
			if (!nData.isEmpty()) {
				commandList.add("-dstnodata");
				commandList.add(nData);
			}
			commandList.add("-cutline");
			commandList.add(pathBuilder(srcClipFile));
			if (!exprList.isEmpty()) {
				String cWhere = exprList.get(i);
				commandList.add("-cwhere");
				//String[] cutlineFeatures = cWhere.split("=");
				//String name = cutlineFeatures[0].trim();
				//int value = Integer.parseInt(cutlineFeatures[1].trim());
				commandList.add("\""+cWhere+"\"");
			}
			commandList.add("-crop_to_cutline");
			commandList.add(pathBuilder(srcTifFile));
			commandList.add(pathBuilder(destTifFile));
			
			listOfCommands.add(commandList);
		}
		
		String folderLoc = destTifFiles.get(0).substring(0, destTifFiles.get(0).lastIndexOf("/"));
		String outputCommandFile = folderLoc +"/ClipRaster.txt";
    	String outputLogFile = folderLoc +"/ClipRaster_log.txt";
		
    	writeListCommand(outputCommandFile,listOfCommands);
		String outputStr = executeListCommand(listOfCommands, exec);
    	writeLog(outputLogFile, outputStr);
    	    	
		return destTifFiles;	
		
	}
	
	/***
	 * prepare gdal_rasterize command and execute it
	 * @param srcShpFiles
	 * @param outFileLoc
	 * @param xRes
	 * @param yRes
	 * @param burn
	 * @param attr
	 * @param noDataValue
	 * @param outputType
	 * @param oFormat
	 * @param tap
	 * @param isRun
	 * @param exec
	 * @return
	 */
	public static List<String> Rasterize(List<String> srcShpFiles, String outFileLoc,  String xRes, String yRes,
			String burn, String attr, String noDataValue, String outputType, String oFormat,  
			boolean tap, boolean isRun, ExecutionContext exec)
	{
		outFileLoc = outFileLoc.replace("\\", "/");
		List<List<String>>  listOfCommands = new ArrayList<List<String>>(srcShpFiles.size());
		List<String> outFiles = new ArrayList<String>();
		
		for (String srcShpFile : srcShpFiles) {
		
			srcShpFile = srcShpFile.replace("\\", "/");
			String[] inPaths = srcShpFile.split("/");
			String inFileName = inPaths[inPaths.length-1];
			String fileName = inFileName.substring(0, inFileName.length()-4) + outputFormat;
			String outFile = outFileLoc + "/" + fileName;
			outFiles.add(outFile);
		
			List<String> commandList = new ArrayList<String>();
			commandList.add("gdal_rasterize");
			if (!burn.isEmpty()) {
				commandList.add("-a");
				commandList.add(attr);
			}
			if (!attr.isEmpty()) {
				commandList.add("-burn");
				commandList.add(burn);
			}
			commandList.add("-of");
			commandList.add(oFormat);
			commandList.add("-a_nodata");
			commandList.add(noDataValue);
			commandList.add("-tr");
			commandList.add(xRes);
			commandList.add(yRes);
			if (tap)
				commandList.add("-tap");
			commandList.add("-ot");
			commandList.add(outputType);
			commandList.add(pathBuilder(srcShpFile));
			commandList.add(pathBuilder(outFile));
			listOfCommands.add(commandList);
		}
		
		String outputStringFile = outFileLoc +"/rasterize_log.txt";
    	String outputCommandFile = outFileLoc +"/commands.txt";
    	
		writeListCommand(outputCommandFile, listOfCommands);
		
		String outputStr = "";
		if (isRun)
			outputStr = executeListCommand(listOfCommands,exec);
		else
			outputStr = "commands";
			
    	writeLog(outputStringFile, outputStr);
    	
		return outFiles;	
		
	}
	
	
	/***
	 * prepare command for gdal_proximity.py
	 * @param srcRasterList - list of raster files
	 * @param outFileLoc - location of output file
	 * @param noDataValue - required parameter for the command
	 * @param outputType- required parameter for the command
	 * @param oFormat- required parameter for the command
	 * @param distUnit- required parameter for the command
	 * @param isRun
	 * @param exec
	 * @return
	 */
	public static List<String> Proximity(List<String> srcRasterList, String outFileLoc, 
			String noDataValue, String outputType, String oFormat, String distUnit, 
			boolean isRun, ExecutionContext exec)
	{
		outFileLoc = outFileLoc.replace("\\", "/");
		String gdalPath = getGdalPath();
		List<List<String>>  listOfCommands = new ArrayList<List<String>>(srcRasterList.size());
		List<String> outFiles = new ArrayList<String>();
		
		for (String srcRaster : srcRasterList) {
		
			srcRaster = srcRaster.replace("\\", "/");
			String[] inPaths = srcRaster.split("/");
			String inFileName = inPaths[inPaths.length-1];
			String fileName = inFileName.substring(0, inFileName.length()-4) + "_proximity" + outputFormat;
			String outFile = outFileLoc + "/" + fileName;
			outFiles.add(outFile);
		
			List<String> commandList = new ArrayList<String>();
			if (gdalPath.length() != 0)
				commandList.add("python");
			commandList.add(pathBuilder(gdalPath+"gdal_proximity.py"));	
			commandList.add(pathBuilder(srcRaster));
			commandList.add(pathBuilder(outFile));
			commandList.add("-of");
			commandList.add(oFormat);
			commandList.add("-ot");
			commandList.add(outputType);
			commandList.add("-distunits");
			commandList.add(distUnit);
			commandList.add("-nodata");
			commandList.add(noDataValue);
			
			listOfCommands.add(commandList);
		}
		
		String outputCommandFile = outFileLoc +"/commands.txt";
		String outputStringFile = outFileLoc +"/proximity_log.txt";
		writeListCommand(outputCommandFile, listOfCommands);
			
			
		String outputStr = "";
		if (isRun)
			outputStr = executeListCommand(listOfCommands,exec);
		else
			outputStr = "commands";
			
    	writeLog(outputStringFile, outputStr);
		return outFiles;	
	}
	
	/***
	 * execute gdalinfo command for a source raster file
	 * @param sourceFile
	 * @return
	 */
	public static String GetGdalInfo(String sourceFile)
	{
		sourceFile = sourceFile.replace("\\", "/");
		List<String> commandList = new ArrayList<String>();
		commandList.add("gdalInfo");
		commandList.add("-stats");
		commandList.add(pathBuilder(sourceFile));
		
		//String command = toCommand(commandList);;
		String outputStr = executeCommand(commandList);
		
		return outputStr;			
	}
	
	/***
	 * return the fixed no data value
	 * @return
	 */
	public static String getNoDataValue()
	{
		return "-340282346638529993179660072199368212480.000";
	}
	
	/***
	 * prepare inout path location based on zip file or not
	 * @param inPath
	 * @param isZip
	 * @return
	 */
	public static String BuildInputPath(String inPath, boolean isZip)
	{
		String zipCommand = "";
		if (isZip){
			if(inPath.startsWith("/"))
				zipCommand = "/vsizip" + inPath;
			else
				zipCommand = "/vsizip/" + inPath;
			return pathBuilder(zipCommand);
		}
		else{
			 return pathBuilder(inPath);
		}
		
	}
	
	/***
	 * for windows add quotes for large path. Because otherwise it will not work when path name have spaces 
	 * @param path
	 * @return
	 */
	private static String pathBuilder(String path)
	{
		if ( isWindows() )
			return new String("\"" + path + "\"");
		else
			return path;
	}
	
	/***
	 * extract the gdal path from the system. Specially required for windows
	 * @return
	 */
	private static String getGdalPath()
	{
		boolean isWindows = true;
		
		String token = "";
		if ( isWindows() ){
			token = ";";
		}
		else{
			token = ":";
			isWindows = false;
		}
		
		String pathVar = System.getenv("PATH");
		String[] varList = pathVar.split(token);
		String gdalPath = "";
		
		for (int i = 0; i < varList.length; i++ ){
			String varStr = varList[i].toLowerCase();
			if(varStr.contains("gdal")){
				gdalPath = varList[i];
				break;
			}
		}
		
		gdalPath = gdalPath.replace("\\", "/");
		if ( isWindows )
			return gdalPath + "/";
		else
			return "";
	}
	
	/***
	 * convert a list in line of text. The command line aguments saved as a list and converted as a string for writing in a file
	 * @param commandList
	 * @return
	 */
	private static String toCommand(List<String> commandList)
	{
		String[] commands = new String[commandList.size()];
		commands = commandList.toArray(commands);
		return StringUtils.join(commands," ");	
	}
	
	/***
	 * execute independent gdal command in parallel
	 * @param listOfCommands - list of commands which have command arguments as list
	 * @param exec - execution environment of KNIME to show the progress bar
	 * @return
	 */
	private static String executeListCommand(List<List<String>> listOfCommands, ExecutionContext exec)  {
		
		File f;
		
		// set execution path based on windows or linux/mac
		if ( isWindows() ){
			f = new File("C:/");
		}
		else{
			f = new File("/");
		}
		
		StringBuffer finalOutput = new StringBuffer();
		
		//get number of cores and prepare execution service
		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService service = Executors.newFixedThreadPool(threads);
		
		List<Future<String>> futures = new ArrayList<Future<String>>();
		
		//parse each command in the list and execute them in parallel using java process call
		for (List<String> commandList : listOfCommands) {
				StringBuffer output = new StringBuffer();
		        Callable<String> callable = new Callable<String>() {
		            public String call() throws Exception {
		                ProcessBuilder pb = new ProcessBuilder(commandList);
		                pb.directory(f);
		                try{
		                	exec.checkCanceled();
							Process p = pb.start();
							int code = p.waitFor();
							if (code == 0){
								BufferedReader reader =
					                            new BufferedReader(new InputStreamReader(p.getInputStream()));
					
					                        String line = "";
								while ((line = reader.readLine())!= null) {
									output.append(line + "\n");
								}
							}
							exec.setProgress( 0.9 * ( (double) listOfCommands.indexOf(commandList) / (double) listOfCommands.size()));
						}
						catch (Exception e) {
							e.printStackTrace();
						}
		                return output.toString();
		            }
		        };
		        futures.add(service.submit(callable));
		  }

		service.shutdown();
		
		boolean finished = false;
		try {
			finished = service.awaitTermination(24, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		while(!finished); // wait until all threads finish their job or the time limit of 24 hour reached
		 
		for (Future<String> future : futures) {
		      try {
				finalOutput.append(future.get());
			} 
		    catch (Exception e) 
		    {
				e.printStackTrace();
			}
		}
		 
		return finalOutput.toString();
		 
		/*
		ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(threads);
		threadPool.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				
			}
		}, 1, threads, TimeUnit.SECONDS);
		*/
		
		//return output.toString();
	}
	
	private static String executeCommand(List<String> commandList) {
		
		File f;
		
		if ( isWindows() ){
			f = new File("C:/");
		}
		else{
			f = new File("/");
		}
		
		String[] commands = new String[commandList.size()];
		commands = commandList.toArray(commands);

		StringBuffer output = new StringBuffer();

		Process p = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(commands);
			pb.directory(f);
			p = pb.start();
			int code = p.waitFor();
			if (code == 0){
				BufferedReader reader =
	                            new BufferedReader(new InputStreamReader(p.getInputStream()));
	
	                        String line = "";
				while ((line = reader.readLine())!= null) {
					output.append(line + "\n");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();
	}
	
	/***
	 * excecute gdal_merge command using parallel processing
	 * @param listOfCommands
	 * @param listOflocations
	 * @param exec
	 * @return
	 */
	private static String executeMergeCommand(List<List<String>> listOfCommands, List<String> listOflocations, ExecutionContext exec) {
		
		StringBuffer finalOutput = new StringBuffer();
		
		int threads = Runtime.getRuntime().availableProcessors();  //get number of cores in the system
		ExecutorService service = Executors.newFixedThreadPool(threads); //prepare it as an executorservice
		
		List<Future<String>> futures = new ArrayList<Future<String>>();
		
		for (List<String> commandList : listOfCommands) {
			StringBuffer output = new StringBuffer();
			
			//process items in commnadlist in parallel and add them a future arraylist to maintain sequentiality in output
	        Callable<String> callable = new Callable<String>() {
	            public String call() throws Exception {
	                ProcessBuilder pb = new ProcessBuilder(commandList);
	                String location = listOflocations.get(listOfCommands.indexOf(commandList));
	                File f = new File(location);
	                pb.directory(f);
	                try{
	                	exec.checkCanceled();
						Process p = pb.start();
						int code = p.waitFor();
						if (code == 0){
							BufferedReader reader =
				                            new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				                        String line = "";
							while ((line = reader.readLine())!= null) {
								output.append(line + "\n");
							}
						}
						exec.setProgress( 0.9 * ( (double) listOfCommands.indexOf(commandList) / (double) listOfCommands.size()));
					}
					catch (Exception e) {
						e.printStackTrace();
					}
	                return output.toString();
	            }
	        };
	        futures.add(service.submit(callable));
		}

		service.shutdown();
	
		boolean finished = false;
		try {
			finished = service.awaitTermination(24, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	
		while(!finished);
		 
		for (Future<String> future : futures) {
		      try {
				finalOutput.append(future.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return finalOutput.toString();
	}
	
	/***
	 * parse the command from the list and prepare the whole command to write in a text file
	 * @param fileName
	 * @param listOfCommands
	 */
	private static void writeListCommand(String fileName, List<List<String>> listOfCommands)
	{
		BufferedWriter bw = null;
		
        try {          	
           bw = new BufferedWriter(new FileWriter(fileName, true));
           for (List<String> commandList : listOfCommands ){
        	   	bw.write(toCommand(commandList));
           		bw.newLine();        
           }
           bw.flush();
        }
        catch (IOException ioe) {
        	ioe.printStackTrace();
        } 
        finally {
	        if (bw != null) try {
	        	bw.close();
	        } 
	        catch (IOException ioe2) {}
	    } 		
	}
	
	/***
	 * wrtie gdal output log in a file
	 * @param fileName - the location of log file
	 * @param outputStr - the log to be written
	 */
	private static void writeLog(String fileName, String outputStr)
	{
		BufferedWriter bw = null;
		 		
        try {          	
           bw = new BufferedWriter(new FileWriter(fileName, true));                      
           bw.write(outputStr);
           bw.newLine();                      
           bw.flush();
        }
        catch (IOException ioe) {
        	ioe.printStackTrace();
        } 
        finally {
	        if (bw != null) try {
	        	bw.close();
	        } 
	        catch (IOException ioe2) {}
	    } 		
		
	}
	
	/***
	 * Merge multiple shape files in a signle shapefile using ogr utility provided by gdal
	 * @param shapeFiles list of shape files to be merged
	 * @return the location of merged shape file
	 */
	public static String MergeShapeFiles(List<String> shapeFiles)
	{
		//extract the root directory of shapefiles using the first shape file in the list
		String inSourcePath = shapeFiles.get(0);
		inSourcePath = inSourcePath.replace("\\", "/");
		String inPath = inSourcePath.substring(0,inSourcePath.lastIndexOf("/"));	
			
		//different apporach for windows OS and other OS (linux/mac)
		if (isWindows() ){			
			//for each shapefile prepare the command and run it using windows process call
			for (int i = 0; i < shapeFiles.size(); i++ ) {
				String inFile = shapeFiles.get(i).replace("\\", "/");  // update \ value in location for being consistent in all OS 
				String[] inPaths = inFile.split("/");
				String inSourceFile = inPaths[inPaths.length-1];
				String command = "ogr2ogr -f \"ESRI Shapefile\" ";
						
				if ( i == 0 )
					 command = command + mergedFileName + " " + inSourceFile;
				else
					command = command + "-update -append " + mergedFileName + " " + inSourceFile + " -nln Merged";
								
				Process p;
				try {
					p = Runtime.getRuntime().exec(command,null,new File(inPath));
					p.waitFor();
					//output += "Merged file " + inSourceFile + " \n";					
				} catch (Exception e) {
					e.printStackTrace();
				}												
			}								 
		}
		else{
			/*
			String importPath = "export PATH=/Library/Frameworks/GDAL.framework/Programs:$PATH";
			try {				
				Process p1 = null;
				p1 = Runtime.getRuntime().exec(importPath);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			*/
			
			String command = "for f in *.shp; do ogr2ogr -update -append " + mergedFileName + " $f -f \"ESRI Shapefile\"; done;";
			String shellScript = inPath +"/merge.sh";
			writeLog(shellScript, command);
			try {
				Process p = Runtime.getRuntime().exec("sh "+shellScript,null,new File(inPath));
				p.waitFor();					
			} catch (Exception e) {
				e.printStackTrace();
			}		
			finally{
				File f = new File(shellScript);
				f.delete();
			}
		}
		
		return inPath+"/"+mergedFileName;
					
	}	
	
	/***
	 * Check whether it's a windows OS or not other (Linux/Mac)
	 * @return
	 */
	private static boolean isWindows()
	{
		String os = System.getProperty("os.name");
		if ( os.startsWith("Windows") )
			return true;
		else
			return false;
	}
		
}
