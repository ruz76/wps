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

@DescribeProcess(title = "networkingWPS", description = "Networking example")
public class NetworkingWPS implements GeoServerProcess {

    @DescribeResult(name = "result", description = "WMS where the path is published")
    public String execute(@DescribeParameter(name = "from", description = "Municipality from") String from, @DescribeParameter(name = "to", description = "Municipality to") String to) {
        Examples e = new Examples();
        return e.networking(from, to);
    }
}
