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

@DescribeProcess(title="overlayWPS", description="Creates buffer around point and overlays it with polygon layer. Returns areas of overlay.")
public class OverlayWPS implements GeoServerProcess {

   @DescribeResult(name="result", description="output result")
   public String execute(@DescribeParameter(name="point", description="point") String point, @DescribeParameter(name="distance", description="distance to search") double distance) {
       Examples e = new Examples();
       return e.overlay(point, distance);
   }
}