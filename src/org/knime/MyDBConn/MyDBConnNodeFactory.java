//package org.knime.MyDBConn;
 package org.knime.MyDBConn;
 
//import org.knime.base.node.io.database.connection.MyDBConnDialog;
//import org.knime.base.node.io.database.connection.;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "MyDBConn" Node.
 * This is a Database Connector TG.
 *
 * @author TG
 */
//
public class MyDBConnNodeFactory 
        extends NodeFactory<MyDBConnNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public MyDBConnNodeModel createNodeModel() {
        return new MyDBConnNodeModel();
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
    public NodeView<MyDBConnNodeModel> createNodeView(final int viewIndex,
            final MyDBConnNodeModel nodeModel) {
      //  return new MyDBConnNodeView(nodeModel);
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
        return new MyDBConnNodeDialog();     //one way,and the third way is copy source code,similar to JDBCConnectorNodeDialog
    //  return new JDBCConnectorNodeDialog();  //seconde way, in the same package
     // return new MyDBConnDialog();           //Third way, in the same package using public class to expose the package class ,
      
      
    }

}

