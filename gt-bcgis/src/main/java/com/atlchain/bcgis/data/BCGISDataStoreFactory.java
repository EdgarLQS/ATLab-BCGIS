package com.atlchain.bcgis.data;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.util.KVP;
import org.opengis.feature.simple.SimpleFeatureType;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

//  DataStoreFactorySpi 的接口
public class BCGISDataStoreFactory implements DataStoreFactorySpi {

    public BCGISDataStoreFactory() { }

    @Override
    public Map<RenderingHints.Key, ?> getImplementationHints() { return Collections.emptyMap(); }

    @Override
    public String getDisplayName() {
        return "BCGIS";
    }

    @Override
    public String getDescription() { return "WKB binary file"; }

    // @return <tt>true</tt> if and only if this factory is available to create DataStores.
    @Override
    public synchronized boolean isAvailable(){ return true; }

    // Parameter description of information required to connect
    public static final Param FILE_PARAM =
            new Param(
                    "file",
                    File.class,
                    "WKB binary file",
                    true,
                    null,
                    new KVP(Param.EXT, "wkb"));

    @Override
    public Param[] getParametersInfo() { return new Param[] { FILE_PARAM }; }

    //  check if a set of provided connection parameters can actually be used ,return true for connection parameters indicating a wkb file
    @Override
    public boolean canProcess(Map<String, Serializable> params) {
        try {
            File file = (File) FILE_PARAM.lookUp(params);
            if (file != null) {
                return file.getPath().toLowerCase().endsWith(".wkb");
            }
        } catch (IOException e) {
            // ignore as we are expected to return true or false
        }
        return false;
    }

    @Override
    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        File file = (File) FILE_PARAM.lookUp(params);
        return new BCGISDataStore(file);
    }

    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.wkb");
    @Override
    public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
       File file = (File)FILE_PARAM.lookUp(params);
       if(file.exists()){
        LOGGER.warning("File already exsists:" + file);
       }
        return  new BCGISDataStore(file);
    }
}
