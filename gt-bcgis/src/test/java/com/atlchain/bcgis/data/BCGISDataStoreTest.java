package com.atlchain.bcgis.data;

import org.geotools.data.*;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.MultiCurve;
import org.junit.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.FeatureId;

import javax.jws.Oneway;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class BCGISDataStoreTest {

    // provide the file
    File file = new File("E:\\DemoRecording\\WkbCode\\Line.wkb");

    // TODO read wkb file into geometry
    @Test
    public void Read() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        File file = new File("E:\\DemoRecording\\WkbCode\\Line.wkb");
//        File file = new File("E:\\DemoRecording\\ATLab_BCGIS_Demo\\ATLab-BCGIS\\gt-bcgis\\duplicate.wkb");   //打印最后复制出来的文件
        BCGISDataStore WKB = new BCGISDataStore(file);
        Geometry geometry = WKB.read();
        for(int i = 0; i < geometry.getNumGeometries();i++)
            System.out.println(geometry.getGeometryN(i));
        System.out.println("=====the feature number is : " + geometry.getNumGeometries());
    }

    // TODO Start testing according to official documentation

    // TODO Test DataStore.getTypeNames()  The method getTypeNames provides a list of the available types
    @Test
    public void getTypeNames() throws IOException {

        // 两种获取 DataStore 的方式
        // 第一种  getDataStore(Map params) Checks each available datasource implementation in turn and returns the first one which claims to support the resource identified by the params object
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore datastore1 = DataStoreFinder.getDataStore(params);
        // 创建 BCGISDataStore
        DataStore datastore = new BCGISDataStore(file);

        String names[] = datastore.getTypeNames();
        System.out.println("typenames: " + names.length);
        System.out.println("typename[0]: " + names[0]);
    }

    // TODO Test DataStore.getSchema( typeName ) The method provides access to a FeatureType referenced by a type name
    @Test
    public void getSchema() throws IOException {
        DataStore datastore = new BCGISDataStore(file);

        SimpleFeatureType type = datastore.getSchema(datastore.getTypeNames()[0]);                                      // 索引0是因为这里只存储了一个
        System.out.println("featureType  name: " + type.getName());
        System.out.println("featureType count: " + type.getAttributeCount());                                           //返回特征值
        // SimpleFeatureType.getAttributeDescriptors() -- The list of attribute descriptors which make up the feature type.
        for (AttributeDescriptor descriptor : type.getAttributeDescriptors()) {
            System.out.print("  " + descriptor.getName());
            System.out.print(" (" + descriptor.getMinOccurs() + "," + descriptor.getMaxOccurs() + ",");
            System.out.print((descriptor.isNillable() ? "nillable" : "manditory") + ")");
            System.out.print(" type: " + descriptor.getType().getName());
            System.out.println(" binding: " + descriptor.getType().getBinding().getSimpleName());
        }
        // access by index （FeatureID） SimpleFeatureType.getDescriptor -- Returns the attribute descriptor which matches the specified name.
        AttributeDescriptor attributeDescriptor = type.getDescriptor(0);
        System.out.println("\t");
        System.out.println("attribute 0    name: " + attributeDescriptor.getName());
        System.out.println("attribute 0    type: " + attributeDescriptor.getType().toString());
        System.out.println("attribute 0 binding: " + attributeDescriptor.getType().getBinding());

    }

    // TODO Test DataStore.getFeatureReader( query, transaction )  The method allows access to the contents of our DataStore
    // 空间几何对象特征获取测试
    @Test
    public void getFeatureReader() throws IOException {
        DataStore datastore = new BCGISDataStore(file);
        // datastore.getTypeNames() -- Gets the names of feature types available in this DataStore.
        Query query = new Query(datastore.getTypeNames()[0]);
        // datastore.getFeatureReader(Query query, Transaction transaction) -- Gets a FeatureReader for features selected by the given Query.
        FeatureReader<SimpleFeatureType, SimpleFeature> reader = datastore.getFeatureReader(query, Transaction.AUTO_COMMIT);
        try {
            int count = 0;
            while (reader.hasNext()) {
                SimpleFeature feature = reader.next();
                if (feature != null) {
                    // SimpleFeature.getAttributes() -- Returns a list of the values of the attributes contained by the feature.
                    System.out.println("  " + feature.getID() + " " + feature.getAttribute("geom"));
                    count++;
                }
            }
            System.out.println("  read in " + count + " features");
        } finally {
            reader.close();
        }
    }

    // TODO Example with a quick “selection” Filter
    @Test
    public void Selection() throws IOException {
        DataStore datastore = new BCGISDataStore(file);
        Query query = new Query(datastore.getTypeNames()[0]);
        FeatureReader<SimpleFeatureType,SimpleFeature> reader = datastore.getFeatureReader(query,Transaction.AUTO_COMMIT);
        try {
            while (reader.hasNext()){
                SimpleFeature feature = reader.next();
                // SimpleFeature.getProperties() -- This method is a convenience method for calling (Collection) getValue().
                for (Property property : feature.getProperties()){
                    System.out.println( property.getName() + " = " + property.getValue());
                }
            }
        } finally {
            reader.close();
        }
    }

    // TODO Test DataStore.getFeatureSource( typeName )  -- Gets a SimpleFeatureSource for features of the type specified by a qualified name (namespace plus type name).
    // bug: wkb 文件只有空间信息无其他属性值，暂不支持 CQL 查询
    @Test
    public void getFeatureSource() throws IOException {
        DataStore datastore = new BCGISDataStore(file);
        SimpleFeatureSource featureSource = datastore.getFeatureSource("Line");
//        Filter filter = null;
//        try {
//            filter = CQL.toFilter("Line.1");
//        } catch (CQLException e) {
//            e.printStackTrace();
//        }

        // SimpleFeatureCollection	getFeatures() -- Retrieves all features in the form of a FeatureCollection.
        SimpleFeatureCollection features = featureSource.getFeatures();
        System.out.println("found : " + features.size() + " feature" );
        SimpleFeatureIterator iterator  = features.features();
        try{
            while(iterator.hasNext()){
                SimpleFeature feature = iterator.next();
                Geometry geometry = (Geometry)feature.getDefaultGeometry();
                System.out.println(feature.getID() + " default geometry " + geometry);
            }
        }catch(Throwable t){
            iterator.close();
        }
    }

    // TODO 边界查询
    @Test
    public void FeatureCollection() throws IOException {
        DataStore datastore = new BCGISDataStore(file);
        SimpleFeatureSource featureSource = datastore.getFeatureSource("Line");
        // SimpleFeatureCollection	getFeatures() -- Retrieves all features in the form of a FeatureCollection.
        SimpleFeatureCollection featureCollection = featureSource.getFeatures();

        List<String>list = new ArrayList<>();
        // FeatureCollection.features() - access to a FeatureIterator
        SimpleFeatureIterator features = featureCollection.features();
        while(features.hasNext()){
            list.add(features.next().getID());
        }
        System.out.println("List Contents:              " + list);
        System.out.println("FeatureSource count :       " + featureSource.getCount(Query.ALL));                         //返回多少个特征值
        System.out.println("FeatureSource bounds:       " + featureSource.getBounds(Query.ALL));                        //返回边界查询
        System.out.println("FeatureCollection bounds:   " + featureCollection.size());;
        System.out.println("FeatureCollection bounds:   " + featureCollection.getBounds());

        // DataUtilities.collection(SimpleFeature feature) -- Copies the provided features into a FeatureCollection.    复制 FeatureCollection
        DefaultFeatureCollection collection = DataUtilities.collection(featureCollection);
        System.out.println("");
        System.out.println("       Collection size:     "  +collection.size());
        System.out.println("       Collection size:     "  +collection.getBounds());
    }

    // TODO Test Using FeatureStore
    // ===2019.8.5 ====以下测试是加入 BCGISFeatureStore 和 BCGISFeatureWriter 之后的测试

    // TODO check the result of getFeatureSource( typeName ) with the instanceof operator
    // FeatureStore provides Transaction support and modification operations. FeatureStore is an extension of FeatureSource
    @Test
    public void FeatureStoreDemo() throws IOException {
        DataStore datastore = new BCGISDataStore(file);
        SimpleFeatureSource featureSource = datastore.getFeatureSource("Line");

        if(!(featureSource instanceof SimpleFeatureStore)){
            try {
                throw new IllegalAccessException("Modification not supported");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        SimpleFeatureStore featureStore = (SimpleFeatureStore)featureSource;
    }





    // TODO  Use  FeatureStore -- FeatureStore provides Transaction support and modification operations
    // FeatureStore.addFeatures( featureReader)                        FeatureStore.removeFeatures( filter )
    // FeatureStore.modifyFeatures( type, value, filter )              FeatureStore.modifyFeatures( types, values, filter )
    // FeatureStore.setFeatures( featureReader )                       FeatureStore.setTransaction( transaction )
    @Test
    public void FeatureStore() throws IOException {
        DataStore datastore = new BCGISDataStore(file);
        // DefaultTransaction(String handle) -- Quick implementation of Transaction api.
        Transaction t1 = new DefaultTransaction("transactions 1");
        Transaction t2 = new DefaultTransaction("transactions 2");

        // getSchema(String typeName) -- Gets the type information (schema) for the specified feature type.
        SimpleFeatureType type = datastore.getSchema("Line");

        // getFeatureSource( typeName ) -- the method is the gateway to our high level api, as provided by an instance of FeatureSource, FeatureStore or FeatureLocking
        SimpleFeatureStore featureStore  = (SimpleFeatureStore)datastore.getFeatureSource("Line") ;
        SimpleFeatureStore featureStore1 = (SimpleFeatureStore)datastore.getFeatureSource("Line") ;
        SimpleFeatureStore featureStore2 = (SimpleFeatureStore)datastore.getFeatureSource("Line") ;

        // FeatureStore.setTransaction(Transaction transaction) -- Provide a transaction for commit/rollback control of a modifying operation on this FeatureStore
        featureStore1.setTransaction(t1);
        featureStore2.setTransaction(t2);

        System.out.println("Step 1 ----- Copy the featureID from each and every feature into a set -----");
        System.out.println("------");
        // DataUtilities.fidSet(FeatureCollection<?,?> featureCollection) -- Copies the feature ids from each and every feature into a set
        System.out.println("start    auto-commit:" + DataUtilities.fidSet(featureStore.getFeatures()));                 // auto-commit”表示磁盘上文件的当前内容
        System.out.println("start    auto-commit:" + DataUtilities.fidSet(featureStore1.getFeatures()));
        System.out.println("start    auto-commit:" + DataUtilities.fidSet(featureStore2.getFeatures()));

        // TODO  select featureID to remove
//        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
//        Filter filter1 = ff.id(Collections.singleton(ff.featureId("Line.4")));
//        featureStore1.removeFeatures(filter1);
////        // 将 featureStore1 的值打印出来
////        SimpleFeatureCollection featureCollectionremove = featureStore1.getFeatures();
////        SimpleFeatureIterator iteratorremove = featureCollectionremove.features();
////        while (iteratorremove.hasNext()) {
////            System.out.println("== remove =="+iteratorremove.next().toString());
////        }
//        System.out.println();
//        System.out.println("Step 2 transaction 1 removes featureID 'Line.4'");
//        System.out.println("-------");
//        System.out.println("t1 remove auto-commit:" + DataUtilities.fidSet(featureStore.getFeatures()));
//        System.out.println("t1 remove          t1:" + DataUtilities.fidSet(featureStore1.getFeatures()));
//        System.out.println("t1 remove          t2:" + DataUtilities.fidSet(featureStore2.getFeatures()));
//        System.out.println();

        // TODO 3、new feature to add
        GeometryFactory gf = JTSFactoryFinder.getGeometryFactory();
        Point bb = gf.createPoint(new Coordinate(75,444));
        // build(SimpleFeatureType type, List<Object> values, String id) -- Static method to build a new feature.
        SimpleFeature feature = SimpleFeatureBuilder.build(type,new Object[]{bb},"Line11");
        SimpleFeatureCollection collection_add = DataUtilities.collection(feature);
        // addFeatures(FeatureCollection<T,F> featureCollection)  A list of FeatureIds is returned, one for each feature in the order created. However, these might not be assigned until after a commit has been performed.
//        featureStore2.addFeatures(featureStore2.getFeatures());
        featureStore2.addFeatures(collection_add);

        System.out.println();
        System.out.println("Step  3 transaction 2 adds a new featureID " + feature.getID()+ "'");
        System.out.println("------");
        System.out.println("t2 add auto-commit:"+DataUtilities.fidSet(featureStore.getFeatures()));
        System.out.println("t2 add          t1:"+DataUtilities.fidSet(featureStore1.getFeatures()));
        System.out.println("t1 add          t2:"+DataUtilities.fidSet(featureStore2.getFeatures()));
        System.out.println();

        // TODO 4、提交事务1(删除)和事务2（增加）
//        t1.commit();
//        System.out.println();
//        System.out.println("Step 4 transaction 1 commits the removal of featureID 'Line.4'");
//        System.out.println("------");
//        System.out.println("t1 commit auto-commit: " + DataUtilities.fidSet(featureStore.getFeatures()));
//        System.out.println("t1 commit          t1: " + DataUtilities.fidSet(featureStore1.getFeatures()));
//        System.out.println("t1 commit          t2: " + DataUtilities.fidSet(featureStore2.getFeatures()));
        t2.commit();
        System.out.println();
        System.out.println("Step 5 transaction 2 commits the addition of '" + feature.getID() + "'");
        System.out.println("------");
        System.out.println("t2 commit auto-commit: " + DataUtilities.fidSet(featureStore.getFeatures()));
        System.out.println("t2 commit          t1: " + DataUtilities.fidSet(featureStore1.getFeatures()));
        System.out.println("t2 commit          t2: " + DataUtilities.fidSet(featureStore2.getFeatures()));

        // Frees all State held by this Transaction.
        t1.close();
        t2.close();
        datastore.dispose();
    }

    // TODO removing all features   bug: 采用hasNext()的方式进行删除，最后会留一行
    @Test
    public void FeatureWriter() throws IOException {
        DataStore datastore = new BCGISDataStore(file);

        Transaction t = new DefaultTransaction("Line");
        try{
            // getFeatureWriter(String typeName, Transaction transaction) -- Gets a FeatureWriter to modify features in this DataStore.
            FeatureWriter<SimpleFeatureType,SimpleFeature>writer = datastore.getFeatureWriter("Line",Filter.INCLUDE,t);
            SimpleFeature feature ;
            try{
                writer.next();
                while(writer.hasNext()){
                    feature = writer.next();
                    System.out.println("==== remove ==== " + feature.getID());
                    // Removes current Feature, must be called before hasNext.
                    writer.remove();
                }
            }finally {
                writer.close();
            }
            System.out.println("commit " + t);
            t.commit();
        }catch(Throwable eek){
            t.rollback();
        }finally {
            t.close();
            datastore.dispose();
        }
    }

    //  TODO completely replace all features  先删除全部，然后增加 feature
    @Test
    public void FeatureWriter_remove() throws IOException {
        DataStore datastore = new BCGISDataStore(file);

        final SimpleFeatureType type = datastore.getSchema("Line");
        final FeatureWriter<SimpleFeatureType,SimpleFeature>writer;
        DefaultFeatureCollection collection = new DefaultFeatureCollection();

        // new add Point
        GeometryFactory gf = JTSFactoryFinder.getGeometryFactory();
        Point boston = gf.createPoint(new Coordinate(-799.0589, 42.3601));
        SimpleFeature bf = SimpleFeatureBuilder.build(type, new Object[] {boston}, "Line.33");
        collection.add(bf);
        // getFeatureWriter(String typeName, Transaction transaction) -- Gets a FeatureWriter to modify features in this DataStore.
        writer = datastore.getFeatureWriter("Line",Transaction.AUTO_COMMIT);
        try{
            // remove all features
            while(writer.hasNext()){
                writer.next();
                writer.remove();
            }
            // copy new features in
            SimpleFeatureIterator iterator = collection.features();
            while(iterator.hasNext()){
                SimpleFeature feature = iterator.next();
                SimpleFeature newFeature = writer.next();//new blank feature
                newFeature.setAttributes(feature.getAttributes());
                writer.write();
            }
        }finally{
            writer.close();
        }
    }

    //  TODO making a copy   bug：因为新加了一个点 所以复制出来的文件里面包含（0 0）这个点
    @Test
    public  void getFeatureWriterAppend() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put( "file", file);
        DataStore datastore = DataStoreFinder.getDataStore(params);
        SimpleFeatureType featuretype = datastore.getSchema("Line");

        File directory = null;
        File file2 = new File(directory,"duplicate.wkb");
        Map<String, Serializable> params2 = new HashMap<>();
        params2.put("file",file2);

        BCGISDataStoreFactory factory = new BCGISDataStoreFactory();
        DataStore duplicate = factory.createNewDataStore(params2);
        duplicate.createSchema(featuretype);

        FeatureReader<SimpleFeatureType, SimpleFeature> reader;
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer;
        SimpleFeature feature, newFeature;

        Query query = new Query(featuretype.getTypeName(),Filter.INCLUDE);
        reader = datastore.getFeatureReader(query,Transaction.AUTO_COMMIT);

        // 以下两种方法都可以
        // getFeatureWriterAppend(String typeName, Transaction transaction) -- Gets a FeatureWriter that can add new features to the DataStore.
        writer = duplicate.getFeatureWriterAppend("duplicate",Transaction.AUTO_COMMIT);
        // getFeatureWriter(String typeName, Transaction transaction) -- Gets a FeatureWriter to modify features in this DataStore.
//         writer = duplicate.getFeatureWriter("duplicate", Transaction.AUTO_COMMIT);
        try {
            while(reader.hasNext()) {
                feature = reader.next();
                newFeature = writer.next();
                newFeature.setAttributes(feature.getAttributes());
                writer.write();
            }
        }finally{
            reader.close();
            writer.close();
        }
    }
}