package com.tdp.workspace.generator.utils;

import com.tdp.workspace.generator.Constants;
import com.tdp.workspace.generator.fileutils.ReadFileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Siarhei_Nahel on 10/22/2016.
 */
public class ModulesDepUtil {
    private static ModulesDepUtil instance;
    private String projectPath;
    private Map<String, NavigableSet<String>> allModulesWithDeps = new TreeMap<>();
    private Map<String, String> artifactoryModules = new TreeMap<>();

    private ModulesDepUtil(String projectPath) {
        this.projectPath = projectPath;
    }

    public static ModulesDepUtil getInstance(String projectPath) throws FileNotFoundException {
        if (instance == null) {
            instance = new ModulesDepUtil(projectPath);
            instance.generate();
        }
        return instance;
    }

    private void generate() throws FileNotFoundException {
        NavigableSet<String> allModules = ReadFileUtil.getAllTDPModulesFromProjectDir(projectPath);
        for (String module : allModules) {
            String pathToBuild = projectPath + Constants.SLASH + module + Constants.SLASH
                    + Constants.BUILD_DIR;
            String pathDepFile = nameDepFile(pathToBuild);
            File fileDep = new File(pathDepFile);
            if (!fileDep.exists()) {
                allModulesWithDeps.put(module, new TreeSet<>());
                continue;
            }
            NavigableSet<String> modulesDep = ReadFileUtil.getModuleNames(fileDep);
            allModulesWithDeps.put(module, modulesDep);
        }
        String pathToArtifactory = projectPath + Constants.SLASH + Constants.ARTIFACTORY_TMP;
        if (new File(pathToArtifactory).exists()) {
            NavigableSet<String> artifactoryTmp = ReadFileUtil.getAllTDPModulesFromProjectDir(pathToArtifactory);
            for (String moduleName : artifactoryTmp) {
                String pathToModuleArtifactory = projectPath + Constants.SLASH + Constants.ARTIFACTORY_TMP + Constants.SLASH + moduleName;
                artifactoryModules.put(moduleName, pathToModuleArtifactory);
            }
        }
    }

    private String nameDepFile(String toPath) {
        File path = new File(toPath);
        if (path.isDirectory()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.getName().endsWith(".dep")) {
                    return file.getAbsolutePath();
                }
            }
        }
        return new String();
    }

    public NavigableSet<String> getDepsForModule(String nameModule) {
        NavigableSet<String> result = null;
        if (allModulesWithDeps.containsKey(nameModule)) {
            result = allModulesWithDeps.get(nameModule);
        } else {
            result = new TreeSet<>();
        }
        return result;
    }

    public NavigableSet<String> getAllDepsForModule(String nameModule) {
        NavigableSet<String> result = new TreeSet<>();
        generateDeps(nameModule, result);
        return result;
    }

    public NavigableSet<String> getAllLibsForModule(String nameModule) {
        NavigableSet<String> allDeps = getAllDepsForModule(nameModule);
        return getLibs(allDeps);
    }

    public NavigableSet<String> getLibsForModule(String nameModule) {
        return getLibs(allModulesWithDeps.get(nameModule));
    }

    private void generateDeps(String base, NavigableSet<String> result) {
        if (result == null) {
            result = new TreeSet<>();
        }
        if (result.contains(base)) {
            return;
        }
        result.add(base);
        if (allModulesWithDeps.containsKey(base)) {
            for (String module : allModulesWithDeps.get(base)) {
                generateDeps(module, result);
            }
        }
    }

    public NavigableSet<String> getAllModulesName() {
        return (NavigableSet<String>) allModulesWithDeps.keySet();
    }

    public Map<String, String> getArtifactoryModules() {
        return artifactoryModules;
    }

    private NavigableSet<String> getLibs(NavigableSet<String> deps) {
        NavigableSet<String> result = new TreeSet<>();
        if (deps == null) {
            return result;
        }
        for (String module : deps) {
            String pathToLib = projectPath + Constants.SLASH + module + Constants.SLASH + "lib";
            File file = new File(pathToLib);
            if (file.exists()) {
                result.add(pathToLib);
            }
        }
        return result;
    }
}
