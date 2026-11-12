package com.oidioomstudio.olib.api;

import com.oidioomstudio.olib.api.options.OOption;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OModConfig {
    private final String modId;
    private final Component displayName;
    private final String version;
    private final List<OConfigCategory> categories;
    private final Runnable onSave;
    private final Runnable onResetAll;

    public OModConfig(String modId, Component displayName, String version,
                       List<OConfigCategory> categories, Runnable onSave, Runnable onResetAll) {
        this.modId = Objects.requireNonNull(modId, "Mod ID cannot be null");
        this.displayName = Objects.requireNonNull(displayName, "Display name cannot be null");
        this.version = version != null ? version : "1.0.0";
        this.categories = Collections.unmodifiableList(new ArrayList<>(categories));
        this.onSave = onSave;
        this.onResetAll = onResetAll;
    }

    public String getModId() {
        return modId;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public String getVersion() {
        return version;
    }

    public List<OConfigCategory> getCategories() {
        return categories;
    }

    public void save() {
        if (onSave != null) {
            try {
                onSave.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void resetAll() {
        if (onResetAll != null) {
            try {
                onResetAll.run();
                return;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        for (OConfigCategory category : categories) {
            for (OOption<?> option : category.getOptions()) {
                option.reset();
            }
        }
    }

    public List<OOption<?>> getAllOptions() {
        List<OOption<?>> list = new ArrayList<>();
        for (OConfigCategory cat : categories) {
            list.addAll(cat.getOptions());
        }
        return list;
    }

    public static Builder builder(String modId, Component displayName, String version) {
        return new Builder(modId, displayName, version);
    }

    public static class Builder {
        private final String modId;
        private final Component displayName;
        private final String version;
        private final List<OConfigCategory> categories = new ArrayList<>();
        private Runnable onSave;
        private Runnable onResetAll;

        public Builder(String modId, Component displayName, String version) {
            this.modId = modId;
            this.displayName = displayName;
            this.version = version;
        }

        public Builder category(OConfigCategory category) {
            if (category != null) {
                this.categories.add(category);
            }
            return this;
        }

        public Builder onSave(Runnable onSave) {
            this.onSave = onSave;
            return this;
        }

        public Builder onResetAll(Runnable onResetAll) {
            this.onResetAll = onResetAll;
            return this;
        }

        public OModConfig build() {
            return new OModConfig(modId, displayName, version, categories, onSave, onResetAll);
        }
    }
}
