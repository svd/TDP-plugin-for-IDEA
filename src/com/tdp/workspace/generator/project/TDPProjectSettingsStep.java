package com.tdp.workspace.generator.project;

import com.intellij.ide.projectWizard.ProjectSettingsStep;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.FieldPanel;
import com.tdp.workspace.generator.Constants;
import com.tdp.workspace.generator.fileutils.ReadFileUtil;
import com.tdp.workspace.generator.utils.InputModulesValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * TDPProjectSettingsStep
 *
 * @author Pavel Semenov (<a href="mailto:Pavel_Semenov1@epam.com"/>)
 */
public class TDPProjectSettingsStep extends ModuleWizardStep {
    private static final String MODULES_DELIMITER = ";";
    @Nullable
    private ModuleWizardStep javaStep;
    private JTextField modulesField;
    private JCheckBox artifactoryDependenceFlag;
    private TDPProjectBuilder moduleBuilder;
    private ProjectSettingsStep settingsStep;

    public TDPProjectSettingsStep(@NotNull SettingsStep settingsStep,
                                  @NotNull ModuleBuilder moduleBuilder) {
        this.settingsStep = (ProjectSettingsStep) settingsStep;
        javaStep = JavaModuleType.getModuleType().modifyProjectTypeStep(settingsStep, moduleBuilder);
        modulesField = new JTextField();
        artifactoryDependenceFlag = new JCheckBox("Use dependencies from artifactory_tmp");
        this.moduleBuilder = (TDPProjectBuilder) moduleBuilder;
        JPanel modulesPanel = new JPanel(new BorderLayout(4, 0));
        modulesPanel.add(modulesField, BorderLayout.CENTER);
        settingsStep.addSettingsField("<html>Enter target modules, <br>separated by semicolons:</html>", modulesPanel);
        settingsStep.addSettingsComponent(artifactoryDependenceFlag);
    }

    @Override
    public JComponent getComponent() {
        return null;
    }

    @Override
    public boolean validate() throws ConfigurationException {
        String projectDir = ((FieldPanel) settingsStep.getModuleNameField().getParent().getComponent(3)).getText();
        InputModulesValidator validator = new InputModulesValidator(
                ReadFileUtil.getAllTDPModulesFromProjectDir(projectDir));
        return validator.validate(modulesField.getText());
        // Another way for validation is "throw new ConfigurationException(...)" if validation fails.
    }

    @Override
    public void updateDataModel() {
        moduleBuilder.setModules(modulesField.getText().split(MODULES_DELIMITER));
        moduleBuilder.setArtifactoryDependenceFlag(artifactoryDependenceFlag.isSelected());
        if (javaStep != null) {
            javaStep.updateDataModel();
        }
    }
}
