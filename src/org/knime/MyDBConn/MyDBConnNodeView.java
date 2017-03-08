//package org.knime.MyDBConn;
 package org.knime.MyDBConn;
 
import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "MyDBConn" Node.
 * This is a Database Connector TG.
 *
 * @author TG
 */
public class MyDBConnNodeView extends NodeView<MyDBConnNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link MyDBConnNodeModel})
     */
    protected MyDBConnNodeView(final MyDBConnNodeModel nodeModel) {
        super(nodeModel);

        // TODO instantiate the components of the view here.

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {

        // TODO retrieve the new model from your nodemodel and 
        // update the view.
        MyDBConnNodeModel nodeModel = 
            (MyDBConnNodeModel)getNodeModel();
        assert nodeModel != null;
        
        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
    
        // TODO things to do when closing the view
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {

        // TODO things to do when opening the view
    }

}

