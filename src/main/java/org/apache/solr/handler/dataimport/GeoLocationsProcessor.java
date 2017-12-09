/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.handler.dataimport;

import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.wrapAndThrow;
import org.apache.solr.core.SolrCore;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.util.SystemIdResolver;
import org.apache.solr.common.util.XMLErrorLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import javax.xml.stream.XMLStreamException ;
import java.util.Scanner;
import java.util.ArrayList;

/**
 * <p> An implementation of {@link EntityProcessor} which uses a streaming xpath parser to extract values out of XML documents.
 * It is typically used in conjunction with {@link URLDataSource} or {@link FileDataSource}. </p> <p/> <p> Refer to <a
 * href="http://wiki.apache.org/solr/DataImportHandler">http://wiki.apache.org/solr/DataImportHandler</a> for more
 * details. </p>
 * <p/>
 * <b>This API is experimental and may change in the future.</b>
 *
 *
 * @see XPathRecordReader
 * @since solr 1.3
 */
public class GeoLocationsProcessor extends EntityProcessorBase {
  private static final Logger LOG = LoggerFactory.getLogger(GeoLocationsProcessor.class);
  private static final XMLErrorLogger xmllog = new XMLErrorLogger(LOG);
  private boolean done=false;
  protected DataSource<ContentStream> dataSource;
  static XMLInputFactory parserfactory = XMLInputFactory.newInstance();

  @Override
  @SuppressWarnings("unchecked")
  public void init(Context context) {
    super.init(context);
    done=false;
  }
	
  @Override
  public Map<String, Object> nextRow() {

	if (done){
		return null;
	}
	done=true;

	try{

		dataSource = context.getDataSource();
		ContentStream content = (ContentStream) dataSource.getData(null);  
		InputStream data = content.getStream();
		XMLStreamReader parser = parserfactory.createXMLStreamReader(data);

		ArrayList<String> entities=parse(parser);

		parser.close();

		if (!entities.isEmpty()){
			Map<String, Object> result=new HashMap<String, Object>();
			result.put("geo",entities);
			return result;
		}
	}
	catch (XMLStreamException e){
	        LOG.error("Parser exception: "+ e.getMessage());
	}
	catch (IOException e){
	        LOG.error("IO Exception: "+ e.getMessage());
	}

	return null;
  }

	public static ArrayList<String> parse(XMLStreamReader parser)throws XMLStreamException  {

		int eventType = parser.getEventType();
		String lastLocalName="";
		double minlat,maxlat,minlon,maxlon;
		minlat=maxlat=minlon=maxlon=Double.NaN;

		int lastEvent = XMLEvent.END_ELEMENT;

		ArrayList<String> result = new ArrayList<String>();

		while(parser.hasNext()) {
		    eventType = parser.next();

		    switch (eventType){
			    case XMLEvent.START_ELEMENT:
				lastLocalName=parser.getLocalName();
				break;
		   	    case XMLEvent.CHARACTERS:
				if (lastEvent == XMLEvent.START_ELEMENT){
					if ( lastLocalName.equals("geoLocationBox")){
						Scanner sc=new Scanner(parser.getText());
						sc.useDelimiter("\\s+");
						if (sc.hasNext()) 
							minlat=Float.parseFloat(sc.next()); 
						if (sc.hasNext()) 
							minlon=Float.parseFloat(sc.next());  
						if (sc.hasNext()) 
							maxlat=Float.parseFloat(sc.next());  
						if (sc.hasNext()) 
							maxlon=Float.parseFloat(sc.next());  	
						sc.close();
					}
					if ( lastLocalName.equals("geoLocationPoint")){
						Scanner sc=new Scanner(parser.getText());
						sc.useDelimiter("\\s+");
						if (sc.hasNext()) 
							minlat=Float.parseFloat(sc.next()); 
						if (sc.hasNext()) 
							minlon=Float.parseFloat(sc.next());  
						sc.close();
					}
					if ( lastLocalName.equals("westBoundLongitude"))
						minlon=Float.parseFloat(parser.getText());  
					if ( lastLocalName.equals("eastBoundLongitude"))
						maxlon=Float.parseFloat(parser.getText());  	
					if ( lastLocalName.equals("southBoundLatitude"))
						minlat=Float.parseFloat(parser.getText()); 
					if ( lastLocalName.equals("northBoundLatitude"))
						maxlat=Float.parseFloat(parser.getText());  
					if ( lastLocalName.equals("pointLatitude"))
						minlat=Float.parseFloat(parser.getText()); 
					if ( lastLocalName.equals("pointLongitude"))
						minlon=Float.parseFloat(parser.getText());  
				}
				break;
			    case XMLEvent.END_ELEMENT:

				if (parser.getLocalName().equals("geoLocationPoint") || parser.getLocalName().equals("geoLocationBox")){

					if (!Double.isNaN(minlat) && !Double.isNaN(maxlat) && !Double.isNaN(minlon)  && !Double.isNaN(maxlon)){
			
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

						result.add(String.valueOf(minlon)+" "+String.valueOf(minlat)+" "+String.valueOf(maxlon)+" "+String.valueOf(maxlat));
			
					}else if (!Double.isNaN(minlat)  && !Double.isNaN(minlon)){

						if (minlon>=180.0 && minlon <=360.0){
							minlon=-180.0;
						}


						result.add(String.valueOf(minlon)+" "+String.valueOf(minlat));
					}
					minlat=maxlat=minlon=maxlon=Double.NaN;
				}
				break;
		    }
		    lastEvent=eventType;
		}
		return result;
	}

}
