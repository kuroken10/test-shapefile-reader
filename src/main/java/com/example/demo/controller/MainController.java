package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;

@Controller
public class MainController {

    public void start() {
        try {
            var shapefile = new ClassPathResource("mesh2/mesh2.shp").getFile();
            Map<String, Object> map = new HashMap<>();
            map.put("url", shapefile.toURI().toURL());

            DataStore dataStore = DataStoreFinder.getDataStore(map);
            String typeName = dataStore.getTypeNames()[0];

            FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);

            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();

            try (FeatureIterator<SimpleFeature> features = collection.features()) {
                while (features.hasNext()) {
                    SimpleFeature feature = features.next();
                    System.out.print(feature.getID());
                    System.out.print(": ");
                    System.out.println(feature.getDefaultGeometryProperty().getValue());
                    for (Property attribute : feature.getProperties()){
                        System.out.println("    " + attribute.getName() + ":" + attribute.getValue());
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
