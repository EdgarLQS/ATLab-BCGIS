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

    // 根据 getDataStore() 强制转化为BCGISDataStore
    public BCGISDataStore getDataStore() {

        return (BCGISDataStore) super.getDataStore();
    }

    //   getReaderInternal( Query ) 提供一种流的方式去读取数据
    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
        return new BCGISFeatureReader(getState(), query);
    }

    /**
     * 根据查询条件查询属性条数
     * @param query 查询条件
     * @return 符合条件的属性条数，-1则表示不能计算该条件的数量，需要外部用户自己计算。
     * @throws IOException
     */
    @Override
    protected int getCountInternal(Query query) throws IOException {
        if(query.getFilter() == Filter.INCLUDE){
            Geometry gemotry = getDataStore().read();
            //返回几何对象gemotry里面有多少个几何对象 直接采用方法进行返回
            int count = gemotry.getNumGeometries();
            return count;
        }
        return -1;
    }

    // 返回该数据格式的边界   后面需要裁减时会用到
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

        Geometry geometry = getDataStore().read();
        if (geometry == null) {
            throw new IOException("WKB file not available");
        }
        // 建立坐标系
        builder.setCRS(DefaultGeographicCRS.WGS84);
//        builder.add("geom", String.class);
        String type = getGeometryTypeInGeometryCollection(geometry);

        // 根据不同的空间几何数据类型定义属性的数据类型 ============其实这里可以改写就是 我把每一个属性值单独写出来（有函数实现），然后再依次判断并写入，最后保存
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


        // 返回SCHEMA(架构)，即根据列定义的数据库可调用
        final SimpleFeatureType SCHEMA = builder.buildFeatureType();
        return SCHEMA;
    }

    // 首先将geometry转化为迭代器，然后每次获取其中一个属性值，然后得出
    private String getGeometryTypeInGeometryCollection(Geometry geometry) {
        GeometryCollectionIterator geometryCollectionIterator = new GeometryCollectionIterator(geometry);
        geometryCollectionIterator.next();
        Geometry geom = (Geometry) geometryCollectionIterator.next();
        // 获取该几何对象的创建形式
        return geom.getGeometryType();
    }

    // 和 BCGISFeatureStore 建立委托关系，两者有相同的代码
    // new add    Make handleVisitor package visible allowing BCGISFeatureStore to delegate to this implementation
    @Override
    protected boolean handleVisitor(Query query, FeatureVisitor visitor) throws IOException{
        return super.handleVisitor(query,visitor);
        // WARNING: Please note this method is in CSVFeatureSource!
    }

}
