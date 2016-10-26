/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoserver.hello.wps;

import cz.vsb.gis.ruz76.gt.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geoserver.wps.gs.GeoServerProcess;
import org.geoserver.wps.process.RawData;
import org.geoserver.wps.process.StreamRawData;

@DescribeProcess(title = "helloWPS", description = "Hello WPS Sample")
public class PlainInputWPS implements GeoServerProcess {

    @DescribeResult(name = "result", description = "output result")
    public String execute(@DescribeParameter(name = "data", description = "input csv", meta = { "mimeTypes=text/csv" }) final RawData input) {
        /*
        To test it put CSV content to TEXT area.
        For example.
        A,B,C
        A,B,C
        A,B,C
        A,B,C
        A,B,C
        */
        BufferedReader br = null;
        String line = "";
        int linescount = 0;
        int fieldscount = 0;
        try {
           br = new BufferedReader(new InputStreamReader(input.getInputStream()));
           while ((line = br.readLine()) != null) {
                 String[] fields = line.split(",");
                 fieldscount = fields.length;
                 linescount++;
           }

        } catch (IOException ex) {
            Logger.getLogger(PlainInputWPS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Number of lines: " + linescount + ". Number of fields: " + fieldscount + ".";
    }
}
