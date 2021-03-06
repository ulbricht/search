package org.datacite.search.handler.dataimport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.Transformer;

public class KeyValueTransformer extends Transformer {

    public static final String TARGET_ATTRIBUTE = "column";
    public static final String KEYS_ATTRIBUTE = "keys";
    public static final String VALUES_ATTRIBUTE = "values";

    @Override
    public Object transformRow(Map<String, Object> row, Context context) {
        List<Map<String, String>> fields = context.getAllEntityFields();
        for (Map<String, String> field : fields) {
            if (field.containsKey(KEYS_ATTRIBUTE) && field.containsKey(VALUES_ATTRIBUTE)) {
                String keysField = field.get(KEYS_ATTRIBUTE);
                String valuesField = field.get(VALUES_ATTRIBUTE);
                String targetField = field.get(TARGET_ATTRIBUTE);
                row = transformField(row, keysField, valuesField, targetField);
            }
        }
        return row;
    }
    
    private Map<String, Object> transformField(Map<String, Object> row, String keysField, String valuesField, String targetField) {
        Object keys = row.get(keysField);
        Object values = row.get(valuesField);
        if (keys != null && values != null) {
            if (!(keys instanceof List && values instanceof List))
                throw new IllegalArgumentException(keysField + " and " + valuesField + " must be multivalued fields");
            
            List keyList = (List) keys;
            List valueList = (List) values;
            
            if (keyList.size() != valueList.size()) 
                throw new RuntimeException(keysField + " and " + valuesField + " must have the same size");
            
            List targetList = new ArrayList();
            
            for (int i = 0; i < keyList.size(); i++) {
                Object target = keyList.get(i) + ":" + valueList.get(i);
                targetList.add(target);
            }
            
            row.put(targetField, targetList);
        }
        return row;
    }

}
