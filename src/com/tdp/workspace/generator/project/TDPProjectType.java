package com.tdp.workspace.generator.project;

import com.intellij.ide.projectWizard.ModuleNameLocationComponent;
import com.intellij.ide.projectWizard.ProjectSettingsStep;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

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
        /* This block is intended for hiding a section with module-specific fields and labels on the finish step.
           There is not more convenient approach to do that. I have created an issue about this
           (https://youtrack.jetbrains.com/issue/IJSDK-158). */
        try {
            Field field = ProjectSettingsStep.class.getDeclaredField("myModuleNameLocationComponent");
            field.setAccessible(true);
            ModuleNameLocationComponent moduleComponent = (ModuleNameLocationComponent) field.get(settingsStep);
            Component[] components = moduleComponent.getModulePanel().getComponents();
            for (int i = 0; i < 6; i++) {
                if (!components[i].isVisible()) {
                    continue;
                }
                Component spy = Mockito.spy(components[i]);
                spy.setVisible(false);
                Mockito.doNothing().when(spy).setVisible(true);
                moduleComponent.getModulePanel().remove(components[i]);
                moduleComponent.getModulePanel().add(spy, i);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return new TDPProjectSettingsStep(settingsStep, moduleBuilder);
    }
}
