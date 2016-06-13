package com.tdp.workspace.generator.project;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * TDPProjectType
 *
 * @author Pavel Semenov (<a href="mailto:Pavel_Semenov1@epam.com"/>)
 */
public class TDPProjectType extends ModuleType<TDPProjectBuilder> {
    public static final String MODULE_ID = "TDP_PROJECT";

    public TDPProjectType() {
        super(MODULE_ID);
    }

    @NotNull
    @Override
    public TDPProjectBuilder createModuleBuilder() {
        return new TDPProjectBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return "";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Icon getBigIcon() {
        return null;
    }

    @Override
    public Icon getNodeIcon(@Deprecated boolean isOpened) {
        return null;
    }

    @Nullable
    @Override
    public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep, @NotNull ModuleBuilder moduleBuilder) {
        return new TDPProjectSettingsStep(settingsStep, moduleBuilder);
    }
}
