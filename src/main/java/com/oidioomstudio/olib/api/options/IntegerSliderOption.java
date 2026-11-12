package com.oidioomstudio.olib.api.options;

import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class IntegerSliderOption extends OOption<Integer> {
    private final int min;
    private final int max;
    private final int step;
    private final Function<Integer, Component> formatter;

    public IntegerSliderOption(String id, Component name, Component description, int defaultValue,
                               int min, int max, int step, Function<Integer, Component> formatter,
                               Supplier<Integer> getter, Consumer<Integer> setter) {
        super(id, name, description, defaultValue, getter, setter);
        this.min = min;
        this.max = max;
        this.step = Math.max(1, step);
        this.formatter = formatter != null ? formatter : (val -> Component.literal(String.valueOf(val)));
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getStep() {
        return step;
    }

    public Component formatValue(int value) {
        return formatter.apply(value);
    }

    public double getRatio() {
        if (max <= min) return 0.0;
        return (double) (getValue() - min) / (double) (max - min);
    }

    public void setFromRatio(double ratio) {
        double clamped = Math.max(0.0, Math.min(1.0, ratio));
        int raw = (int) Math.round(min + clamped * (max - min));
        int stepped = min + (Math.round((float) (raw - min) / step) * step);
        stepped = Math.max(min, Math.min(max, stepped));
        setValue(stepped);
    }

    public static Builder builder(String id, Component name) {
        return new Builder(id, name);
    }

    public static class Builder {
        private final String id;
        private final Component name;
        private Component description;
        private int defaultValue = 0;
        private int min = 0;
        private int max = 100;
        private int step = 1;
        private Function<Integer, Component> formatter;
        private Supplier<Integer> getter;
        private Consumer<Integer> setter;

        public Builder(String id, Component name) {
            this.id = id;
            this.name = name;
        }

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(int defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder range(int min, int max) {
            this.min = min;
            this.max = max;
            return this;
        }

        public Builder range(int min, int max, int step) {
            this.min = min;
            this.max = max;
            this.step = step;
            return this;
        }

        public Builder formatter(Function<Integer, Component> formatter) {
            this.formatter = formatter;
            return this;
        }

        public Builder getter(Supplier<Integer> getter) {
            this.getter = getter;
            return this;
        }

        public Builder setter(Consumer<Integer> setter) {
            this.setter = setter;
            return this;
        }

        public IntegerSliderOption build() {
            return new IntegerSliderOption(id, name, description, defaultValue, min, max, step, formatter, getter, setter);
        }
    }
}
