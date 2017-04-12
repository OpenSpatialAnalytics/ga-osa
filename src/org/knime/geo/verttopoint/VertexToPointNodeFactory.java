package org.knime.geo.verttopoint;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "VertexToPoint" Node.
 * 
 *
 * @author 
 */
public class VertexToPointNodeFactory 
        extends NodeFactory<VertexToPointNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public VertexToPointNodeModel createNodeModel() {
        return new VertexToPointNodeModel();
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
    public NodeView<VertexToPointNodeModel> createNodeView(final int viewIndex,
            final VertexToPointNodeModel nodeModel) {
        return new VertexToPointNodeView(nodeModel);
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
        return new VertexToPointNodeDialog();
    }

}

