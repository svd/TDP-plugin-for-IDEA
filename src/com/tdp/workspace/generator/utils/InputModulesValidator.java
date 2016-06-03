package com.tdp.workspace.generator.utils;

import com.tdp.workspace.generator.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.NavigableSet;

/**
 * InputModulesValidator
 *
 * @author Pavel Semenov (<a href="mailto:Pavel_Semenov1@epam.com"/>)
 */
public class InputModulesValidator {
    private NavigableSet<String> allModules;

    public InputModulesValidator(@NotNull NavigableSet<String> allModules) {
        this.allModules = allModules;
    }

    public boolean validate(@NotNull String modulesString) {
        String[] modules = modulesString.split(Constants.SEMICOLON);
        if (modules.length == 0){
            return false;
        }
        int count = 0;
        for (String module : modules){
            if (allModules.contains(module)){
                count++;
            }
        }
        if (modules.length == count){
            return true;
        }
        return false;
    }
}
