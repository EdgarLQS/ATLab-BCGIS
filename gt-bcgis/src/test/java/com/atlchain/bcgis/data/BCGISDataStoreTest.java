package com.atlchain.bcgis.data;

import javafx.collections.MapChangeListener;
import org.geotools.data.*;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.junit.Test;
import org.junit.runner.FilterFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BCGISDataStoreTest {

    File file = new File("E:\\DemoRecording\\WkbCode\\Line.wkb");
    BCGISDataStore WKB = new BCGISDataStore(file);


    // 测试获取wkb的读取功能并打印出空间几何要素
    @Test
    public void testRead() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        Geometry geometry = WKB.read();
        String type = geometry.getGeometryType();

        System.out.println(geometry);
        System.out.println(type);

    }

    // 根据文档开始测试

    //测试获取wkb文件名
    @Test
    public void getTypeNames() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore store = DataStoreFinder.getDataStore(params);

        String names[] = store.getTypeNames();
        System.out.println("typenames: " + names.length);
        System.out.println("typename[0]: " + names[0]);
    }

    // Test DataStore.getSchema( typeName ) The method provides access to a FeatureType referenced by a type name
    @Test
    public void getSchema() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore store = DataStoreFinder.getDataStore(params);

        SimpleFeatureType type = store.getSchema(store.getTypeNames()[0]);                              // 为什么是0 ，可能是因为这里只存储了一个  后面应该多了就有了

        System.out.println("featureType  name: " + type.getName());
        System.out.println("featureType count: " + type.getAttributeCount());                           //返回有几个特征值

        for (AttributeDescriptor descriptor : type.getAttributeDescriptors()) {
            System.out.print("  " + descriptor.getName());
            System.out.print(" (" + descriptor.getMinOccurs() + "," + descriptor.getMaxOccurs() + ",");
            System.out.print((descriptor.isNillable() ? "nillable" : "manditory") + ")");
            System.out.print(" type: " + descriptor.getType().getName());
            System.out.println(" binding: " + descriptor.getType().getBinding().getSimpleName());
        }

        // access by index 因为现在主要只定义了 FeatureID
        AttributeDescriptor attributeDescriptor = type.getDescriptor(0);
        System.out.println("attribute 0    name: " + attributeDescriptor.getName());
        System.out.println("attribute 0    type: " + attributeDescriptor.getType().toString());
        System.out.println("attribute 0 binding: " + attributeDescriptor.getType().getBinding());

    }

    // Test DataStore.getFeatureReader( query, transaction )  The method allows access to the contents of our DataStore
    // 空间几何对象特征获取测试
    @Test
    public void getFeatureReader() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore datastore = DataStoreFinder.getDataStore(params);

        Query query = new Query(datastore.getTypeNames()[0]);

        System.out.println("open feature reader");
        FeatureReader<SimpleFeatureType, SimpleFeature> reader =
                datastore.getFeatureReader(query, Transaction.AUTO_COMMIT);
        try {
            int count = 0;
            while (reader.hasNext()) {
                SimpleFeature feature = reader.next();
                if (feature != null) {
                    System.out.println("  " + feature.getID() + " " + feature.getAttribute("geom"));
                    count++;
                }
            }
            System.out.println("close feature reader");
            System.out.println("read in " + count + " features");
        } finally {
            reader.close();
        }
    }

    // 可以进行查询
    // Example with a quick “selection” Filter:
    @Test
    public void Selection() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore datastore = DataStoreFinder.getDataStore(params);
        Query query = new Query(datastore.getTypeNames()[0]);

        FeatureReader<SimpleFeatureType,SimpleFeature> reader =
                datastore.getFeatureReader(query,Transaction.AUTO_COMMIT);

        try {
            while (reader.hasNext()){
                SimpleFeature feature = reader.next();
                if(reader.hasNext() == false) break;                                                // 增加if语句 跳出循环 防止空指针异常发生
//                System.out.println(feature);
                System.out.println(reader.hasNext());
                for (Property property : feature.getProperties()){
                    System.out.println("\t");
                    System.out.println( property.getName());
                    System.out.println("=");
                    System.out.println(property.getValue());
                }
            }
        } finally {
            reader.close();
        }
    }

    // Test DataStore.getFeatureSource( typeName )
    // 原测试里面的 CQL 查询测试 （未做） 原文是按照城市名，即属性查询，我这里没有怎么办，暂时不查询
    @Test
    public void getFeatureSource() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore datastore = DataStoreFinder.getDataStore(params);
        SimpleFeatureSource featureSource = datastore.getFeatureSource("Line");


    }

    // 问题：测试时会出现 全是true的情况 暂时未解决                // 暂时加了一个中断 后面怎么解决
    @Test
    public void FeatureCollection() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore datastore = DataStoreFinder.getDataStore(params);
        SimpleFeatureSource featureSource = datastore.getFeatureSource("Line");
        SimpleFeatureCollection featureCollection = featureSource.getFeatures();                // 获取所有属性到featureCollection里面

        List<String>list = new ArrayList<>();
        //FeatureCollection.features() - access to a FeatureIterator
        SimpleFeatureIterator features = featureCollection.features();
        while(features.hasNext()){
//                if(features.hasNext() == false) break;
//                System.out.println(features.hasNext());
                if (list.size() == 5) break;
            list.add(features.next().getID());
        }

        System.out.println("List Contents:" +  list);
        System.out.println("FeatureSource count :       " + featureSource.getCount(Query.ALL));     //返回多少个特征值
        System.out.println("FeatureSource bounds:       " + featureSource.getBounds(Query.ALL));    //返回边界查询
        System.out.println("FeatureCollection bounds:   " + featureCollection.size());;
        System.out.println("FeatureCollection bounds:   " + featureCollection.getBounds());

        // Load the feature into memory
        DefaultFeatureCollection collection = DataUtilities.collection(featureCollection);
        System.out.println("       Collection size:"  +collection.size());
    }


    // =====================2019.7.30 ===========前测试已完成（问题是两个hashNext有点问题需要解决）
    // ====================后面测试主要是涉及到加入 BCGISFeatureStore 和 BCGISFeatureWriter 之后的测试


    // check the result of getFeatureSource( typeName ) with the instanceof operator
    @Test
    public void FeatureStoreDemo() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore datastore = DataStoreFinder.getDataStore(params);
        SimpleFeatureSource featureSource = datastore.getFeatureSource("Line");

        if(!(featureSource instanceof SimpleFeatureStore)){
            try {
                throw new IllegalAccessException("Modification not supported");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        SimpleFeatureStore featureStore = (SimpleFeatureStore)featureSource;
        System.out.println(featureStore);
    }


    // FeatureStore 使用实例
    @Test
    public void FeatureStore() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore datastore = DataStoreFinder.getDataStore(params);

        Transaction t1 = new DefaultTransaction("transactions1");
        Transaction t2 = new DefaultTransaction("transactions2");

        SimpleFeatureType type = datastore.getSchema("Line");

        SimpleFeatureStore featureStore  = (SimpleFeatureStore)datastore.getFeatureSource("Line") ;
        SimpleFeatureStore featureStore1 = (SimpleFeatureStore)datastore.getFeatureSource("Line") ;
        SimpleFeatureStore featureStore2 = (SimpleFeatureStore)datastore.getFeatureSource("Line") ;

        featureStore1.setTransaction(t1);
        featureStore2.setTransaction(t2);

        System.out.println("Step 1");
        System.out.println("------");
        System.out.println("start    auto-commit:" + DataUtilities.fidSet(featureStore.getFeatures()));
//        System.out.println("start    auto-commit:" + DataUtilities.fidSet(featureStore1.getFeatures()));
//        System.out.println("start    auto-commit:" + DataUtilities.fidSet(featureStore2.getFeatures()));

        // 删除功能

        // 添加功能

        //提交事务1

        //提交事务2

    }








}