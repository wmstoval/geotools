/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2015, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.gml2.simple;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.xsd.XSDElementDeclaration;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.gml2.GML;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.gml2.bindings.GML2EncodingUtils;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.geotools.xml.XSD;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.helpers.AttributesImpl;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Optimized encoder delegate for GML2 feature collections.
 * 
 * @author Justin Deoliveira, OpenGeo
 * @author Andrea Aime, GeoSolutions
 *
 */
public class GML2FeatureCollectionEncoderDelegate extends FeatureCollectionEncoderDelegate {

    
    public GML2FeatureCollectionEncoderDelegate(SimpleFeatureCollection features, Encoder encoder) {
        super(features, encoder, new GML2Delegate(encoder));
    }
    

    public static class GML2Delegate implements GMLDelegate {

        static final QualifiedName FEATURE_MEMBER = new QualifiedName(GML.NAMESPACE,
                "featureMember", "gml");

        QualifiedName featureMember;

        private String gmlPrefix;

        private int numDecimals;

        public GML2Delegate(Encoder encoder) {
            this.gmlPrefix = encoder.getNamespaces().getPrefix(GML.NAMESPACE);
            this.featureMember = FEATURE_MEMBER.derive(gmlPrefix);
            this.numDecimals = getNumDecimals(encoder.getConfiguration());
        }

        private int getNumDecimals(Configuration configuration) {
            GMLConfiguration config;
            if (configuration instanceof GMLConfiguration) {
                config = (GMLConfiguration) configuration;
            } else {
                config = configuration.getDependency(GMLConfiguration.class);
            }

            if (config == null) {
                return 6;
            } else {
                return config.getNumDecimals();
            }
        }

        public List getFeatureProperties(SimpleFeature f, XSDElementDeclaration element, Encoder e) {
            return GML2EncodingUtils.AbstractFeatureType_getProperties(f, element, 
                    e.getSchemaIndex(),
                    new HashSet<String>(Arrays.asList("name", "description", "boundedBy")),
                    e.getConfiguration());
          
        }
        
        public EnvelopeEncoder createEnvelopeEncoder(Encoder e) {
            return new EnvelopeEncoder(e, gmlPrefix);
        }

        public void initFidAttribute(AttributesImpl atts) {
            atts.addAttribute( null,"fid","fid",null,"");
        }

        public void startFeatures(GMLWriter handler) {
            
        }

        public void startFeature(GMLWriter handler) throws Exception {
            handler.startElement(FEATURE_MEMBER, null);
        }

        public void endFeature(GMLWriter handler) throws Exception {
            handler.endElement(FEATURE_MEMBER);
        }

        public void endFeatures(GMLWriter handler) {
            
        }

        public void setSrsNameAttribute(AttributesImpl atts, CoordinateReferenceSystem crs) {
            atts.addAttribute(null, "srsName", "srsName", null, GML2EncodingUtils.toURI(crs, true));
        }
        
        @Override
        public void setGeometryDimensionAttribute(AttributesImpl srsatts, int dimension) {
            // nothing to do
        }

        @Override
        public void registerGeometryEncoders(Map<Class, GeometryEncoder> encoders, Encoder encoder) {
            encoders.put(Point.class, new PointEncoder(encoder, gmlPrefix));
            encoders.put(MultiPoint.class, new MultiPointEncoder(encoder, gmlPrefix));
            encoders.put(LineString.class, new LineStringEncoder(encoder, gmlPrefix));
            encoders.put(LinearRing.class, new LinearRingEncoder(encoder, gmlPrefix));
            encoders.put(MultiLineString.class, new MultiLineStringEncoder(encoder, gmlPrefix));
            encoders.put(Polygon.class, new PolygonEncoder(encoder, gmlPrefix));
            encoders.put(MultiPolygon.class, new MultiPolygonEncoder(encoder, gmlPrefix));
        }

        @Override
        public String getGmlPrefix() throws Exception {
            return gmlPrefix;
        }

        @Override
        public boolean supportsTuples() {
            return false;
        }

        @Override
        public void startTuple(GMLWriter output) {
            //

        }

        @Override
        public void endTuple(GMLWriter output) {
            //

        }

        @Override
        public XSD getSchema() {
            return GML.getInstance();
        }

        @Override
        public int getNumDecimals() {
            return numDecimals;
        }

        @Override
        public boolean forceDecimalEncoding() {
            return true;
        }

    }
}
