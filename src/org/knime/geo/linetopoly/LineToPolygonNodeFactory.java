package org.knime.geo.linetopoly;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "LineToPolygon" Node.
 * 
 *
 * @author Forkan
 */
public class LineToPolygonNodeFactory 
        extends NodeFactory<LineToPolygonNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public LineToPolygonNodeModel createNodeModel() {
        return new LineToPolygonNodeModel();
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
    public NodeView<LineToPolygonNodeModel> createNodeView(final int viewIndex,
            final LineToPolygonNodeModel nodeModel) {
        return new LineToPolygonNodeView(nodeModel);
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
        return new LineToPolygonNodeDialog();
    }

}

