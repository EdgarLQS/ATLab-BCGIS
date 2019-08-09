package com.atlchain.bcgis.data;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geomgraph.GeometryGraph;
import org.locationtech.jts.io.WKBReader;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.IOException;

// 提供对DataStore进行访问
public class BCGISFeatureSource extends ContentFeatureSource {



    public BCGISFeatureSource(ContentEntry entry, Query query) {

        super(entry, query);
    }

    public BCGISDataStore getDataStore() {

        return (BCGISDataStore) super.getDataStore();
    }

    //   getReaderInternal( Query ) 提供一种流的方式去读取数据
    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
        return new BCGISFeatureReader(getState(), query);
    }

    // return the count for the geometry
    @Override
    protected int getCountInternal(Query query) throws IOException {
        if(query.getFilter() == Filter.INCLUDE){
            Geometry geometry = getDataStore().read();
            int count = geometry.getNumGeometries();
            return count;
        }
        return -1;
    }

    // TODO 返回图形边界 在外界打开显示时需要
    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
        return null;
    }

    // 获取哪些信息我们可用  数据库（database）中这个架构（schema)由 列（columns）来定义
    // 功能：将geometry解析出来到builder里面，然后存到 schema
    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(entry.getName());

        // 这里的DataStore是直接创建的  那么这个就相当于是新的 DataStore
        BCGISDataStore bcds = getDataStore();
        Geometry geometry = bcds.read();
        if (geometry == null) {
            throw new IOException("WKB file not available");
        }
        // 建立坐标系
        builder.setCRS(DefaultGeographicCRS.WGS84);
        String type = getGeometryTypeInGeometryCollection(geometry);
        // 根据不同的空间几何数据类型定义属性的数据类型
        switch (type) {
            case "Point":
                builder.add("geom", Point.class);
                break;
            case "MultiPoint":
                builder.add("geom", MultiPoint.class);
            case "LineString":
                builder.add("geom", LineString.class);
                break;
            case "MultiLineString":
                builder.add("geom", MultiLineString.class);
                break;
            case "Polygon":
                builder.add("geom", Polygon.class);
                break;
            case "MultiPolygon":
                builder.add("geom", MultiPolygon.class);
                break;
            default:
                break;
        }
        final SimpleFeatureType SCHEMA = builder.buildFeatureType();
        return SCHEMA;
    }

    // 返回 geometry.getGeometryN(0).getGeometryType(); 表示 type 类型
    private String getGeometryTypeInGeometryCollection(Geometry geometry) {
        if (geometry.getNumGeometries() > 0) {
            return geometry.getGeometryN(0).getGeometryType();
        }
        return null;
    }

    // new add    Make handleVisitor package visible allowing BCGISFeatureStore to delegate to this implementation
    @Override
    protected boolean handleVisitor(Query query, FeatureVisitor visitor) throws IOException{
        return super.handleVisitor(query,visitor);
        // WARNING: Please note this method is in FeatureSource!
    }
}
