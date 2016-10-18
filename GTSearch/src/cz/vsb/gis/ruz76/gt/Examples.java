/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vsb.gis.ruz76.gt;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.BBOX;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.postgresql.geometric.PGpoint;

/**
 *
 * @author jencek
 */
public class Examples {
    
    private SimpleFeatureSource getShapefileCollection(String path, String name) {
        SimpleFeatureSource fs = null;
        try {
            //SHP Read
            ShapefileDataStore sfds = new ShapefileDataStore(new URL(path));
            fs = sfds.getFeatureSource(name);
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex2) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex2);
        }
        return fs;
    }
    
    private FeatureCollection<SimpleFeatureType, SimpleFeature> getWfsCollection(String name) {
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = null;
        try {
            String getCapabilities = "http://localhost:8080/geoserver/wfs?REQUEST=GetCapabilities";
            
            Map connectionParameters = new HashMap();
            connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities);

// Step 2 - connection
            DataStore data = DataStoreFinder.getDataStore(connectionParameters);

// Step 3 - discouvery
            
            /* String typeNames[] = data.getTypeNames();
             String typeName = typeNames[0];
            
             for (int i=0; i<typeNames.length; i++) {
             System.out.println(typeNames[i]);
             }
            */
            
            String typeName = name;
            /*
            //Toto nefunguje - neumí zpracovat odpověď DescribeFeatureType
            //Divne je, ze to umel, al epak to prestalo fungovat - asi nejaka bitva knihoven
            SimpleFeatureType schema = data.getSchema(typeName);
            */
            //Mozny workaround
            ShapefileDataStore sfds = new ShapefileDataStore(new URL("file:///data/install/geoserver/geoserver-2.7.2/data_dir/data/sf/restricted.shp"));
            SimpleFeatureSource fs = sfds.getFeatureSource("restricted");
            SimpleFeatureType schema = fs.getSchema();

// Step 4 - target
            FeatureSource<SimpleFeatureType, SimpleFeature> source = data.getFeatureSource(typeName);
            //System.out.println("Metadata Bounds:" + source.getBounds());

// Step 5 - query
            String geomName = schema.getGeometryDescriptor().getLocalName();
            //Envelope bbox = new Envelope(589234, 609749, 4913638, 4928375);
            ReferencedEnvelope bbox = new ReferencedEnvelope();
            bbox.include(589234, 609749);
            bbox.include(4913638, 4928375);
            
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());

            //BoundingBox bbox = new BoundingBox();   
            BBOX filter = ff.bbox(ff.property(geomName), (BoundingBox) bbox);

            //Object polygon = JTS.toGeometry(bbox);            
            //Intersects filter = ff.intersects(ff.property(geomName), ff.literal(polygon));
            Query query = new DefaultQuery(typeName, filter, new String[]{geomName});
            features = source.getFeatures(query);
            
            return features;
            
        } catch (IOException ex) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
            return features;
        }
    }
    
    public String wfs(String name) {
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = getWfsCollection(name);
        SimpleFeatureSource fs = getShapefileCollection("file:///data/install/geoserver/geoserver-2.7.2/data_dir/data/sf/archsites.shp", "archsites");
        String nazvy = "";
        //FeatureIterator<SimpleFeature> fi = features.features();
        //ReferencedEnvelope bounds = new ReferencedEnvelope();
        /*SimpleFeatureType schema = features.getSchema();
         for (int i = 0; i < schema.getAttributeDescriptors().size(); i++) {
         nazvy = nazvy + " : " + schema.getAttributeDescriptors().get(i).getName();
         }*/
        while (features.features().hasNext()) {
            try {
                SimpleFeature feature = (SimpleFeature) features.features().next();
                nazvy = nazvy + "\nFeature: " + feature.getID();
                FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
                Filter filter = ff.within(ff.property("the_geom"), ff.literal(feature.getDefaultGeometry()));
                //System.out.println(filter.toString());
                SimpleFeatureIterator sfi = fs.getFeatures(filter).features();
                while (sfi.hasNext()) {
                    SimpleFeature sf = sfi.next();
                    nazvy = nazvy + "\nFeatures within: " + sf.getAttribute("str1");
                }
                //bounds.include(feature.getBounds());
            } //return bounds.toString();
            catch (IOException ex) {
                Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return nazvy;
    }
    
    public String bbox(String name) {
        String nazvy = "Hledaný řetězec v STATE_NAME: " + name + "\nNalezené objekty:";
        try {

            //SHP Read
            ShapefileDataStore sfds = new ShapefileDataStore(new URL("file:///data/install/geoserver/geoserver-2.7.2/data_dir/data/shapefiles/states.shp"));
            SimpleFeatureSource fs = sfds.getFeatureSource("states");
            
            Filter filter = CQL.toFilter("STATE_NAME LIKE '%" + name + "%'");
            SimpleFeatureIterator sfi = fs.getFeatures(filter).features();
            
            while (sfi.hasNext()) {
                SimpleFeature sf = sfi.next();
                MultiPolygon mp = (MultiPolygon) sf.getDefaultGeometry();
                Envelope envelope = new Envelope();
                Geometry enclosingGeometry = mp.getEnvelope();
                Coordinate[] enclosingCoordinates = enclosingGeometry.getCoordinates();
                for (Coordinate c : enclosingCoordinates) {
                    envelope.expandToInclude(c);
                }
                
                nazvy = nazvy + "\n" + sf.getAttribute("STATE_NAME") + ": " + envelope.getMinX() + "," + envelope.getMinY() + "," + envelope.getMaxX() + "," + envelope.getMaxY();
            }
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex2) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex2);
        } catch (CQLException ex) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nazvy;
    }
    
    public String overlay(String pointString, double distance) {
        String nazvy = "Objekt:Plocha překryvu";
        try {

            //SHP Read
            ShapefileDataStore sfds = new ShapefileDataStore(new URL("file:///data/install/geoserver/geoserver-2.7.2/data_dir/data/sf/restricted.shp"));
            SimpleFeatureSource fs = sfds.getFeatureSource("restricted");

            //double distance = 10000.0d;
            GeometryFactory gf = new GeometryFactory();
            String xy[] = pointString.split(" ");
            Point point = gf.createPoint(new Coordinate(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
            
            Polygon p1 = (Polygon) point.buffer(distance);
            
            SimpleFeatureIterator sfi = fs.getFeatures().features();
            while (sfi.hasNext()) {
                SimpleFeature sf = sfi.next();
                MultiPolygon mp2 = (MultiPolygon) sf.getDefaultGeometry();
                Polygon p2 = (Polygon) mp2.getGeometryN(0);
                Polygon p3 = (Polygon) p2.intersection(p1);
                nazvy = nazvy + "\n" + sf.getAttribute("cat") + ": " + p3.getArea();
            }
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex2) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex2);
        }
        return "Nalezené objekty: " + nazvy;
    }
    
    public String overlayWMS(String pointString, double distance) {
        long milis = System.currentTimeMillis();
        String workspace = "w" + String.valueOf(milis);
        String geoserverpath = "/data/install/gis/geoserver/geoserver-2.8.2";
        try {

            //SHP Read
            ShapefileDataStore sfds = new ShapefileDataStore(new URL("file://" + geoserverpath + "/data_dir/data/sf/restricted.shp"));
            SimpleFeatureSource fs = sfds.getFeatureSource("restricted");
            
            SimpleFeatureType TYPE = fs.getSchema();
            //double distance = 10000.0d;
            GeometryFactory gf = new GeometryFactory();
            String xy[] = pointString.split(" ");
            Point point = gf.createPoint(new Coordinate(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
            
            Polygon p1 = (Polygon) point.buffer(distance);
            List<SimpleFeature> features = new ArrayList<>();
            SimpleFeatureIterator sfi = fs.getFeatures().features();
            while (sfi.hasNext()) {
                SimpleFeature sf = sfi.next();
                MultiPolygon mp2 = (MultiPolygon) sf.getDefaultGeometry();
                Polygon p2 = (Polygon) mp2.getGeometryN(0);
                Polygon p3 = (Polygon) p2.intersection(p1);
                if (p3.getArea() > 0) {
                    sf.setDefaultGeometry(p3);
                    features.add(sf);
                }
            }
            new File(geoserverpath + "/data_dir/data/" + workspace + "/overlay/").mkdirs();
            saveFeatureCollectionToShapefile(geoserverpath + "/data_dir/data/" + workspace + "/overlay/overlay.shp", features, TYPE);
            
        } catch (MalformedURLException ex) {
            System.out.println(ex.getMessage());
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex2) {
            System.out.println(ex2.getMessage());
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex2);
        }
        
        addWorkspace(workspace);
        publishShapefile(workspace, "overlay", "file://" + geoserverpath + "/data_dir/data/" + workspace + "/overlay/overlay.shp");
        return "http://localhost:8080/geoserver/" + workspace + "/wms?service=WMS&version=1.3.0&request=GetCapabilities";
    }
    
    private void saveFeatureCollectionToShapefile(String path, List<SimpleFeature> features, SimpleFeatureType TYPE) throws MalformedURLException, IOException {
        File newFile = new File(path);
        
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", newFile.toURI().toURL());
        //params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        
        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        
        newDataStore.createSchema(TYPE);
        newDataStore.forceSchemaCRS(TYPE.getGeometryDescriptor().getCoordinateReferenceSystem());
        
        Transaction transaction = new DefaultTransaction("create");
        
        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
        SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
        SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
        featureStore.setTransaction(transaction);
        featureStore.addFeatures(collection);
        transaction.commit();
        transaction.close();
    }
    
    public SimpleFeatureCollection overlayCollectionOutput(String pointString, double distance) {
        //Problems with WFS ourptu - JSON and SHP works
        //https://github.com/geotools/geotools/blob/master/modules/unsupported/process-feature/src/main/java/org/geotools/process/vector/BufferFeatureCollection.java
        long milis = System.currentTimeMillis();
        String geoserverpath = "/data/install/gis/geoserver/geoserver-2.8.2";
        SimpleFeatureCollection collection = null;
        try {

            //SHP Read
            ShapefileDataStore sfds = new ShapefileDataStore(new URL("file://" + geoserverpath + "/data_dir/data/sf/restricted.shp"));
            SimpleFeatureSource fs = sfds.getFeatureSource("restricted");
            
            SimpleFeatureType TYPE = fs.getSchema();
            //double distance = 10000.0d;
            GeometryFactory gf = new GeometryFactory();
            String xy[] = pointString.split(" ");
            Point point = gf.createPoint(new Coordinate(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
            
            Polygon p1 = (Polygon) point.buffer(distance);
            List<SimpleFeature> features = new ArrayList<>();
            SimpleFeatureIterator sfi = fs.getFeatures().features();
            while (sfi.hasNext()) {
                SimpleFeature sf = sfi.next();
                MultiPolygon mp2 = (MultiPolygon) sf.getDefaultGeometry();
                Polygon p2 = (Polygon) mp2.getGeometryN(0);
                Polygon p3 = (Polygon) p2.intersection(p1);
                if (p3.getArea() > 0) {
                    sf.setDefaultGeometry(p3);
                    features.add(sf);
                }
            }
            collection = new ListFeatureCollection(TYPE, features);
            //saveFeatureCollectionToShapefile(geoserverpath + "/data_dir/data/" + workspace + "/overlay/overlay.shp", features, TYPE);
            
        } catch (MalformedURLException ex) {
            System.out.println(ex.getMessage());
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex2) {
            System.out.println(ex2.getMessage());
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex2);
        }
        
        return collection;
    }
    
    public String search(String pointString, double distance) {
        String nazvy = "Nazvy: ";
        try {

            //SHP Read
            ShapefileDataStore sfds = new ShapefileDataStore(new URL("file:///data/install/geoserver/geoserver-2.7.2/data_dir/data/sf/archsites.shp"));
            SimpleFeatureSource fs = sfds.getFeatureSource("archsites");

            //double distance = 10000.0d;
            GeometryFactory gf = new GeometryFactory();
            String xy[] = pointString.split(" ");
            //Point point = gf.createPoint(new Coordinate(600000, 4920000));
            Point point = gf.createPoint(new Coordinate(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
            Filter filter = ff.dwithin(ff.property("the_geom"), ff.literal(point), distance, "m");
            SimpleFeatureIterator sfi = fs.getFeatures(filter).features();
            while (sfi.hasNext()) {
                SimpleFeature sf = sfi.next();
                nazvy = nazvy + "\n" + sf.getAttribute("str1");
            }
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex2) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex2);
        }
        return "Nalezené objekty: " + nazvy;
    }
    
    private void addWorkspace(String name) {
        String strURL = "http://localhost:8080/geoserver/rest/workspaces";
        PostMethod post = new PostMethod(strURL);
        
        post.setRequestHeader("Content-type", "text/xml");
        post.setRequestEntity(new StringRequestEntity("<?xml version=\"1.0\"?><workspace><name>" + name + "</name></workspace>"));
        post.setDoAuthentication(true);
        
        HttpClient httpclient = new HttpClient();
        
        Credentials defaultcreds = new UsernamePasswordCredentials("admin", "geoserver");
        httpclient.getState().setCredentials(new AuthScope("localhost", 8080, AuthScope.ANY_REALM), defaultcreds);
        
        try {
            
            int response = httpclient.executeMethod(post);
            
        } catch (IOException ex) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            post.releaseConnection();
        }
    }
    
    private void publishShapefile(String workspace, String datastore, String shp) {
        String strURL = "http://localhost:8080/geoserver/rest/workspaces/" + workspace + "/datastores/" + datastore + "/external.shp";
        PutMethod put = new PutMethod(strURL);
        
        put.setRequestHeader("Content-type", "text/plain");
        put.setRequestEntity(new StringRequestEntity(shp));
        put.setDoAuthentication(true);
        
        HttpClient httpclient = new HttpClient();
        
        Credentials defaultcreds = new UsernamePasswordCredentials("admin", "geoserver");
        httpclient.getState().setCredentials(new AuthScope("localhost", 8080, AuthScope.ANY_REALM), defaultcreds);
        
        try {
            
            int response = httpclient.executeMethod(put);
            
        } catch (IOException ex) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            put.releaseConnection();
        }
    }
    
    public String testREST_POST_WorkSpace() {
        String strURL = "http://localhost:8080/geoserver/rest/workspaces";
        PostMethod post = new PostMethod(strURL);
        
        post.setRequestHeader("Content-type", "text/xml");
        post.setRequestEntity(new StringRequestEntity("<?xml version=\"1.0\"?><workspace><name>acme2</name></workspace>"));
        post.setDoAuthentication(true);
        
        HttpClient httpclient = new HttpClient();
        
        Credentials defaultcreds = new UsernamePasswordCredentials("admin", "geoserver");
        httpclient.getState().setCredentials(new AuthScope("localhost", 8080, AuthScope.ANY_REALM), defaultcreds);
        
        try {
            
            int response = httpclient.executeMethod(post);
            
            System.out.println("Response: ");
            BufferedReader br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            //System.out.println(post.getResponseBodyAsStream());

        } catch (IOException ex) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            post.releaseConnection();
        }
        
        return "testREST_POST_WorkSpace";
    }
    
    public String testREST_GET_WorkSpace() {
        String strURL = "http://localhost:8080/geoserver/rest/workspaces/acme";
        GetMethod get = new GetMethod(strURL);
        
        get.setRequestHeader("Accept", "text/xml");
        get.setDoAuthentication(true);
        
        HttpClient httpclient = new HttpClient();
        
        Credentials defaultcreds = new UsernamePasswordCredentials("admin", "geoserver");
        httpclient.getState().setCredentials(new AuthScope("localhost", 8080, AuthScope.ANY_REALM), defaultcreds);
        
        try {
            
            int response = httpclient.executeMethod(get);
            
            System.out.println("Response: ");
            BufferedReader br = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            //System.out.println(post.getResponseBodyAsStream());

        } catch (IOException ex) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            get.releaseConnection();
        }
        
        return "testREST_GET_WorkSpace";
    }
    
    public String testREST_PUT() {
        String strURL = "http://localhost:8080/geoserver/rest/workspaces/acme/datastores/reky/file.shp";
        PutMethod put = new PutMethod(strURL);
        
        put.setRequestHeader("Content-type", "application/zip");
        put.setRequestEntity(new FileRequestEntity(new File("reky.zip"), "application/zip"));
        put.setDoAuthentication(true);
        
        HttpClient httpclient = new HttpClient();
        
        Credentials defaultcreds = new UsernamePasswordCredentials("admin", "geoserver");
        httpclient.getState().setCredentials(new AuthScope("localhost", 8080, AuthScope.ANY_REALM), defaultcreds);
        
        try {
            
            int response = httpclient.executeMethod(put);
            
            System.out.println("Response: ");
            BufferedReader br = new BufferedReader(new InputStreamReader(put.getResponseBodyAsStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            //System.out.println(post.getResponseBodyAsStream());

        } catch (IOException ex) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            put.releaseConnection();
        }
        
        return "TestREST2";
    }
    
    public String testREST_PUT_Local() {
        String strURL = "http://localhost:8080/geoserver/rest/workspaces/acme/datastores/zeleznice/external.shp";
        PutMethod put = new PutMethod(strURL);
        
        put.setRequestHeader("Content-type", "text/plain");
        put.setRequestEntity(new StringRequestEntity("file:///data/shpdata/cr-shp-wgs84/linie/zeleznice.shp"));
        put.setDoAuthentication(true);
        
        HttpClient httpclient = new HttpClient();
        
        Credentials defaultcreds = new UsernamePasswordCredentials("admin", "geoserver");
        httpclient.getState().setCredentials(new AuthScope("localhost", 8080, AuthScope.ANY_REALM), defaultcreds);
        
        try {
            
            int response = httpclient.executeMethod(put);
            
            System.out.println("Response: ");
            BufferedReader br = new BufferedReader(new InputStreamReader(put.getResponseBodyAsStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            //System.out.println(post.getResponseBodyAsStream());

        } catch (IOException ex) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            put.releaseConnection();
        }
        
        return "http://localhost:8080/geoserver/acme/wms?service=WMS&version=1.3.0&request=GetCapabilities";
    }
    
    public String postGIS_example1() {
        //http://postgis.net/docs/manual-1.4/ch05.html
        Connection conn;
        
        try {
            /* 
             * Load the JDBC driver and establish a connection. 
             */
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://158.196.143.25:5432/test";
            conn = DriverManager.getConnection(url, "test", "test");

            /* 
             * Create a statement and execute a select query. 
             */
            Statement s = conn.createStatement();
            ResultSet r = s.executeQuery("SELECT ST_AsText(geom) as geom, gid FROM sf.archsites");
            while (r.next()) {
                
                String geom = r.getString(1);
                int gid = r.getInt(2);
                System.out.println("Row " + gid + ":");
                System.out.println(geom);
            }
            s.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Done";
    }
    
    public int postGIS_example2(String pointString, double distance) {
        //http://postgis.net/docs/manual-1.4/ch05.html
        Connection conn;
        String xy[] = pointString.split(" ");
        int count = 0;
        try {
            
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://158.196.143.25:5432/test";
            conn = DriverManager.getConnection(url, "test", "test");
            
            Statement s = conn.createStatement();
            s.execute("DROP VIEW IF EXISTS ruzicka_archsites_10000");
            s.execute("CREATE VIEW ruzicka_archsites_10000 AS SELECT ST_AsText(geom) as geom, gid FROM sf.archsites WHERE ST_DWITHIN(geom, ST_MakePoint(" + xy[0] + ", " + xy[1] + "), " + distance + ")");
            s.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Map params = new HashMap();
        params.put("dbtype", "postgis"); //must be postgis
//the name or ip address of the machine running PostGIS
        params.put("host", "158.196.143.25");
//the port that PostGIS is running on (generally 5432)
        params.put("port", new Integer(5432));
//the name of the database to connect to.
        params.put("database", "test");
        params.put("user", "test"); //the user to connect with
        params.put("passwd", "test"); //the password of the user.
        FeatureSource fs = null;
        DataStore pgDatastore;
        try {
            pgDatastore = DataStoreFinder.getDataStore(params);
            fs = pgDatastore.getFeatureSource("ruzicka_archsites_10000");
            //System.out.println("Count: " + fs.getCount(Query.ALL));
            count = fs.getCount(Query.ALL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return count;
    }
    
    public String postGIS_example3() {
        //http://postgis.net/docs/manual-1.4/ch05.html
        //apt-get install libpostgis-java
        //Nepovedlo se zprovoznit
        Connection conn;
        
        try {
            /* 
             * Load the JDBC driver and establish a connection. 
             */
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/user";
            conn = DriverManager.getConnection(url, "user", "user");

            //((org.postgresql.PGConnection)conn).addDataType("point", "org.postgis.PGpoint");
            /* 
             * Create a statement and execute a select query. 
             */
            Statement s = conn.createStatement();
            ResultSet r = s.executeQuery("SELECT ST_AsText(geom) as geom, gid FROM archsites");
            while (r.next()) {
                PGpoint geom = (PGpoint) r.getObject(1);
                int gid = r.getInt(2);
                System.out.println("Row " + gid + ":");
                System.out.println(geom.toString());
            }
            s.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Done";
    }
    
    private void publishPostGISTable(String name) {
        String strURL = "http://localhost:8080/geoserver/rest/workspaces/crwgs84/datastores/public/featuretypes";
        PostMethod post = new PostMethod(strURL);
        
        post.setRequestHeader("Content-type", "text/xml");
        post.setRequestEntity(new StringRequestEntity("<?xml version=\"1.0\"?><featureType><name>" + name + "</name></featureType>"));
        post.setDoAuthentication(true);
        
        HttpClient httpclient = new HttpClient();
        
        Credentials defaultcreds = new UsernamePasswordCredentials("admin", "geoserver");
        httpclient.getState().setCredentials(new AuthScope("localhost", 8080, AuthScope.ANY_REALM), defaultcreds);
        
        try {
            
            int response = httpclient.executeMethod(post);
            

        } catch (IOException ex) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            post.releaseConnection();
        }
        
   
    }
    
    public String networking(String from, String to) {
        Connection conn;
        int nodefrom = 0;
        int nodeto = 0;
        long milis = System.currentTimeMillis();
        String name = "path" + String.valueOf(milis);
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://158.196.143.25:5432/test";
            conn = DriverManager.getConnection(url, "test", "test");
            
            Statement s = conn.createStatement();
            ResultSet r = s.executeQuery("SELECT a.id municipalityid, b.id nodeid, ST_distance(a.geom,b.the_geom) AS distance FROM cr_wgs84.municipalities AS a, cr_wgs84.roads_vertices_pgr AS b WHERE ST_dwithin(a.geom,b.the_geom,2000) AND a.name = '" + from + "' ORDER BY distance LIMIT 1");
            while (r.next()) {
                nodefrom = r.getInt(2);
            }
            r = s.executeQuery("SELECT a.id municipalityid, b.id nodeid, ST_distance(a.geom,b.the_geom) AS distance FROM cr_wgs84.municipalities AS a, cr_wgs84.roads_vertices_pgr AS b WHERE ST_dwithin(a.geom,b.the_geom,2000) AND a.name = '" + to + "' ORDER BY distance LIMIT 1");
            while (r.next()) {
                nodeto = r.getInt(2);
            }
            s.execute("DROP TABLE IF EXISTS " + name);
            
            s.execute("CREATE TABLE " + name + " AS SELECT seq, route.id1 AS node, route.id2 AS edge, route.cost, edges.geom FROM pgr_dijkstra('\n"
                    + "   SELECT gid AS id,\n"
                    + "         source::integer,\n"
                    + "         target::integer,\n"
                    + "         reverse_cost::double precision AS cost\n"
                    + "   FROM cr_wgs84.roads',\n"
                    + nodefrom 
                    + ", "
                    + nodeto 
                    + ", false, false) \n"
                    + "AS route, cr_wgs84.roads AS edges \n"
                    + "WHERE route.id2 = edges.gid ORDER BY seq;");
            s.execute("SELECT UpdateGeometrySRID('" + name + "','geom',4326)");
            s.close();
            conn.close();
            publishPostGISTable(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String ret = "Name from to:" + from + " " + to + "\n";
        ret += "Id from to:" + nodefrom + " " + nodeto + "\n";
        ret += "http://localhost:8080/geoserver/crwgs84/wms?service=WMS&version=1.1.&request=GetCapabilities" + "\n";
        ret += "Layer name:" + name + "\n";
        return ret;
    }
    
    public String palmal() {
        int pocet_zaznamu = 0;
        String nazvy = "Nazvy: ";
        String nazvy2 = "Nazvy2: ";
        try {

            //SHP Read
            ShapefileDataStore sfds = new ShapefileDataStore(new URL("file:///data/install/geoserver/geoserver-2.7.2/data_dir/data/sf/archsites.shp"));
            SimpleFeatureSource fs = sfds.getFeatureSource("archsites");
            pocet_zaznamu = fs.getCount(new Query("archsites"));
            try {
                //FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
                Filter filter = CQL.toFilter("cat < 10");
                SimpleFeatureIterator sfi = fs.getFeatures(filter).features();
                
                while (sfi.hasNext()) {
                    SimpleFeature sf = sfi.next();
                    nazvy = nazvy + ";" + sf.getAttribute("str1");
                }
                
                SimpleFeatureType schema = fs.getSchema();
                for (int i = 0; i < schema.getAttributeDescriptors().size(); i++) {
                    nazvy = nazvy + " : " + schema.getAttributeDescriptors().get(i).getName();
                }
                
                CoordinateReferenceSystem crs = schema.getGeometryDescriptor().getCoordinateReferenceSystem();
                
                double distance = 10000.0d;
                GeometryFactory gf = new GeometryFactory();
                Point point = gf.createPoint(new Coordinate(600000, 4920000));
                FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
                Filter filter2 = ff.dwithin(ff.property("the_geom"), ff.literal(point), distance, "m");
                SimpleFeatureIterator sfi2 = fs.getFeatures(filter2).features();
                while (sfi2.hasNext()) {
                    SimpleFeature sf = sfi2.next();
                    //nazvy2 = nazvy2 + sf.getDefaultGeometryProperty().getType().toString();
                    nazvy2 = nazvy2 + ";" + sf.getAttribute("str1");
                }
                //nazvy2 = nazvy2 + " CRS:" + crs.getName().getCode();
                //nazvy2 = nazvy2 + " Filter2:" + filter2.toString();
                //nazvy2 += "Units: " + crs.getCoordinateSystem().getAxis(0).getUnit().toString();
                /*Map<String, Object> params = new HashMap<String, Object>();
                 params.put("dbtype", "postgis");
                 params.put("host", "localhost");
                 params.put("port", 5432);
                 params.put("schema", "public");
                 params.put("database", "database");
                 params.put("user", "postgres");
                 params.put("passwd", "postgres");
                 DataStore dataStore = DataStoreFinder.getDataStore(params);
                 */
                //return fs.getFeatures();
            } catch (CQLException ex) {
                Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex2) {
            Logger.getLogger(Examples.class.getName()).log(Level.SEVERE, null, ex2);
        }
        return pocet_zaznamu + ": " + nazvy + " " + nazvy2;
    }
}
