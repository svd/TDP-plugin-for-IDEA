package com.tdp.workspace.generator.project;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.IconLoader;
import com.tdp.workspace.generator.Constants;
import com.tdp.workspace.generator.Controller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.*;

/**
 * TDPProjectBuilder
 *
 * @author Pavel Semenov (<a href="mailto:Pavel_Semenov1@epam.com"/>)
 */
public class TDPProjectBuilder extends ModuleBuilder {
    private static final TDPProjectType MODULE_TYPE = new TDPProjectType();

    /* Contains array of selected modules */
    private String[] modules = new String[0];

    public void setModules(String[] modules) {
        this.modules = modules;
    }

    @Override
    public ModuleType getModuleType() {
        return MODULE_TYPE;
    }

    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        // Do nothing here
    }

    @Nullable
    @Override
    public List<Module> commit(@NotNull Project project, ModifiableModuleModel model, ModulesProvider modulesProvider) {
        List<String> baseModulesList = new ArrayList<>();
        baseModulesList.addAll(Arrays.asList(modules));
        Controller controller = new Controller(baseModulesList, project.getBasePath());
        try {
            controller.generateWorkspace(project);
            StringBuilder builder = new StringBuilder();
            for(int i=0; i < baseModulesList.size(); i++) {
                builder.append(baseModulesList.get(i));
                if (i < baseModulesList.size() - 1) {
                    builder.append(Constants.SEMICOLON);
                }
            }
            controller.saveBaseModulesProperty(builder.toString());
            if (!baseModulesList.get(0).equals(Constants.ALL_MODULES_STRING)) {
                controller.renameProject(project, baseModulesList.get(0));
            }
        } catch (IOException | TransformerException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
        return super.commit(project, model, modulesProvider);
    }

    @Override
    public boolean canCreateModule() {
        return false;
    }

    @Override
    public Icon getNodeIcon() {
        return IconLoader.getIcon("/images/favicon.png");
    }

    @Override
    public boolean isSuitableSdkType(SdkTypeId sdkType) {
        return sdkType instanceof JavaSdkType;
    }
}
