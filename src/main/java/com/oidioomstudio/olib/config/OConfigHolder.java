package com.oidioomstudio.olib.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * High-performance, zero-boilerplate JSON configuration file manager with debounced write support.
 */
public class OConfigHolder<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger("O Lib/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "O Lib-ConfigSaver");
        thread.setDaemon(true);
        return thread;
    });

    private final String modId;
    private final Class<T> configClass;
    private final Supplier<T> defaultSupplier;
    private final Path filePath;
    private T instance;
    private ScheduledFuture<?> pendingDebounce;

    public OConfigHolder(String modId, Class<T> configClass, Supplier<T> defaultSupplier) {
        this.modId = modId;
        this.configClass = configClass;
        this.defaultSupplier = defaultSupplier;
        this.filePath = FabricLoader.getInstance().getConfigDir().resolve(modId + ".json");
    }

    public synchronized T get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public synchronized void set(T newInstance) {
        this.instance = newInstance;
        save();
    }

    public synchronized T load() {
        try {
            if (Files.exists(filePath)) {
                try (Reader reader = Files.newBufferedReader(filePath)) {
                    instance = GSON.fromJson(reader, configClass);
                    if (instance != null) {
                        save();
                        return instance;
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to load config for {}: {}", modId, t.getMessage());
        }

        instance = defaultSupplier.get();
        save();
        return instance;
    }

    /**
     * Saves the configuration immediately to disk. Cancels any pending debounced save.
     */
    public synchronized void save() {
        if (pendingDebounce != null && !pendingDebounce.isDone()) {
            pendingDebounce.cancel(false);
            pendingDebounce = null;
        }

        if (instance == null) {
            instance = defaultSupplier.get();
        }
        try {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            Path tempFile = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(tempFile)) {
                GSON.toJson(instance, writer);
            }
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Throwable t) {
            LOGGER.error("Failed to save config for {}: {}", modId, t.getMessage());
        }
    }

    /**
     * Debounces disk write by 500ms to eliminate disk I/O thrashing during slider dragging.
     */
    public synchronized void saveDebounced() {
        saveDebounced(500);
    }

    /**
     * Debounces disk write by the specified delay in milliseconds.
     */
    public synchronized void saveDebounced(long delayMillis) {
        if (pendingDebounce != null && !pendingDebounce.isDone()) {
            pendingDebounce.cancel(false);
        }
        pendingDebounce = SCHEDULER.schedule(this::save, delayMillis, TimeUnit.MILLISECONDS);
    }
}
