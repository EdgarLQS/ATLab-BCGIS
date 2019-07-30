package com.atlchain.bcgis.data;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;
import org.opengis.feature.Feature;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.cs.AxisDirection;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.NoSuchElementException;

// We will be outputting content to a temporary file, leaving the original for concurrent processes such as rendering.
// When streaming is closed the temporary file is moved into the correct location to effect the change.
// Iterator supporting writing of feature content
public class BCGISFeatureWriter implements FeatureWriter<SimpleFeatureType, SimpleFeature> {

    // State of current transaction
    private ContentState state;

    // Delegate handing reading of original file
    private BCGISFeatureReader delegate;

    // Temporary file used to stage output
    private File temp ;

    // wkbWriter used for temp file output
    private WKBWriter wkbWriter;

    // Current feature available for modification, may be null if feature removed
    private SimpleFeature currentFeature;

    // Flag indicating we have reached the end of the file
    private boolean appending = false;

    // Row count used to generate FeatureId when appending
    int nextRow = 0 ;


    // TODO
    // 实现功能  建立临时文件、创造wkbWriter输出、复制一个文件为了临时的增加功能、实现委托以读取原始文件
    // new add
    public BCGISFeatureWriter(ContentState state, Query query) throws  IOException{

        this.state = state;
        String typename = query.getTypeName();
        File file = ((BCGISDataStore)state.getEntry().getDataStore()).file;         // 修改原BCGISDataStore里面的file 为protected（原为pritvate）
        File directory = file.getParentFile();
        // 设置临时文件temp
        this.temp = File.createTempFile(typename+System.currentTimeMillis(),"wkb",directory);
        // wkbWriter输出
        this.wkbWriter = new WKBWriter();                                           // 和原来不一样
        // 实现委托以读取原始文件
        this.delegate = new BCGISFeatureReader(state,query);
        this.wkbWriter.write(delegate.geometry);                                     // 和原来不一样
    }

    // Add FeatureWriter.getFeatureType() implementation
    @Override
    public SimpleFeatureType getFeatureType() {

        return state.getFeatureType();
    }

    // new add  making use of delegate before switching over to returning false when appending
    @Override
    public boolean hasNext() throws IOException {
        if(wkbWriter == null){
            return false;
        }
        if(this.appending){
            return false;
        }
        return delegate.hasNext();
    }

    // new add
    // To access Features for modification or removal (when working through existing content)
    // To create new Features (when working past the end of the file)
    @Override
    public SimpleFeature next() throws IOException, IllegalAttributeException, NoSuchElementException {
        if(wkbWriter == null ){
            throw new IOException("FeatureWriter has been closed");
        }
        if(this.currentFeature != null){
            this.write();
        }
        try{
            if(!appending){
                if(delegate.reader != null && delegate.hasNext()){
                    this.currentFeature = delegate.next();
                    return  this.currentFeature;
                }else{
                    this.appending = true;
                }
            }
            SimpleFeatureType featureType = state.getFeatureType();
            String fid = featureType.getTypeName() + "." + nextRow;
            Object values[] = DataUtilities.defaultValues(featureType);

            this.currentFeature = SimpleFeatureBuilder.build(featureType,values,fid);
            return  this.currentFeature;
        }catch (IllegalAttributeException invalid){
            throw new IOException("Unable to create feature :" + invalid.getMessage(),invalid);
        }
    }

    // new add  marking the currentFeature as null.
    @Override
    public void remove() throws IOException {
        this.currentFeature = null;
    }

    // TODO
    // new add  wkb的写入需要空间几何信息才行。这里没有
    @Override
    public void write() throws IOException {
        if(this.currentFeature == null){
            return;
        }

        // 这里的工作是把坐标写入到文件里面
        // csv文件是lat和lon好写，但是这里 wkbWriter 要写的是geometry文件 看如何处理
        for(Property property:currentFeature.getProperties()){
            Object value = property.getValue();

            if(value == null){
                this.wkbWriter.write(null);
            }else if(value instanceof Point){
                Geometry geometry = (Geometry)value;

            }
        }
        nextRow++;
        this.currentFeature = null ;
    }

    // new add   replace the existing File with our new one
    @Override
    public void close() throws IOException {
        if(wkbWriter == null ){
            throw new IOException("Write alread closed");
        }
        if(this.currentFeature != null){
         this.write();
        }
        //Step 1: Write out remaining contents (if applicable)
        while (hasNext()){
            next();
            write();
        }
        wkbWriter = null ;

        if(delegate != null){
            this.delegate.close();
            this.delegate = null;
        }
        // Step 2: Replace file contents
        File file = ((BCGISDataStore)state.getEntry().getDataStore()).file;

        Files.copy(temp.toPath(),file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

}

