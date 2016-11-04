package com.tdp.workspace.generator.fileutils;

import com.tdp.workspace.generator.Constants;

import java.io.*;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.TreeSet;

/**
 * Created by Siarhei Nahel on 21.05.2016.
 */
public class TdpPluginPropertiesReader {
    private static TdpPluginPropertiesReader instance;
    private String pathToProperties;
    private Properties properties;

    private TdpPluginPropertiesReader(String path) {
        this.pathToProperties = path + Constants.PROPERTIES_FILE;
    }

    public static TdpPluginPropertiesReader getInstance(String projectPath) throws IOException {
        if (instance == null){
            instance = new TdpPluginPropertiesReader(projectPath);
            instance.init();
        }
        return instance;
    }

    private void init() throws IOException {
        File prFile = new File(pathToProperties);
        if (!prFile.exists()){
            createDefault(pathToProperties);
        }
        FileInputStream ins = new FileInputStream(prFile);
        properties = new Properties();
        properties.load(ins);
    }

    private void createDefault(String pathToProperties) throws IOException {
        File prop = new File(pathToProperties);
        FileWriter fileWriter = new FileWriter(prop, false);
        fileWriter.append("sourceDirs=/src/java;/src/main/java\n" +
                "generatedSourceDir=/xsd_gen\n" +
                "testSourceDirs=/src/test/java\n" +
                "testResourcesDirs=/src/test/resources\n" +
                "resourcesDirs=\n" +
                "baseModules=\n"+
                "artifactoryDep=");
        fileWriter.close();
    }

    public NavigableSet<String> getSourceDirs(){
        return getValues("sourceDirs");
    }

    public NavigableSet<String> getGeneratedDirs(){
        return getValues("generatedSourceDir");
    }

    public NavigableSet<String> getTestSourceDirs(){
        return getValues("testSourceDirs");
    }

    public NavigableSet<String> getResourcesDirs() {
        return getValues("resourcesDirs");
    }

    public NavigableSet<String> getTestResourcesDirs(){
        return getValues("testResourcesDirs");
    }

    public String getBaseModules() {
        return properties.getProperty("baseModules");
    }

    private NavigableSet<String> getValues(String key){
        NavigableSet<String> result = new TreeSet<>();
        if (properties.containsKey(key)){
            String line = properties.getProperty(key);
            if (line == null){
                return result;
            }
            String[] dirs = line.split(Constants.SEMICOLON);
            for (String dir : dirs) {
                result.add(dir.trim());
            }
        }
        return result;
    }

    public void setProperty(String key, String value) throws IOException {
        properties.setProperty(key, value);
        File prop = new File(pathToProperties);
        FileWriter fileWriter = new FileWriter(prop, false);
        properties.store(fileWriter, null);
        fileWriter.flush();
        fileWriter.close();
    }

    public boolean getArtifactoryDep() {
        String artifDep = properties.getProperty("artifactoryDep");
        return artifDep ==null || artifDep.isEmpty() ? false : Boolean.parseBoolean(artifDep);
    }
}