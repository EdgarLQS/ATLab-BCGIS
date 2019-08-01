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

    // used in the generation of FeatureId.
    private int index ;

    protected Geometry geometry;

    // Utility class used to build features
    protected SimpleFeatureBuilder builder;

    // Factory class for geometry creation
    protected GeometryFactory geometryFactory;

    public BCGISFeatureReader(ContentState contentState, Query query) throws IOException {
        this.state = contentState;
        BCGISDataStore bcgisDataStore = (BCGISDataStore) contentState.getEntry().getDataStore();
        builder = new SimpleFeatureBuilder(state.getFeatureType());
        geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        index = 0 ;
        geometry = bcgisDataStore.read();
    }

    // Access FeatureType (documenting available attributes)
    @Override
    public SimpleFeatureType getFeatureType() {

        return (SimpleFeatureType) state.getFeatureType();
    }

    // new add The next feature
    private SimpleFeature next;
    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
        SimpleFeature feature;
        if(next != null){
            feature = next;
            next = null;
        }else{
            // 将geometry里面的元素根据index索引值取出来赋值给geom 然后获取getfeature
            Geometry geom = geometry.getGeometryN(index);
            feature = getFeature(geom);
        }
        return feature;
    }

    //  return true or false to the next()
    @Override
    public boolean hasNext() throws IOException {
        if (index < geometry.getNumGeometries()){
            return true;
        }else if(geometry == null){
            return  false;
        } else{
            next = getFeature(geometry);
            return false;
        }
    }

    // getFeature(geom) 将geome存入 builder 中  并记 ID 为 index
    private SimpleFeature getFeature(Geometry geometry) throws IOException {
        if(geometry == null){
           throw new IOException("FeatureReader is closed;no additional feature can be");
        }
        index ++;
        builder.set("geom", geometry);
        return builder.buildFeature(state.getEntry().getTypeName() + "." + index);
    }

    @Override
    public void close() throws IOException {
        builder = null;
        geometryFactory = null;
        next = null;
    }
}
