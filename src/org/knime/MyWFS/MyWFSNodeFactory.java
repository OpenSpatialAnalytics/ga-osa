package org.knime.MyWFS;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "MyWFS" Node.
 * This is a test node for Web Feature Service.TG
 *
 * @author Gen Tian
 */
public class MyWFSNodeFactory 
        extends NodeFactory<MyWFSNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public MyWFSNodeModel createNodeModel() {
        return new MyWFSNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<MyWFSNodeModel> createNodeView(final int viewIndex,
            final MyWFSNodeModel nodeModel) {
        return new MyWFSNodeView(nodeModel);
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
        return new MyWFSNodeDialog();
    }

}

