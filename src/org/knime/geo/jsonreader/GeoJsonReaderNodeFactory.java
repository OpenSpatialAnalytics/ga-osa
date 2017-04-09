package org.knime.geo.jsonreader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "GeoJsonReader" Node.
 * 
 *
 * @author Forkan
 */
public class GeoJsonReaderNodeFactory 
        extends NodeFactory<GeoJsonReaderNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public GeoJsonReaderNodeModel createNodeModel() {
        return new GeoJsonReaderNodeModel();
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
    public NodeView<GeoJsonReaderNodeModel> createNodeView(final int viewIndex,
            final GeoJsonReaderNodeModel nodeModel) {
        return new GeoJsonReaderNodeView(nodeModel);
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
        return new GeoJsonReaderNodeDialog();
    }

}

