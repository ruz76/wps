/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoserver.hello.wps;

import cz.vsb.gis.ruz76.gt.Examples;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geoserver.wps.gs.GeoServerProcess;

@DescribeProcess(title="bboxWPS", description="Search in ESRI Shapefile based on attribute and returns BBOX of selected features")
public class BboxWPS implements GeoServerProcess {

   @DescribeResult(name="result", description="output result")
   public String execute(@DescribeParameter(name="name", description="name") String name) {
       Examples e = new Examples();
       return e.bbox(name);
   }
}