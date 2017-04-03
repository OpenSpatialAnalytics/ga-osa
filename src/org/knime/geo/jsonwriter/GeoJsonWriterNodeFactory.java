package org.knime.geo.jsonwriter;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "GeoJsonWriter" Node.
 * 
 *
 * @author 
 */
public class GeoJsonWriterNodeFactory 
        extends NodeFactory<GeoJsonWriterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public GeoJsonWriterNodeModel createNodeModel() {
        return new GeoJsonWriterNodeModel();
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
    public NodeView<GeoJsonWriterNodeModel> createNodeView(final int viewIndex,
            final GeoJsonWriterNodeModel nodeModel) {
        return new GeoJsonWriterNodeView(nodeModel);
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
        return new GeoJsonWriterNodeDialog();
    }

}

