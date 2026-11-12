package com.oidioomstudio.olib.api.options;

import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FloatSliderOption extends OOption<Float> {
    private final float min;
    private final float max;
    private final float step;
    private final Function<Float, Component> formatter;

    public FloatSliderOption(String id, Component name, Component description, float defaultValue,
                             float min, float max, float step, Function<Float, Component> formatter,
                             Supplier<Float> getter, Consumer<Float> setter) {
        super(id, name, description, defaultValue, getter, setter);
        this.min = min;
        this.max = max;
        this.step = step > 0 ? step : 0.05f;
        this.formatter = formatter != null ? formatter : (val -> Component.literal(String.format(Locale.ROOT, "%.2f", val)));
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getStep() {
        return step;
    }

    public Component formatValue(float value) {
        return formatter.apply(value);
    }

    public double getRatio() {
        if (max <= min) return 0.0;
        return (getValue() - min) / (max - min);
    }

    public void setFromRatio(double ratio) {
        double clamped = Math.max(0.0, Math.min(1.0, ratio));
        float raw = (float) (min + clamped * (max - min));
        float stepped = min + (Math.round((raw - min) / step) * step);
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
        private float defaultValue = 0.0f;
        private float min = 0.0f;
        private float max = 1.0f;
        private float step = 0.05f;
        private Function<Float, Component> formatter;
        private Supplier<Float> getter;
        private Consumer<Float> setter;

        public Builder(String id, Component name) {
            this.id = id;
            this.name = name;
        }

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(float defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder range(float min, float max) {
            this.min = min;
            this.max = max;
            return this;
        }

        public Builder range(float min, float max, float step) {
            this.min = min;
            this.max = max;
            this.step = step;
            return this;
        }

        public Builder formatter(Function<Float, Component> formatter) {
            this.formatter = formatter;
            return this;
        }

        public Builder getter(Supplier<Float> getter) {
            this.getter = getter;
            return this;
        }

        public Builder setter(Consumer<Float> setter) {
            this.setter = setter;
            return this;
        }

        public FloatSliderOption build() {
            return new FloatSliderOption(id, name, description, defaultValue, min, max, step, formatter, getter, setter);
        }
    }
}
