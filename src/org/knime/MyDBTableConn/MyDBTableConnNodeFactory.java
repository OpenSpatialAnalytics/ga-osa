package org.knime.MyDBTableConn;

import org.knime.MyDBReader.MyDBReaderNodeDialog;
import org.knime.base.node.io.database.util.DBReaderDialogPane;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "MyDBTableConn" Node.
 * This is a Database Table Connection Test.TG
 *
 * @author TG
 */
public class MyDBTableConnNodeFactory 
        extends NodeFactory<MyDBTableConnNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public MyDBTableConnNodeModel createNodeModel() {
        return new MyDBTableConnNodeModel();
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
    public NodeView<MyDBTableConnNodeModel> createNodeView(final int viewIndex,
            final MyDBTableConnNodeModel nodeModel) {
      //  return new MyDBTableConnNodeView(nodeModel);
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
      //  return new MyDBTableConnNodeDialog();
     //   return new DBReaderDialogPane(true, true);
        return new MyDBReaderNodeDialog(true, true);
    }

}

