package com.oidioomstudio.olib.api;

import com.oidioomstudio.olib.api.options.OOption;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents a major configuration tab/category in O Lib.
 * Contains both top-level options and structured sub-category groups.
 */
public class OConfigCategory {
    private final String id;
    private final Component title;
    private final List<OOption<?>> standaloneOptions;
    private final List<OConfigGroup> groups;

    public OConfigCategory(String id, Component title, List<OOption<?>> standaloneOptions, List<OConfigGroup> groups) {
        this.id = Objects.requireNonNull(id, "Category ID cannot be null");
        this.title = Objects.requireNonNull(title, "Category title cannot be null");
        this.standaloneOptions = Collections.unmodifiableList(new ArrayList<>(standaloneOptions));
        this.groups = Collections.unmodifiableList(new ArrayList<>(groups));
    }

    public String getId() {
        return id;
    }

    public Component getTitle() {
        return title;
    }

    public List<OOption<?>> getStandaloneOptions() {
        return standaloneOptions;
    }

    public List<OConfigGroup> getGroups() {
        return groups;
    }

    /**
     * Returns all options in this category, including standalone options and all grouped options.
     */
    public List<OOption<?>> getOptions() {
        List<OOption<?>> all = new ArrayList<>(standaloneOptions);
        for (OConfigGroup group : groups) {
            all.addAll(group.getOptions());
        }
        return Collections.unmodifiableList(all);
    }

    public static Builder builder(String id, Component title) {
        return new Builder(id, title);
    }

    public static class Builder {
        private final String id;
        private final Component title;
        private final List<OOption<?>> standaloneOptions = new ArrayList<>();
        private final List<OConfigGroup> groups = new ArrayList<>();

        public Builder(String id, Component title) {
            this.id = id;
            this.title = title;
        }

        public Builder add(OOption<?> option) {
            if (option != null) {
                this.standaloneOptions.add(option);
            }
            return this;
        }

        public Builder addAll(List<OOption<?>> options) {
            if (options != null) {
                this.standaloneOptions.addAll(options);
            }
            return this;
        }

        public Builder group(OConfigGroup group) {
            if (group != null) {
                this.groups.add(group);
            }
            return this;
        }

        public Builder group(String id, Component title, Consumer<OConfigGroup.Builder> groupConsumer) {
            OConfigGroup.Builder groupBuilder = OConfigGroup.builder(id, title);
            if (groupConsumer != null) {
                groupConsumer.accept(groupBuilder);
            }
            this.groups.add(groupBuilder.build());
            return this;
        }

        public OConfigCategory build() {
            return new OConfigCategory(id, title, standaloneOptions, groups);
        }
    }
}
