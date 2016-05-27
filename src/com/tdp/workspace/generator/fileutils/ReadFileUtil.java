package com.tdp.workspace.generator.fileutils;

import com.tdp.workspace.generator.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NavigableSet;
import java.util.Scanner;
import java.util.TreeSet;

/**
 * Created by Siarhei Nahel on 21.05.2016.
 */
public class ReadFileUtil {
    public static NavigableSet<String> getAllTDPModulesFromProjectDir(String path){
        File projectDir = new File(path);
        NavigableSet<String> allModules = new TreeSet<>();
        File[] modules = projectDir.listFiles();
        for (File module : modules){
            String name = module.getName();
            if (module.isDirectory() && name.contains("000")){
                allModules.add(name);
            }
        }
        return allModules;
    }

    public static NavigableSet<String> getModuleNames(File depFile) throws FileNotFoundException {
        NavigableSet<String> modules = new TreeSet<String>();
        Scanner scanner = new Scanner(depFile);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains(Constants.HEAD) && !line.contains("#")) {
                String moduleName = line.replace(Constants.HEAD, "");
                modules.add(moduleName.trim());
            }
        }
        scanner.close();
        return modules;
    }
}
