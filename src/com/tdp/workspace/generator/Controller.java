package com.tdp.workspace.generator;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.impl.ProjectImpl;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.impl.libraries.LibraryImpl;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.tdp.decorator.DescriptionsCache;
import com.tdp.workspace.generator.fileutils.TdpPluginPropertiesReader;
import com.tdp.workspace.generator.utils.GeneratorModuleDep;
import com.tdp.workspace.generator.utils.GeneratorSourceContent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.impl.ModuleManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.Consumer;
import com.tdp.workspace.generator.utils.ModulesDepUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

/**
 * Created by Siarhei Nahel on 21.05.2016.
 */
public class Controller {
    private static final String JAVA_MODULE = "JAVA_MODULE";
    private String path;
    private List<String> baseModules;
    private NavigableSet<String> allDepend;
    protected boolean artifactoryDep;

    public Controller(List<String> baseModules, String path, boolean artifactoryDep) {
        this.baseModules = baseModules;
        this.path = path;
        this.artifactoryDep = artifactoryDep;
    }

    public void generateWorkspace(Project project) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        ModuleManager manager = ModuleManagerImpl.getInstance(project);
        LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
        ApplicationManagerEx.getApplicationEx().runProcessWithProgressSynchronously(new RemoveLibraries(libraryTable),
                "Removing libraries", false, project);

        GeneratorModuleDep generatorModuleDep = new GeneratorModuleDep(path, baseModules);
        allDepend = generatorModuleDep.getModuleNames();
        ModulesDepUtil modulesDepUtil = ModulesDepUtil.getInstance(path);
        final List<Module> modules = Arrays.asList(manager.getModules());
        ApplicationManagerEx.getApplicationEx().runProcessWithProgressSynchronously(new RemoveModules(modules, manager),
                "Removing modules", false, project);
        allDepend.add("dlex_build_templates");

        ApplicationManagerEx.getApplicationEx().runProcessWithProgressSynchronously(new ImportModules(project, manager, modulesDepUtil),
                "Generating modules", false, project);
    }

    public void saveBaseModulesProperty(String baseModulesString, boolean artifactoryDep) {
        try {
            TdpPluginPropertiesReader.getInstance(path).setProperty("baseModules", baseModulesString);
            TdpPluginPropertiesReader.getInstance(path).setProperty("artifactoryDep", Boolean.toString(artifactoryDep));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renameProject(Project project, String nameBaseModule) {
        DescriptionsCache cache = DescriptionsCache.getInstance();
        String newNameProject = cache.getDescription(nameBaseModule);
        if (newNameProject != null) {
            ProjectImpl projectImpl = (ProjectImpl) project;
            projectImpl.setProjectName(newNameProject);
        }
    }

    private static void doWriteAction(final Runnable action) {
        final Application application = ApplicationManager.getApplication();
        application.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                application.runWriteAction(action);
            }
        }, application.getDefaultModalityState());
    }

    private class RemoveLibraries implements Runnable {
        private LibraryTable table;
        public RemoveLibraries(LibraryTable table) {
            this.table = table;
        }

        @Override
        public void run() {
            Library[] libraries = table.getLibraries();
            for (Library library : libraries) {
                ProgressManager.getInstance().getProgressIndicator().setText("Remove library: " + library.getName());
                doWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        table.removeLibrary(library);
                    }
                });
            }
        }
    }

    private class RemoveModules implements Runnable {
        private List<Module> modules;
        private ModuleManager manager;
        public RemoveModules(List<Module> modules, ModuleManager manager) {
            this.modules = modules;
            this.manager = manager;
        }

        @Override
        public void run() {
            for (Module module : modules) {
                String moduleName = module.getName();
                ProgressManager.getInstance().getProgressIndicator().setText("Remove module: " + moduleName);
                if (!allDepend.contains(moduleName)){
                    doWriteAction(new Runnable() {
                        @Override
                        public void run() {
                            manager.disposeModule(module);
                        }
                    });
                }
            }
        }
    }

    private class ImportModules implements Runnable {
        private final Project project;
        private final ModulesDepUtil modulesDepUtil;
        private final ModuleManager manager;

        public ImportModules(Project project, ModuleManager manager, ModulesDepUtil modulesDepUtil) {
            this.manager = manager;
            this.modulesDepUtil = modulesDepUtil;
            this.project = project;
        }

        @Override
        public void run() {
            Map<String, String> artifactory = modulesDepUtil.getArtifactoryModules();
            for (String moduleName : allDepend) {
                ProgressManager.getInstance().getProgressIndicator().setText("Load module: " + moduleName);
                doWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        ModifiableModuleModel modifiableModel = manager.getModifiableModel();
                        String pathToModule = MessageFormat.format(Constants.PATTERN_PATH_TO_MODULE_FILE, path, moduleName);
//                        Module module = manager.newModule(pathToModule, JAVA_MODULE);
                        Module module = modifiableModel.newModule(pathToModule, JAVA_MODULE);
                        String rootContent = path + Constants.SLASH + moduleName;
                        String output = rootContent + Constants.CLASSES_DIR;
                        ModuleRootModificationUtil.updateModel(module, new Consumer<ModifiableRootModel>() {
                            @Override
                            public void consume(ModifiableRootModel model) {
                                CompilerModuleExtension extension = model.getModuleExtension(CompilerModuleExtension.class);
                                ContentEntry rootContentEntry = model.addContentEntry(VfsUtilCore.pathToUrl(rootContent));
                                extension.inheritCompilerOutputPath(false);
                                extension.setCompilerOutputPath(VfsUtilCore.pathToUrl(output));
                                extension.setCompilerOutputPathForTests(VfsUtilCore.pathToUrl(output));
                                extension.setExcludeOutput(true);
                                model.inheritSdk();
                                boolean hasModuleInArtifactory = artifactory.containsKey(moduleName);
                                OrderEntry[] orderEntries = model.getOrderEntries();
                                NavigableSet<String> modulesDep = modulesDepUtil.getDepsForModule(moduleName);
                                for (OrderEntry entry : orderEntries) {
                                    if ((entry instanceof ModuleOrderEntry) || (entry instanceof LibraryOrderEntry)) {
                                        model.removeOrderEntry(entry);
                                    }
                                }
                                for (String moduleDep : modulesDep) {
                                    model.addInvalidModuleEntry(moduleDep).setExported(true);
                                }
                                String pathToLib;
                                if (hasModuleInArtifactory && artifactoryDep) {
                                    pathToLib = artifactory.get(moduleName);
                                } else {
                                    try {
                                        GeneratorSourceContent.generateRes(path, rootContentEntry, rootContent);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    pathToLib = path + Constants.SLASH + moduleName + Constants.SLASH + Constants.LIB_DIR;
                                }
                                File libs = new File(pathToLib);
                                if (libs.exists()) {
                                    LibraryImpl library = (LibraryImpl) model.getModuleLibraryTable().createLibrary(moduleName);
                                    LibraryEx.ModifiableModelEx libraryModel = library.getModifiableModel();
                                    VirtualFile file = VfsUtilCore.findRelativeFile(pathToLib, project.getBaseDir());
                                    libraryModel.addJarDirectory(file, true);
                                    LibraryOrderEntry entry = model.findLibraryOrderEntry(library);
                                    assert entry != null : library;
                                    entry.setScope(DependencyScope.COMPILE);
                                    entry.setExported(true);
                                    doWriteAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            libraryModel.commit();
                                        }
                                    });
                                }

                            }
                        });
                        modifiableModel.commit();
                    }
                });
            }
        }
    }
}