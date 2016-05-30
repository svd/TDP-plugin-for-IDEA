package com.tdp.workspace.generator.utils;

import com.tdp.workspace.generator.fileutils.TdpPluginPropertiesReader;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.vfs.VfsUtilCore;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.java.JavaSourceRootProperties;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;

import java.io.File;
import java.io.IOException;

/**
 * Created by Siarhei_Nahel on 5/26/2016.
 */
public class GeneratorSourceContent {
    public static void generateRes(String pathToRepo, ContentEntry rootContentEntry, String pathToModule) throws IOException {
        for (String dir : TdpPluginPropertiesReader.getInstance(pathToRepo).getSourceDirs()){
            if (dir.isEmpty()) {
                continue;
            }
            if (isExistDir(pathToModule, dir)){
                rootContentEntry.addSourceFolder(VfsUtilCore.pathToUrl(pathToModule + dir), false);
            }
        }

        for (String dir : TdpPluginPropertiesReader.getInstance(pathToRepo).getGeneratedDirs()){
            if (dir.isEmpty()) {
                continue;
            }
            if (isExistDir(pathToModule, dir)){
                JavaSourceRootProperties properties = JpsJavaExtensionService.getInstance().createSourceRootProperties("", true);
                rootContentEntry.addSourceFolder(VfsUtilCore.pathToUrl((pathToModule + dir)), JavaSourceRootType.SOURCE, properties);
            }
        }
        for (String dir : TdpPluginPropertiesReader.getInstance(pathToRepo).getTestSourceDirs()) {
            if (dir.isEmpty()) {
                continue;
            }
            if (isExistDir(pathToModule, dir)) {
                rootContentEntry.addSourceFolder(VfsUtilCore.pathToUrl(pathToModule + dir), true);
            }
        }

        for (String dir : TdpPluginPropertiesReader.getInstance(pathToRepo).getTestResourcesDirs()) {
            if (dir.isEmpty()) {
                continue;
            }
            if (isExistDir(pathToModule, dir)) {
                rootContentEntry.addSourceFolder(VfsUtilCore.pathToUrl(pathToModule + dir), JavaResourceRootType.TEST_RESOURCE);
            }
        }

        for (String dir : TdpPluginPropertiesReader.getInstance(pathToRepo).getResourcesDirs()) {
            if (dir.isEmpty()) {
                continue;
            }
            if (isExistDir(pathToModule, dir)) {
                rootContentEntry.addSourceFolder(VfsUtilCore.pathToUrl(pathToModule + dir), JavaResourceRootType.RESOURCE);
            }
        }
    }

    private static boolean isExistDir(String pathToModule, String dirName){
        String path = pathToModule + dirName;
        File file = new File(path);
        if (file.exists()){
            return true;
        }
        return false;
    }
}