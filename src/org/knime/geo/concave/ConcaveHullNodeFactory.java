package org.knime.geo.concave;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ConcaveHull" Node.
 * 
 *
 * @author Forkan
 */
public class ConcaveHullNodeFactory 
        extends NodeFactory<ConcaveHullNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ConcaveHullNodeModel createNodeModel() {
        return new ConcaveHullNodeModel();
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
    public NodeView<ConcaveHullNodeModel> createNodeView(final int viewIndex,
            final ConcaveHullNodeModel nodeModel) {
        return new ConcaveHullNodeView(nodeModel);
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
        return new ConcaveHullNodeDialog();
    }

}

