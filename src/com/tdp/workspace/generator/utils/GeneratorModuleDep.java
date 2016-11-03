package com.tdp.workspace.generator.utils;

import com.tdp.workspace.generator.Constants;
import com.tdp.workspace.generator.fileutils.ReadFileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Siarhei Nahel on 21.05.2016.
 */
public class GeneratorModuleDep {
    private final String repoPath;
    private final List<String> baseModules;
    private NavigableSet<String> allModules = new TreeSet<>();
    private ModulesDepUtil modulesDepUtil;

    public GeneratorModuleDep(String repoPath, List<String> baseModules) throws FileNotFoundException {
        this.repoPath = repoPath;
        this.baseModules = baseModules;
        modulesDepUtil = ModulesDepUtil.getInstance(repoPath);
    }

    private void generateAllDeps(String name) {
        allModules.addAll(modulesDepUtil.getAllDepsForModule(name));
    }

    public NavigableSet<String> getModuleNames() {
        for (String m : baseModules) {
            generateAllDeps(m);
        }
        return allModules;
    }
}