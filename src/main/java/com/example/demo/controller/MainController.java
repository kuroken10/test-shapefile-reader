package com.example.demo.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDumper;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;

@Controller
public class MainController {

    public void start() {
        try {
            // PROJ4J での変換準備
            CRSFactory crsFactory = new CRSFactory();
            CoordinateReferenceSystem wgs84 = crsFactory.createFromName("epsg:4326");
            CoordinateReferenceSystem tky = crsFactory.createFromName("epsg:4301");
            CoordinateTransformFactory transformFactory = new CoordinateTransformFactory();
            CoordinateTransform transform = transformFactory.createTransform(wgs84, tky);

            // シェープファイル読み込み
            var shapefile = new ClassPathResource("mesh2/mesh2.shp").getFile();
            Map<String, Object> map = new HashMap<>();
            map.put("url", shapefile.toURI().toURL());

            DataStore dataStore = DataStoreFinder.getDataStore(map);
            String typeName = dataStore.getTypeNames()[0];
            FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();

            // 測地系変換後のfeatureを格納する変数
            DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();

            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

            try (FeatureIterator<SimpleFeature> features = collection.features()) {
                while (features.hasNext()) {
                    SimpleFeature feature = features.next();

                    // 座標情報取得
                    var mp = (MultiPolygon) feature.getDefaultGeometryProperty().getValue();
                    Coordinate[] coords = mp.getCoordinates();
                    // 各座標ごとに変換
                    List<Coordinate> transformedList = new ArrayList<>();
                    for (Coordinate coord : coords) {
                        var result = new ProjCoordinate();
                        transform.transform(new ProjCoordinate(coord.x, coord.y), result);
                        transformedList.add(new Coordinate(result.x, result.y));
                    }
                    Coordinate[] arr = transformedList.toArray(new Coordinate[transformedList.size()]);
                    var polygon = geometryFactory.createPolygon(arr);

                    // 変換後の座標をセット
                    feature.setDefaultGeometry(polygon);
                    featureCollection.add(feature);
                }
            }

            // シェープファイルへ書き出しß
            ShapefileDumper dumper = new ShapefileDumper(new File("./output"));
            dumper.dump(featureCollection);

            System.out.println("---end---");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
