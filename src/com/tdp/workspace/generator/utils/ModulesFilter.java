package com.tdp.workspace.generator.utils;

import com.tdp.workspace.generator.Constants;

import java.io.File;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Created by Siarhei Nahel on 21.05.2016.
 */
public class ModulesFilter {
    private NavigableSet<String> modules;
    private String repo;

    public ModulesFilter(NavigableSet<String> modules, String repoPath) {
        this.modules = modules;
        this.repo = repoPath;
    }

    public NavigableSet<String> getModuleFiles(){
        NavigableSet<String> moduleFiles = new TreeSet<String>();
        for (String moduleName : modules) {
            String path = repo + Constants.SLASH + moduleName;
            File dir = new File(path);
            if (isValidDir(dir)){
                moduleFiles.add(moduleName);
            }
        }
        return moduleFiles;
    }

    private boolean isValidDir(File dir){
        if (!dir.exists()){
            return false;
        }
        return true;
    }
}