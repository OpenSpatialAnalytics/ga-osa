package org.knime.MyDBReader;

import org.knime.base.node.io.database.util.DBReaderDialogPane;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.database.DatabaseConnectionPortObject;

/**
 * <code>NodeFactory</code> for the "MyDBReader" Node.
 * This is a Database Reader, TG.
 *
 * @author TG
 */
public class MyDBReaderNodeFactory 
        extends NodeFactory<MyDBReaderNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public MyDBReaderNodeModel createNodeModel() {
      //  return new MyDBReaderNodeModel();
    	return new MyDBReaderNodeModel(new PortType[]{DatabaseConnectionPortObject.TYPE_OPTIONAL},
    			//new PortType[]{BufferedDataTable.TYPE});
    	        new PortType[]{BufferedDataTable.TYPE,DatabaseConnectionPortObject.TYPE});//TG transfer Database Connection
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
       // return 1;
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<MyDBReaderNodeModel> createNodeView(final int viewIndex,
            final MyDBReaderNodeModel nodeModel) {
       // return new MyDBReaderNodeView(nodeModel);
    	return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
       // return new MyDBReaderNodeDialog();
       //	 return new DBReaderDialogPane(true, true) {
        return new MyDBReaderNodeDialog(true, true) {
            /** {@inheritDoc} */
             @Override
             protected boolean runWithoutConfigure() {
                 return true;
             }
         };
    	
    }

}