package com.cleanroommc.multiblocked.api.crafttweaker;

import com.cleanroommc.multiblocked.Multiblocked;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.socket.SingleError;
import crafttweaker.zenscript.CrtStoringErrorLogger;
import crafttweaker.zenscript.GlobalRegistry;
import stanhebben.zenscript.ZenModule;
import stanhebben.zenscript.ZenParsedFile;
import stanhebben.zenscript.ZenTokener;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.parser.ParseException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static stanhebben.zenscript.ZenModule.compileScripts;
import static stanhebben.zenscript.ZenModule.extractClassName;

public class CTHelper {
    private static String ERROR;

    public static String getError() {
        return ERROR;
    }

    public static boolean executeDynamicScript(String script) {
        ERROR = null;
        boolean loadSuccessful = true;

        // prepare logger
        ((CrtStoringErrorLogger) GlobalRegistry.getErrors()).clear();

        String loaderName = "multiblocked";
        Map<String, byte[]> classes = new HashMap<>();
        IEnvironmentGlobal environmentGlobal = GlobalRegistry.makeGlobalEnvironment(classes, loaderName);

        // ZS magic
        // update class name generator
        environmentGlobal.getClassNameGenerator().setPrefix("Multiblocked");

        ZenParsedFile zenParsedFile = null;
        String filename = "mbd_dynamic.zs";
        String className = extractClassName(filename);

        ZenModule.classes.remove(className);
        ZenModule.loadedClasses.remove(className);

        // start reading of the scripts
        ZenTokener parser = null;
        try {
            parser = new ZenTokener(script, environmentGlobal.getEnvironment(), filename, false);
            zenParsedFile = new ZenParsedFile(filename, className, parser, environmentGlobal);
        } catch(IOException ex) {
            ERROR = loaderName + ": Could not load script " + filename + ": " + ex.getMessage();
            Multiblocked.LOGGER.error(ERROR);
            loadSuccessful = false;
        } catch(ParseException ex) {
            ERROR = loaderName + ": Error parsing " + ex.getFile().getFileName() + ":" + ex.getLine() + " -- " + ex.getExplanation();
            ERROR += "\n" + new SingleError(ex.getFile().getFileName(), ex.getLine(), ex.getLineOffset(), ex.getExplanation(), SingleError.Level.ERROR);
            loadSuccessful = false;
        } catch(Exception ex) {
            ERROR = loaderName + ": Error loading " + ex;
            if (parser != null) {
                ERROR += "\n" + new SingleError(parser.getFile().getFileName(), parser.getLine(), parser.getLineOffset(), "Generic ERROR", SingleError.Level.ERROR);
            }
            loadSuccessful = false;
        }

        try {
            // Stops if the compile is disabled
            if(zenParsedFile != null) {
                compileScripts(className, Collections.singletonList(zenParsedFile), environmentGlobal, false);
                ZenModule module = new ZenModule(classes, CraftTweakerAPI.class.getClassLoader());
                Runnable runnable = module.getMain();
                if(runnable != null)
                    runnable.run();
            }
        } catch(Throwable ex) {
            ERROR = ex.toString();
            loadSuccessful = false;
        }
        return loadSuccessful;
    }
}
