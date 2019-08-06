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

    File file = new File("E:\\DemoRecording\\WkbCode\\Line.wkb");
    BCGISDataStore WKB = new BCGISDataStore(file);

    // TODO  读取 wkb 文件并以 geometry 展示
    @Test
    public void testReadWkb(){
        File f2 = new File("E:\\DemoRecording\\WkbCode\\Line22.wkb");
        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(Paths.get(f2.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        WKBReader reader = new WKBReader();
        Geometry geometry = null;
        try {
            geometry = reader.read(fileBytes);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for(int i = 0; i <geometry.getNumGeometries();i++)
            System.out.println(geometry.getGeometryN(i));
        System.out.println(geometry.getNumGeometries());
    }


    // TODO 测试获取wkb的读取功能并打印出空间几何要素
    @Test
    public void testRead() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        File file = new File("E:\\DemoRecording\\WkbCode\\Line.wkb");
        BCGISDataStore WKB = new BCGISDataStore(file);
        Geometry geometry = WKB.read();
        for(int i = 0; i < geometry.getNumGeometries();i++)
            System.out.println(geometry.getGeometryN(i));
        System.out.println("=====the feature number is : " + geometry.getNumGeometries()+ "============");

    }

    // TODO 根据官方文档开始测试

    // TODO getTypeNames
    @Test
    public void getTypeNames() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore store = DataStoreFinder.getDataStore(params);

        String names[] = store.getTypeNames();
        System.out.println("typenames: " + names.length);
        System.out.println("typename[0]: " + names[0]);
    }

    // TODO Test DataStore.getSchema( typeName )
    // The method provides access to a FeatureType referenced by a type name
    @Test
    public void getSchema() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore store = DataStoreFinder.getDataStore(params);
        SimpleFeatureType type = store.getSchema(store.getTypeNames()[0]);                                              // 索引0是因为这里只存储了一个
        System.out.println("featureType  name: " + type.getName());
        System.out.println("featureType count: " + type.getAttributeCount());                                           //返回特征值

        // 提取特征值
        for (AttributeDescriptor descriptor : type.getAttributeDescriptors()) {
            System.out.print("  " + descriptor.getName());
            System.out.print(" (" + descriptor.getMinOccurs() + "," + descriptor.getMaxOccurs() + ",");
            System.out.print((descriptor.isNillable() ? "nillable" : "manditory") + ")");
            System.out.print(" type: " + descriptor.getType().getName());
            System.out.println(" binding: " + descriptor.getType().getBinding().getSimpleName());
        }

        // access by index （FeatureID）
        AttributeDescriptor attributeDescriptor = type.getDescriptor(0);
        System.out.println("\t");
        System.out.println("attribute 0    name: " + attributeDescriptor.getName());
        System.out.println("attribute 0    type: " + attributeDescriptor.getType().toString());
        System.out.println("attribute 0 binding: " + attributeDescriptor.getType().getBinding());

    }

    // TODO 空间几何对象特征获取测试
    // Test DataStore.getFeatureReader( query, transaction )  The method allows access to the contents of our DataStore
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

    // TODO 数据类型 gemo 和 属性值查询 Example with a quick “selection” Filter:
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
                for (Property property : feature.getProperties()){
                    System.out.println("\t");
                    System.out.println( property.getName() + " = " + property.getValue());
                }
            }
        } finally {
            reader.close();
        }
    }

    // TODO Test DataStore.getFeatureSource( typeName )
    // 原测试里面的 CQL 查询测试 （未做） 原文是按照城市名，即属性查询，我这里没有怎么办，暂时不查询
    @Test
    public void getFeatureSource() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore datastore = DataStoreFinder.getDataStore(params);
        SimpleFeatureSource featureSource = datastore.getFeatureSource("Line");

//        Filter filter = CQL.toFilter("Line1");
    }

    // TODO 边界查询
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


    // =====================2019.8.5 ===========前测试已完成
    // ====================后面测试主要是涉及到加入 BCGISFeatureStore 和 BCGISFeatureWriter 之后的测试


    // FeatureStore provides Transaction support and modification operations. FeatureStore is an extension of FeatureSource
    // TODO check the result of getFeatureSource( typeName ) with the instanceof operator
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
    }


    // TODO FeatureStore 使用实例
    // FeatureStore 定义
    // FeatureStore.addFeatures( featureReader)              FeatureStore.removeFeatures( filter )
    // FeatureStore.modifyFeatures( type, value, filter )    FeatureStore.modifyFeatures( types, values, filter )
    // FeatureStore.setFeatures( featureReader )             FeatureStore.setTransaction( transaction )
    @Test
    public void FeatureStore() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore datastore = DataStoreFinder.getDataStore(params);

        // 实现事务API接口  DefaultTransaction(String handle)  Quick implementation of Transaction api.
        Transaction t1 = new DefaultTransaction("transactions 1");
        Transaction t2 = new DefaultTransaction("transactions 2");
        // 得到属性值 可参考前面的 getSchema() 测试
        SimpleFeatureType type = datastore.getSchema("Line");
//        SimpleFeatureType type = datastore.getSchema(datastore.getTypeNames()[0]);  //        SimpleFeatureType type = datastore.getSchema("Line");

        //  getFeatureSource( typeName )由FeatureSource、FeatureStore或FeatureLocking实例提供
        //  the method is the gateway to our high level api, as provided by an instance of FeatureSource, FeatureStore or FeatureLocking
        SimpleFeatureStore featureStore  = (SimpleFeatureStore)datastore.getFeatureSource("Line") ;
        SimpleFeatureStore featureStore1 = (SimpleFeatureStore)datastore.getFeatureSource("Line") ;
        SimpleFeatureStore featureStore2 = (SimpleFeatureStore)datastore.getFeatureSource("Line") ;
        // 为Feat ureStore的修改操作事务  FeatureStore.setTransaction(Transaction transaction)  Provide a transaction for commit/rollback control of a modifying operation on this FeatureStore
        featureStore1.setTransaction(t1);
        featureStore2.setTransaction(t2);

        // TODO 获取 featureStore 上存在特征的 FeatureID (已完成)
        System.out.println("Step 1 ----- capture the featureID -----");
        System.out.println("------");
        // 获取 featureStore 上存在特征的 FeatureID  DataUtilities.fidSet(FeatureCollection<?,?> featureCollection) Copies the feature ids from each and every feature into a set
        System.out.println("start    auto-commit:" + DataUtilities.fidSet(featureStore.getFeatures()));                 // auto-commit”表示磁盘上文件的当前内容
        System.out.println("start    auto-commit:" + DataUtilities.fidSet(featureStore1.getFeatures()));
        System.out.println("start    auto-commit:" + DataUtilities.fidSet(featureStore2.getFeatures()));

        // TODO select feature to remove 根据给定的 FeatureID 删除(已完成)
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        Filter filter1 = ff.id(Collections.singleton(ff.featureId("Line.4")));
        featureStore1.removeFeatures(filter1);
        // 将 featureStore1 的值打印出来 =========== 可以打印出来
//        SimpleFeatureCollection featureCollectionremove = featureStore1.getFeatures();
//        SimpleFeatureIterator iteratorremove = featureCollectionremove.features();
//        while (iteratorremove.hasNext()) {
//            System.out.println("== remove =="+iteratorremove.next().toString());
//        }
        System.out.println();
        System.out.println("Step 2 transaction 1 removes feature 'fid1'");
        System.out.println("-------");
        System.out.println("t1 remove auto-commit:" + DataUtilities.fidSet(featureStore.getFeatures()));
        System.out.println("t1 remove          t1:" + DataUtilities.fidSet(featureStore1.getFeatures()));
        System.out.println("t1 remove          t2:" + DataUtilities.fidSet(featureStore2.getFeatures()));
        System.out.println();

        // TODO new feature to add!
        GeometryFactory gf = JTSFactoryFinder.getGeometryFactory();
        Point bb = gf.createPoint(new Coordinate(75,444));
        SimpleFeature feature = SimpleFeatureBuilder.build(type,new Object[]{bb},"Line11");
        SimpleFeatureCollection collection_add = DataUtilities.collection(feature);
        // addFeatures(FeatureCollection<T,F> featureCollection)  A list of FeatureIds is returned, one for each feature in the order created. However, these might not be assigned until after a commit has been performed.
//        featureStore2.addFeatures(featureStore2.getFeatures());
        featureStore2.addFeatures(collection_add);
        // 将 featureStore2 里面的元素值打印出来
//        SimpleFeatureCollection featureCollectionadd = featureStore2.getFeatures();
//        SimpleFeatureIterator iteratoradd = featureCollectionadd.features();
//        while (iteratoradd.hasNext()) {
//            System.out.println("== add ==" +iteratoradd .next().toString());
//        }

        System.out.println();
        System.out.println("Step  3 transaction 2 adds a new feature " + feature.getID()+"'");
        System.out.println("------");
        System.out.println("t2 add auto-commit:"+DataUtilities.fidSet(featureStore.getFeatures()));
        System.out.println("t2 add          t1:"+DataUtilities.fidSet(featureStore1.getFeatures()));
        System.out.println("t1 add          t2:"+DataUtilities.fidSet(featureStore2.getFeatures()));// 这一步对featureStore2 不产生变化
        System.out.println();

        // TODO 提交事务1(删除)和事务2（增加）
        t1.commit();
        System.out.println();
        System.out.println("Step 4 transaction 1 commits the removal of feature 'fid1'");
        System.out.println("------");
        System.out.println("t1 commit auto-commit: " + DataUtilities.fidSet(featureStore.getFeatures()));
        System.out.println("t1 commit          t1: " + DataUtilities.fidSet(featureStore1.getFeatures()));
        System.out.println("t1 commit          t2: " + DataUtilities.fidSet(featureStore2.getFeatures()));

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

    // TODO 获取 wkb 文件的空间几何要素
    @Test
    public void testRead1() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        File file = new File("E:\\DemoRecording\\WkbCode\\Line8.wkb");
        BCGISDataStore WKB = new BCGISDataStore(file);
        Geometry geometry = WKB.read();
        for(int i = 0; i < geometry.getNumGeometries();i++)
            System.out.println(geometry.getGeometryN(i));
        System.out.println("=====the feature number is : " + geometry.getNumGeometries()+ " ============");
    }

    // TODO removing all features
    // ---bug：当文件为空时会报错,需要在BCGISDataStore--read()里面在读取时添加信息防止报错
    @Test
    public void testFeatureWriter() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore datastore = DataStoreFinder.getDataStore(params);

        Transaction t = new DefaultTransaction("Line");
        try{
            FeatureWriter<SimpleFeatureType,SimpleFeature>writer =
                    datastore.getFeatureWriter("Line",Filter.INCLUDE,t);
            SimpleFeature feature ;
            try{
                while(writer.hasNext()){
                    feature = writer.next();
                    System.out.println("==== remove ==== " + feature.getID());
                    writer.remove();// Removes current Feature, must be called before hasNext.
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


    //  TODO completely replace all features  思路是先删除，然后增加feature   问题是最后加的元素并没有加入进去
    @Test
    public void TestFeatureWriter() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore datastore = DataStoreFinder.getDataStore(params);

        final SimpleFeatureType type = datastore.getSchema("Line");
        final FeatureWriter<SimpleFeatureType,SimpleFeature>writer;
        SimpleFeature f;
        DefaultFeatureCollection collection = new DefaultFeatureCollection();

        // new add
        SimpleFeatureCollection  featureCollection = datastore.getFeatureSource(datastore.getTypeNames()[0]).getFeatures();
        SimpleFeature simpleFeature = featureCollection.features().next();
        List<Object> obj = simpleFeature.getAttributes();
        WKTReader reader = new WKTReader();
        Geometry gemo = null;
        try {
            gemo = reader.read((String) obj.get(0));
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        MultiLineString multiLineString = (MultiLineString) obj.get(0);
//        System.out.println(">>>>>>>" + multiLineString);
        SimpleFeature bf = SimpleFeatureBuilder.build(type,new Object[]{ gemo },"Line.9");
        collection.add(bf);
        writer = datastore.getFeatureWriter("Line",Transaction.AUTO_COMMIT);
        try{
            // remove all features
//            while(writer.hasNext()){
//                writer.next();
//                writer.remove();
//            }
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

    // ===============暂时不写，目前DataStore里面的 createSchema 尚未实现
    // making a copy
    @Test
    public  void TestgetFeatureWriterAppend() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore datastore = DataStoreFinder.getDataStore(params);

        final SimpleFeatureType type = datastore.getSchema("Line");

    }

}