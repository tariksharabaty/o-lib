package com.oidioomstudio.olib.api;

import com.oidioomstudio.olib.api.options.OOption;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents a hierarchical sub-category (group/section) within a configuration category.
 * Used across all Oidioom Studio mods for unified and clean option grouping.
 */
public class OConfigGroup {
    private final String id;
    private final Component title;
    private final Component description;
    private final List<OOption<?>> options;

    public OConfigGroup(String id, Component title, Component description, List<OOption<?>> options) {
        this.id = Objects.requireNonNull(id, "Group ID cannot be null");
        this.title = Objects.requireNonNull(title, "Group title cannot be null");
        this.description = description;
        this.options = Collections.unmodifiableList(new ArrayList<>(options));
    }

    public String getId() {
        return id;
    }

    public Component getTitle() {
        return title;
    }

    public Component getDescription() {
        return description;
    }

    public List<OOption<?>> getOptions() {
        return options;
    }

    public static Builder builder(String id, Component title) {
        return new Builder(id, title);
    }

    public static class Builder {
        private final String id;
        private final Component title;
        private Component description;
        private final List<OOption<?>> options = new ArrayList<>();

        public Builder(String id, Component title) {
            this.id = id;
            this.title = title;
        }

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder add(OOption<?> option) {
            if (option != null) {
                this.options.add(option);
            }
            return this;
        }

        public Builder addAll(List<OOption<?>> options) {
            if (options != null) {
                this.options.addAll(options);
            }
            return this;
        }

        public OConfigGroup build() {
            for (OOption<?> option : options) {
                if (option.getGroupName() == null) {
                    option.setGroupName(title);
                }
            }
            return new OConfigGroup(id, title, description, options);
        }
    }
}
