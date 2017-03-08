package org.knime.MyPortDBConn;

import org.knime.MyDBConn.MyDBConnNodeDialog;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "MyPortDBConn" Node.
 * This is a Database Connection using my DatabaseConnPort, TG.
 *
 * @author TG
 */
public class MyPortDBConnNodeFactory 
        extends NodeFactory<MyPortDBConnNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public MyPortDBConnNodeModel createNodeModel() {
        return new MyPortDBConnNodeModel();
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
    public NodeView<MyPortDBConnNodeModel> createNodeView(final int viewIndex,
            final MyPortDBConnNodeModel nodeModel) {
        //return new MyPortDBConnNodeView(nodeModel);
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
       // return new MyPortDBConnNodeDialog();   //one way,the same source code 
        return new MyDBConnNodeDialog();         //second way, use existing code
    }

}

