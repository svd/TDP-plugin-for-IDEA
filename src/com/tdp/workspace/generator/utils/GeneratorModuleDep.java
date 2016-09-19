package com.tdp.workspace.generator.utils;

import com.tdp.workspace.generator.Constants;
import com.tdp.workspace.generator.fileutils.ReadFileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Created by Siarhei Nahel on 21.05.2016.
 */
public class GeneratorModuleDep {
    private final String repoPath;
    private final List<String> baseModules;
    private NavigableSet<String> allModules = new TreeSet<>();

    public GeneratorModuleDep(String repoPath, List<String> baseModules) {
        this.repoPath = repoPath;
        this.baseModules = baseModules;
    }

    private void generateAllDeps(String name) throws FileNotFoundException {
        String pathToBuild = repoPath + Constants.SLASH + name + Constants.SLASH
                + Constants.BUILD_DIR;
        String pathDepFile = nameDepFile(pathToBuild);
        if (!pathDepFile.isEmpty()) {
            File file = new File(pathDepFile);
            if (file.exists()) {
                NavigableSet<String> modules = ReadFileUtil.getModuleNames(file);
                for (String moduleName : modules) {
                    if (!allModules.contains(moduleName)) {
                        allModules.add(moduleName);
                        generateAllDeps(moduleName);
                    }
                }
            }
        }
    }

    public NavigableSet<String> getModuleNames() throws FileNotFoundException {
        for (String m : baseModules) {
            allModules.add(m);
            generateAllDeps(m);
        }
        ModulesFilter filter = new ModulesFilter(allModules, repoPath);
        return filter.getModuleFiles();
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
}