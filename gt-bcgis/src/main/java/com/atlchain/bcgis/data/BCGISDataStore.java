package com.atlchain.bcgis.data;

import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BCGISDataStore extends ContentDataStore {
    protected File file;

    // be working with a single WKB file
    public BCGISDataStore(File file) {

        this.file = file;
    }

    // ADD the reader
    Geometry read() throws IOException {
        WKBReader reader = new WKBReader();
        Geometry geometry = null;
        try {
            geometry = reader.read(Files.readAllBytes(Paths.get(file.getPath())));
//            geometry = new Shp2Wkb("Line.shp").getGeometry();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return geometry;
    }

    @Override
    protected List<Name> createTypeNames() throws IOException {
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf('.'));

        Name typeName = new NameImpl(name);
        return Collections.singletonList(typeName);
    }

    // TODO   未完成
    // the createSchema(SimpleFeatureType featureType) method used to set up a new file  目的是建立一个新文件
    // 把属性等保存到新文件
    @Override
    public void createSchema(SimpleFeatureType featureType) throws IOException {

//        List<String> builder = new ArrayList<>();
//        GeometryDescriptor geometryDescriptor = featureType.getGeometryDescriptor();
//
//        // 判断语句对geometryDescriptor进行判断看是不是我们要的东西
//        if(geometryDescriptor != null
//                && CRS.equalsIgnoreMetadata(DefaultGeographicCRS.WGS84,
//                geometryDescriptor.getCoordinateReferenceSystem())
//                && geometryDescriptor.getType().getBinding().isAssignableFrom(Point.class)){
//        }else{
//            throw new IOException("Unable use to represent" + geometryDescriptor);
//        }
//
//        for(AttributeDescriptor descriptor : featureType.getAttributeDescriptors()){
//            if(descriptor instanceof  GeometryDescriptor)continue;
//            builder.add(descriptor.getLocalName());
//        }
//
//        WKBWriter writer = new WKBWriter();
//        byte[] WKBByteArray = writer.write(null);//建立新文件 在Shp2Wkb中可借鉴  后期在考虑

    }


    // 返回 BCGISFeatureStore 和 BCGISFeatureSource
    @Override
    protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        if(file.canWrite()){
            return new BCGISFeatureStore(entry,Query.ALL);
        }else{
            return new BCGISFeatureSource(entry,Query.ALL);
        }
        //return new BCGISFeatureSource(entry, Query.ALL);
    }
}