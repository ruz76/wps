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

@DescribeProcess(title="overlayWPSwithWMSoutput", description="Creates buffer around point and overlays it with polygon layer. Returns WMS where are published results of overlay.")
public class OverlayWPSwithWMSoutput implements GeoServerProcess {

   @DescribeResult(name="result", description="WMS where are data published")
   public String execute(@DescribeParameter(name="point", description="point") String point, @DescribeParameter(name="distance", description="distance to search") double distance) {
       Examples e = new Examples();
       return e.overlayWMS(point, distance);
   }
}