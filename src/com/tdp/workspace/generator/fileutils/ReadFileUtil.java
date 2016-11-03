package com.tdp.workspace.generator.fileutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Scanner;
import java.util.TreeSet;

/**
 * Created by Siarhei Nahel on 21.05.2016.
 */
public class ReadFileUtil {

    private static List<String> jars = new ArrayList<>();
    private static final int LENGTH_MODULE_NAME = 8;
    private static final String START_MODULE_NAME_PATTERN = "00";

    public static NavigableSet<String> getAllTDPModulesFromProjectDir(String path){
        File projectDir = new File(path);
        NavigableSet<String> allModules = new TreeSet<>();
        File[] modules = projectDir.listFiles();
        for (File module : modules){
            String name = module.getName();
            if (module.isDirectory() && name.startsWith(START_MODULE_NAME_PATTERN)){
                allModules.add(name);
            }
        }
        return allModules;
    }

    public static NavigableSet<String> getModuleNames(File depFile) throws FileNotFoundException {
        NavigableSet<String> modules = new TreeSet<String>();
        Scanner scanner = new Scanner(depFile);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith(START_MODULE_NAME_PATTERN)) {
                String moduleName;
                if (line.length() > LENGTH_MODULE_NAME) {
                    moduleName = line.substring(0, LENGTH_MODULE_NAME);
                } else {
                    moduleName = line;
                }
//                System.out.println(moduleName);
                modules.add(moduleName.trim());
            }
        }
        scanner.close();
        return modules;
    }

    public static List<String> getJars(String path){
        File file = new File(path + "/projectoutput");
        if (file.exists() && file.isDirectory()) {
            generateJars(file);
        }
        return jars;
    }

    private static void generateJars(File dir) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                generateJars(file);
            } else {
                if (file.getName().endsWith(".jar")) {
                    jars.add(file.getAbsolutePath());
                }
            }
        }
    }
}