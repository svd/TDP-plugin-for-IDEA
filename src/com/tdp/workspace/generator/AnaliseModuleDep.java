package com.tdp.workspace.generator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.tdp.decorator.DescriptionsCache;
import com.tdp.workspace.generator.fileutils.ReadFileUtil;
import com.tdp.workspace.generator.utils.InputModulesValidator;
import com.tdp.workspace.generator.utils.ModulesDepUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.NavigableSet;

/**
 * Created by Siarhei_Nahel on 10/26/2016.
 */
public class AnaliseModuleDep extends AnAction {
    private static final String PATTERN_TITLE = "Dependencies for module {0} ({1}):\n";
    private static final String PATTER_LINE = "{0} - {1}\n";
    @Override
    public void actionPerformed(AnActionEvent e) {
        DescriptionsCache cache = DescriptionsCache.getInstance();
        Project project = e.getProject();
        String path = project.getBasePath();
        ModulesDepUtil modulesDepUtil = null;
        try {
            modulesDepUtil = ModulesDepUtil.getInstance(path);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        NavigableSet<String> allModules = modulesDepUtil.getAllModulesName();
        String baseModule = Messages.showInputDialog(project, "Choose base module:",
                null, null, null, new InputValidator() {
                    @Override
                    public boolean checkInput(String s) {
                        return allModules.contains(s);
                    }

                    @Override
                    public boolean canClose(String s) {
                        return allModules.contains(s);
                    }
                });
        if (baseModule != null){
            NavigableSet<String> modules = modulesDepUtil.getAllDepsForModule(baseModule);
            modules.remove(baseModule);
            String pathToOutFile = path + Constants.SLASH + baseModule + "_dep.txt";
            File file = new File(pathToOutFile);
            try {
                OutputStreamWriter outputStreamWriter = new FileWriter(file);
                outputStreamWriter.write(MessageFormat.format(PATTERN_TITLE, baseModule, cache.getDescription(baseModule)));
                for (String module : modules) {
                    outputStreamWriter.write(MessageFormat.format(PATTER_LINE, module, cache.getDescription(module)));
                }
                outputStreamWriter.flush();
                outputStreamWriter.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
