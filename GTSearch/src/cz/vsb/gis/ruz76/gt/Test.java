/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vsb.gis.ruz76.gt;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author ruz76
 */
public class Test {

    public String getBuffer(double x, double y, double distance) {
        GeometryFactory gf = new GeometryFactory();
        Point point = gf.createPoint(new Coordinate(x, y));
        Polygon p1 = (Polygon) point.buffer(distance);
        String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        kml += "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n";
        kml += "<Document>\n";
        kml += "<name>Buffer</name>\n";
        kml += "<Placemark>\n";
        kml += "<name>OKD</name>\n";
        kml += "<Point>\n";
        kml += "<coordinates>" + x + "," + y + ",0</coordinates>\n";
        kml += "</Point>\n";
        kml += "</Placemark>\n";
        kml += "<Placemark> \n";
        kml += " <Polygon> <outerBoundaryIs>  <LinearRing>  \n";
        kml += "  <coordinates>";
        Coordinate pc[] = p1.getCoordinates();
        for (int i = 0; i < pc.length; i++) {
            kml += " " + pc[i].x + "," + pc[i].y + ",0";
        } 
        kml += "</coordinates>\n";
        kml += " </LinearRing> </outerBoundaryIs> </Polygon>";
        kml += "</Placemark>\n";
        kml += "</Document>\n";
        kml += "</kml>\n";
        return kml;
        //return "Plocha obálky: " + p1.getArea();
        //return p1.toText();
        //return "Buffer: " + p1.toText() + " kolem bodu: " + point.toText();
    }
}
