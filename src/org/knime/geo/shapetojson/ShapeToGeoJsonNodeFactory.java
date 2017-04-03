package org.knime.geo.shapetojson;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ShapeToGeoJson" Node.
 * 
 *
 * @author Forkan
 */
public class ShapeToGeoJsonNodeFactory 
        extends NodeFactory<ShapeToGeoJsonNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ShapeToGeoJsonNodeModel createNodeModel() {
        return new ShapeToGeoJsonNodeModel();
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
    public NodeView<ShapeToGeoJsonNodeModel> createNodeView(final int viewIndex,
            final ShapeToGeoJsonNodeModel nodeModel) {
        return new ShapeToGeoJsonNodeView(nodeModel);
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
        return new ShapeToGeoJsonNodeDialog();
    }

}

