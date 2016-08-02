package org.datacite.search.handler.dataimport;

/*

  2016-08-01 ulbricht@gfz-potsdam.de  modified DataCite's TrimTransformerTest to work as GeoTransformerTest

*/

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.DataImporter;
import org.easymock.EasyMock;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class GeoTransformerTest {
    
    GeoTransformer transformer = new GeoTransformer();
    
    String FIELD="field";
    
    @Test
    public void test() {
        assertEquals("-180.0 -90.0 180.0 90.0", transform("     -90 -180 90 180          "));
        assertEquals("5.0 1.0 20.0 20.0", transform("1 5 20 20"));
        assertEquals("105.85749816894531 52.07749938964844 105.85749816894531 52.079498291015625", transform("52.0775 105.8575 52.0795 105.8575"));
        assertEquals("12.192000389099121 49.5880012512207", transform("49.588 12.192"));	    
        assertEquals("20.0 2.0", transform("2 20"));	    
    }
    
    @Test
    public void testMultiValue() {
        List<String> original = Arrays.asList("-90 -180 90 180", "5 1 20 20","5 1");
        List<String> expected = Arrays.asList("-180.0 -90.0 180.0 90.0", "1.0 5.0 20.0 20.0", "1.0 5.0");
        List<String> actual = (List<String>) transform(original);
        assertArrayEquals(expected.toArray(), actual.toArray());
}
    
    @Test
    public void testBooleanParameter() {
        assertTrue(isTransforming("true"));
        assertFalse(isTransforming("false"));
        assertFalse(isTransforming(""));
        assertFalse(isTransforming(null));
        assertFalse(isTransforming("foobar"));
    }
    
    private boolean isTransforming(String geotrans) {
        String value = "20 2";
        return !transform(value, geotrans).equals(value);
    }
    
    private Object transform(Object value) {
        String geotrans = "true";
        return transform(value, geotrans);
    }

    private Object transform(Object value, String geotrans) {
        String column = "column";
        
        List<Map<String, String>> fields = new ArrayList<Map<String, String>>();
        Map<String, String> field = new HashMap<String, String>();
        field.put(DataImporter.COLUMN, column);
        field.put(GeoTransformer.ATTRIBUTE, geotrans);
        fields.add(field);
        
        Map<String, Object> row = new HashMap<String, Object>();
        row.put(column, value);
        
        Map<String, Object> newrow = (Map<String, Object>) transformer.transformRow(row, mockContext(fields));
        return newrow.get(column);
    }
    
    private Context mockContext(List<Map<String, String>> fields) {
        Context context = EasyMock.createNiceMock(Context.class);
        EasyMock.expect(context.getAllEntityFields()).andReturn(fields);
        EasyMock.replay(context);
        return context;
    }

    

}
