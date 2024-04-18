package net.carnien.dependencymanager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class DependencyManager {

    private final List<Dependency> dependencies = new ArrayList<>();

    public void add(Dependency dependency) {
        if (dependencies.contains(dependency)) return;
        dependencies.add(dependency);
    }

    public void remove(Dependency dependency) {
        if (!dependencies.contains(dependency)) return;
        dependencies.remove(dependency);
    }

    public Dependency[] get() {
        return dependencies.toArray(new Dependency[]{});
    }

    public void downloadMissingDependencies() {
        final Dependency[] missingDependencies = getMissingDependencies();

        if (missingDependencies.length == 0) return;

        final Logger logger = Bukkit.getLogger();
        logger.info("Downloading missing dependencies ...");

        for (Dependency dependency : dependencies) {
            final String name = dependency.getName();
            final String link = dependency.getLink();
            logger.info("Downloading Plugin '" + name + "' from '" + link + "' ...");

            if (!download(dependency)) {
                logger.warning("An error accorded while downloading Plugin '" + name+ "'!");
                continue;
            }

            logger.info("File downloaded successfully.");
            logger.info("Loading plugin '" + name + "' ...");
            final PluginManager pluginManager = Bukkit.getPluginManager();
            final File file = new File("plugins/" + name + ".jar");

            try {
                final Plugin plugin = pluginManager.loadPlugin(file);
                plugin.onLoad();
            } catch (InvalidPluginException | InvalidDescriptionException exception) {
                logger.warning("An error accorded while loading plugin '" + name + "'.");
                return;
            }

            logger.info("Plugin '" + name + "' has been loaded successfully.");
        }
    }

    public boolean isReloadRequired() {
        for (Dependency dependency : dependencies) {
            final String name = dependency.getName();
            final PluginManager pluginManager = Bukkit.getPluginManager();
            Plugin plugin = pluginManager.getPlugin(name);

            if (plugin == null) return true;
            if (plugin.isEnabled()) continue;

            return true;
        }

        return false;
    }

    private Dependency[] getMissingDependencies() {
        final List<Dependency> missingDependencies = new ArrayList<>();

        for (Dependency dependency : dependencies) {
            final String name = dependency.getName();
            final PluginManager pluginManager = Bukkit.getPluginManager();
            Plugin plugin = pluginManager.getPlugin(name);

            if (plugin != null) continue;

            missingDependencies.add(dependency);
        }

        return missingDependencies.toArray(new Dependency[]{});
    }

    private boolean download(Dependency dependency) {
        final String name = dependency.getName();
        final String link = dependency.getLink();

        try {
            final InputStream inputStream = new URL(link).openStream();
            final ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
            FileOutputStream fos = new FileOutputStream("plugins/" + name + ".jar");
            fos.getChannel().transferFrom(readableByteChannel, 0, Integer.MAX_VALUE);
        } catch (IOException exception) { return false; }

        return true;
    }

}
