package org.datacite.search.handler.dataimport;

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
    
    private boolean isTransforming(String trim) {
        String value = "20 2";
        return !transform(value, trim).equals(value);
    }
    
    private Object transform(Object value) {
        String trim = "true";
        return transform(value, trim);
    }

    private Object transform(Object value, String trim) {
        String column = "column";
        
        List<Map<String, String>> fields = new ArrayList<Map<String, String>>();
        Map<String, String> field = new HashMap<String, String>();
        field.put(DataImporter.COLUMN, column);
        field.put(GeoTransformer.TRIM_ATTRIBUTE, trim);
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
