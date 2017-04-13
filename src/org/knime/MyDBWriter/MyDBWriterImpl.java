package org.knime.MyDBWriter;

import org.knime.core.node.port.database.DatabaseConnectionSettings;
import org.knime.core.node.port.database.writer.DBWriterImpl;

public class MyDBWriterImpl extends DBWriterImpl {
	 /**
     * @param conn {@link DatabaseConnectionSettings}
     */
    public MyDBWriterImpl(final DatabaseConnectionSettings conn) {
    	super(conn);
    	
    }
    /**
     * @param table
     * @param columnNames
     * @param mapping
     * @param insertNullForMissingCols
     * @return the insert statement
     */
    
    @Override
    protected String createInsertStatment(String table, String columnNames, int[] mapping,
    		boolean insertNullForMissingCols) {
    	// TODO Auto-generated method stub
    	//return super.createInsertStatment(table, columnNames, mapping, insertNullForMissingCols);
    	// // creates the wild card string based on the number of columns
        // this string it used every time an new row is inserted into the db
    	String strcolumnNames=columnNames.substring(1, columnNames.length()-1);
    	String[] strsplit=strcolumnNames.split(",");
    	/*for (int i = 0; i < strsplit.length; i++) {
			System.out.println(strsplit[i]);
		}*/
    	
        final StringBuilder wildcard = new StringBuilder("(");
        boolean first = true;
        for (int i = 0; i < mapping.length; i++) {
            if (mapping[i] >= 0 || insertNullForMissingCols) {
                    //insert only a ? if the column is available in the input table or the insert null for missing
                    //columns option is enabled
                if (first) {
                    first = false;
                } else {
                    wildcard.append(", ");
                }
                //tg
                 if((strsplit[i].trim().equals("geom") || (strsplit[i].trim().equals("the_geom")))){
                	 //wildcard.append("ST_GeomFromGeoJSON(?)");
                	//wildcard.append("ST_GeomFromGeoJSON(split_part(?,'|',2))");
                	wildcard.append("ST_SetSRID(ST_GeomFromGeoJSON(split_part(?,'|',2)),split_part(split_part(?,'\"',4),':',2)::int)");
                }
                else {
                	wildcard.append("?");
				}
            }
        }
        wildcard.append(")");
        // create table meta data with empty column information
        final String query = "INSERT INTO " + table + " " + columnNames + " VALUES " + wildcard;
        return query;
    }
}
