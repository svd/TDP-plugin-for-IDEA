package com.tdp.workspace.generator;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.impl.ProjectImpl;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.impl.libraries.LibraryImpl;
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
import java.util.Arrays;
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
    boolean artifactoryDep;

    public Controller(List<String> baseModules, String path, boolean artifactoryDep) {
        this.baseModules = baseModules;
        this.path = path;
        this.artifactoryDep = artifactoryDep;
    }

    public void generateWorkspace(Project project) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        ModuleManager manager = ModuleManagerImpl.getInstance(project);
        LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
        Library[] libraries = libraryTable.getLibraries();
        ApplicationManagerEx.getApplicationEx().runProcessWithProgressSynchronously(new Runnable() {
            @Override
            public void run() {
                for (Library library : libraries) {
                    ProgressManager.getInstance().getProgressIndicator().setText("Remove library: " + library.getName());
                    doWriteAction(new Runnable() {
                        @Override
                        public void run() {
                            libraryTable.removeLibrary(library);
                        }
                    });
                }
            }
        }, "Removing libraries", false, project);

        GeneratorModuleDep generatorModuleDep = new GeneratorModuleDep(path, baseModules);
        allDepend = generatorModuleDep.getModuleNames();
        ModulesDepUtil modulesDepUtil = ModulesDepUtil.getInstance(path);
        final List<Module> modules = Arrays.asList(manager.getModules());
        ApplicationManagerEx.getApplicationEx().runProcessWithProgressSynchronously(new Runnable() {
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
        }, "Removing modules", false, project);

        Map<String, String> artifactory = modulesDepUtil.getArtifactoryModules();
        ApplicationManagerEx.getApplicationEx().runProcessWithProgressSynchronously(new Runnable() {
            @Override
            public void run() {
                allDepend.add("dlex_build_templates");
                for (String moduleName : allDepend) {
                    ProgressManager.getInstance().getProgressIndicator().setText("Load module: " + moduleName);
                    String pathToModule = MessageFormat.format(Constants.PATTERN_PATH_TO_MODULE_FILE, path, moduleName);
                    doWriteAction(new Runnable() {
                        @Override
                        public void run() {
                            Module module = manager.newModule(pathToModule, JAVA_MODULE);
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
                                    if(libs.exists()) {
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
                        }
                    });
                }
            }
        }, "Generating modules", false, project);
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
}