package org.datacite.search.handler.dataimport;

/*

  2016-08-01 ulbricht@gfz-potsdam.de  modified DataCite's TrimTransformer to work as GeoTransformer

*/


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.DataImporter;
import org.apache.solr.handler.dataimport.Transformer;

/*

GeoTransformer converts spatial coordinates from the DataCite format (GeoRSS) to 
the format that is accepted by SOLRs spatial search, i.e.

Points: "lat lon"  -> "lon lat"

BBoxes: "minlat minlon maxlat maxlon" -> "minlon minlat maxlon maxlat"

In addition, it checks the bounds (-180<=lon<=180, -90<=lat<=90) and makes 
corrections where possible. Currently, it is not possible specify bounding boxes
that cross the 180° line.


*/
public class GeoTransformer extends Transformer {

    public static final String ATTRIBUTE = "geotrans";

    @Override
    public Object transformRow(Map<String, Object> row, Context context) {
        List<Map<String, String>> fields = context.getAllEntityFields();
        for (Map<String, String> field : fields) {
            String geotrans = field.get(ATTRIBUTE);
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
		
		double minlat,maxlat,minlon,maxlon;
		boolean is_minlat,is_maxlat,is_minlon,is_maxlon;
		minlat=maxlat=minlon=maxlon=0;
		String ret=null;
		is_minlat=is_maxlat=is_minlon=is_maxlon=false;

		String strval=(String)value;
		
		Scanner sc=new Scanner((String)value);
		sc.useDelimiter("\\s+");
	
		if (sc.hasNext()) {
			minlat=Float.parseFloat(sc.next()); 
			is_minlat=true;
		}
		if (sc.hasNext()) {
			minlon=Float.parseFloat(sc.next());  
			is_minlon=true;
		}
		if (sc.hasNext()) {
			maxlat=Float.parseFloat(sc.next());  
			is_maxlat=true;
		}
		if (sc.hasNext()) {
			maxlon=Float.parseFloat(sc.next());  
			is_maxlon=true;
		}
		sc.close();



		if (is_minlat && is_maxlat && is_minlon  && is_maxlon){
			
			if (maxlat-minlat>=180.0){
				minlat=-90.0;
				maxlat=90.0;
			}
			if (maxlon-minlon>=360.0){
				minlon=-180.0;
				maxlon=180.0;
			}	

			if (minlat>maxlat){
				double tmp;
				tmp=minlat;
				minlat=maxlat;
				maxlat=tmp;
			}

			if (minlon>maxlon){
				double tmp;
				tmp=minlon;
				minlon=maxlon;
				maxlon=tmp;
			}

			ret= valueout(minlon)+" "+valueout(minlat)+" "+valueout(maxlon)+" "+valueout(maxlat);
			
			return ret;			
			
		}else if (is_minlat  && is_minlon){
			if (minlat >90.0 || minlat<-90.0 || minlon>180.0 || minlon<-180.0)
				return "";
			ret= valueout(minlon)+" "+valueout(minlat);

			return ret;
			
		}else{
			
			throw new IllegalArgumentException("Ambiguous content for "+value.toString());
		}
        }else{

	    throw new IllegalArgumentException("Can not parse "+value.toString());
  
        }
    }
    private String valueout(double value){
	String strval=String.valueOf(value);
	    return strval;

    }

}
