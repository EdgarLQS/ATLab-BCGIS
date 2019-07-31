package com.atlchain.bcgis.data;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKBReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.util.NoSuchElementException;

public class BCGISFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    //  State used when reading file
    protected ContentState state;

    protected Geometry geometry;

    // new add 在BCGISFeatureWriter会用到
    protected WKBReader reader;

    protected SimpleFeatureBuilder builder;

    protected GeometryFactory geometryFactory;

    private int index = 0;

    // new add
    private GeometryCollectionIterator iterator;

    public BCGISFeatureReader(ContentState contentState, Query query) throws IOException {
        this.state = contentState;
        BCGISDataStore bcgisDataStore = (BCGISDataStore) contentState.getEntry().getDataStore();
        geometry = bcgisDataStore.read();
        iterator = new GeometryCollectionIterator(geometry);
        builder = new SimpleFeatureBuilder(state.getFeatureType());
        geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
    }

    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
        Geometry _geom = (Geometry) iterator.next();
        // ！表示反的意思 iterator.hasNext()返回false则！false = trye 则关闭借宿后面迭代计算下一个没有了就关闭
        if (! iterator.hasNext()) {
            close();
            return null;
        }
        //和上面那重复定义
        Geometry geom = (Geometry) iterator.next();

        // new add feature（后面返回需要）
        SimpleFeature feature = getFeature(geom);
        return feature;
    }

    //  检查下一个元素是否有，若有则返回true  应用到next()中
    @Override
    public boolean hasNext() throws IOException {

        return iterator.hasNext();
    }

    @Override
    public SimpleFeatureType getFeatureType() {

        return (SimpleFeatureType) state.getFeatureType();
    }

    // getFeature(geom) 应用到上面
    // 将空间几何数据geome存入到 builder 中  并写入它的 ID 为 index
    private SimpleFeature getFeature(Geometry geometry) throws IOException {
        if(geometry == null){
            System.out.println("JJJJJJJJJJJJJJ");
           throw new IOException("FeatureReader is closed;no additional feature can be");
        }
        index ++;
        builder.set("geom", geometry);
        return builder.buildFeature(state.getEntry().getTypeName() + "." + index);
    }

    @Override
    public void close() throws IOException {
        //new add if 语句 关闭FeatureReader
        builder = null;
        geometryFactory = null;
    }
}
