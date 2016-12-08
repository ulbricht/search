package org.datacite.search.handler.dataimport;

/*

  2016-08-19 ulbricht@gfz-potsdam.de  modified DataCite's TrimTransformer to work as ScienceKeywordsTransformer

*/


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.DataImporter;
import org.apache.solr.handler.dataimport.Transformer;

public class ScienceKeywordsTransformer extends Transformer {

    public static final String ATTRIBUTE = "sciencekeytrans";
    public static final String POSITION = "position";
    public int position;
    @Override
    public Object transformRow(Map<String, Object> row, Context context) {
        List<Map<String, String>> fields = context.getAllEntityFields();
        for (Map<String, String> field : fields) {
            String geotrans = field.get(ATTRIBUTE);
            position = Integer.parseInt(field.get(POSITION));


            if ("true".equals(geotrans)) {
                String columnName = field.get(DataImporter.COLUMN);
                Object value = row.get(columnName);
                if (value != null) 
                    row.put(columnName, transform(value));
            }
        }
        return row;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object transform(Object value) {
        if (value instanceof List) {
            List list = (List) value;
            List newlist = new ArrayList(list.size());
            for (Object elem : list) 
                newlist.add(transform(elem));
            return newlist;
        } else if (value instanceof String) {	

		String valstr=(String)value;	

		if (!valstr.startsWith("EARTH SCIENCE"))
			return "";

		String[] retarr=valstr.split(">");

		if (position<=retarr.length)
			return retarr[position-1];

		return "";

        }else{

	    throw new IllegalArgumentException("Can not parse "+value.toString());
  
        }
    }
    private String valueout(double value){
	String strval=String.valueOf(value);
	    return strval;

    }

}
