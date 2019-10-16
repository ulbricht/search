package org.datacite.search.handler.dataimport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.DataImporter;
import org.apache.solr.handler.dataimport.Transformer;

public class GeoTransformer extends Transformer {

    public static final String TRIM_ATTRIBUTE = "geotrans";

    @Override
    public Object transformRow(Map<String, Object> row, Context context) {
        List<Map<String, String>> fields = context.getAllEntityFields();
        for (Map<String, String> field : fields) {
            String trim = field.get(TRIM_ATTRIBUTE);
            if ("true".equals(trim)) {
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
		
		double minlat,maxlat,minlon,maxlon;
		boolean is_minlat,is_maxlat,is_minlon,is_maxlon;
		minlat=maxlat=minlon=maxlon=0;
		is_minlat=is_maxlat=is_minlon=is_maxlon=false;
		
		Scanner sc=new Scanner(value.toString());
		
		if (sc.hasNextDouble()) {
			minlat=sc.nextDouble(); 
			is_minlat=true;
		}
		if (sc.hasNextDouble()) {
			minlon=sc.nextDouble(); 
			is_minlon=true;
		}
		if (sc.hasNextDouble()) {
			maxlat=sc.nextDouble(); 
			is_maxlat=true;
		}
		if (sc.hasNextDouble()) {
			maxlon=sc.nextDouble(); 
			is_maxlon=true;
		}
		sc.close();
		if (is_minlat && is_maxlat && is_minlon  && is_maxlon){
			
			if (maxlat-minlat>180.0){
				minlat=-90.0;
				maxlat=90.0;
			}
			if (maxlon-minlon>360.0){
				minlon=-180.0;
				maxlon=180.0;
			}						
			return String.valueOf(minlon)+" "+String.valueOf(minlat)+" "+String.valueOf(maxlon)+" "+String.valueOf(maxlat);			
			
		}else if (is_minlat  && is_minlon){
			if (minlat >90.0 || minlat<-90.0 || minlon>180.0 || minlon<-180.0)
				return "";
			
			return String.valueOf(minlon)+" "+String.valueOf(minlat);
			
		}else{
			throw new IllegalArgumentException("Ambiguous content for "+value.toString());
		}
        }else{
	    throw new IllegalArgumentException("Can not parse "+value.toString());
  
        }
    }

}
