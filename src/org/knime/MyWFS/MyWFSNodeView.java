package org.knime.MyWFS;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.Style;
import org.geotools.swing.JMapPane;
import org.knime.core.node.NodeView;
import org.knime.geoutils.StyleLab;




import net.miginfocom.swing.MigLayout;


/**
 * <code>NodeView</code> for the "MyWFS" Node.
 * This is a test node for Web Feature Service.TG
 *
 * @author Gen Tian
 */
public class MyWFSNodeView extends NodeView<MyWFSNodeModel> {

	/**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link MyWFSNodeModel})
     */
    protected MyWFSNodeView(final MyWFSNodeModel nodeModel) {
        super(nodeModel);
            
        try {
			     
	        String typeName = nodeModel.m_selStr.getStringValue();
	        SimpleFeatureSource featureSource = nodeModel.dataStore.getFeatureSource(typeName);
	        
	        MapContent map = new MapContent();
	        
	        Style style = StyleLab.createStyle2(featureSource);
	        Layer layer = new FeatureLayer(featureSource, style);
	        map.addLayer(layer);
	        
	        JMapPane mapPane = new JMapPane(map);
	        mapPane.setBackground(Color.WHITE);
	        StringBuilder sb = new StringBuilder();
	        sb.append("[grow]");
	        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 0", "[grow]",  sb.toString()));
	        panel.add(mapPane,"grow");
	        panel.setPreferredSize(new Dimension(640, 480));
	        panel.setMaximumSize(panel.getPreferredSize()); 
	        panel.setMinimumSize(panel.getPreferredSize());
	        setComponent(panel);
	        	        
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {

        // TODO retrieve the new model from your nodemodel and 
        // update the view.
        MyWFSNodeModel nodeModel = 
            (MyWFSNodeModel)getNodeModel();
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

