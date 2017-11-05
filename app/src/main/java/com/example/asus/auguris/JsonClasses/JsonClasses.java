package com.example.asus.auguris.JsonClasses;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by asus on 4.11.2017.
 */

public class JsonClasses {

    public class Properties
    {
        private int weather;

        public int getWeather() { return this.weather; }

        public void setWeather(int weather) { this.weather = weather; }

        private int speed;

        public int getSpeed() { return this.speed; }

        public void setSpeed(int speed) { this.speed = speed; }

        private int severity;

        public int getSeverity() { return this.severity; }

        public void setSeverity(int severity) { this.severity = severity; }
    }

    public class Geometry
    {
        private String type;

        public String getType() { return this.type; }

        public void setType(String type) { this.type = type; }

        private ArrayList<Double> coordinates;

        public ArrayList<Double> getCoordinates() { return this.coordinates; }

        public void setCoordinates(ArrayList<Double> coordinates) { this.coordinates = coordinates; }
    }

    public class Feature
    {
        private String type;

        public String getType() { return this.type; }

        public void setType(String type) { this.type = type; }

        private Properties properties;

        public Properties getProperties() { return this.properties; }

        public void setProperties(Properties properties) { this.properties = properties; }

        private Geometry geometry;

        public Geometry getGeometry() { return this.geometry; }

        public void setGeometry(Geometry geometry) { this.geometry = geometry; }
    }

    public class RootObject
    {
        private String type;

        public String getType() { return this.type; }

        public void setType(String type) { this.type = type; }

        private ArrayList<Feature> features;

        public ArrayList<Feature> getFeatures() { return this.features; }

        public void setFeatures(ArrayList<Feature> features) { this.features = features; }
    }
}
