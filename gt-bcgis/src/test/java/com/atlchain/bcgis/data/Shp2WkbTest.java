package com.atlchain.bcgis.data;

import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.GeometryCollection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// 读取shp文件，然后保存为wkb文件
public class Shp2WkbTest {
    String shpURL = this.getClass().getResource("/Line/Line.shp").getPath();
    File shpFile = new File(shpURL);
    Shp2Wkb shp2WKB = new Shp2Wkb(shpFile);

    @Test
    public void testGetRightGeometryCollectionType() {
        try {
            Assert.assertEquals(GeometryCollection.class, shp2WKB.getGeometry().getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetRightGeometryValue() {
        try {
            GeometryCollection geometryCollection = shp2WKB.getGeometry();
            Assert.assertEquals(5, geometryCollection.getNumGeometries());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 将shp文件转为WKB文件 并保存到指定位置
    @Test
    public void testBoolean(){
        try {
            boolean bool = shp2WKB.save(new File("E:\\DemoRecording\\WkbCode\\Line.wkb"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}