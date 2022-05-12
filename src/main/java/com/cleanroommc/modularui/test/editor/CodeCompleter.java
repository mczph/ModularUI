package com.cleanroommc.modularui.test.editor;

import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeCompleter {

    public static final Map<String, ZsClass> classes = new HashMap<>();
    public static final Map<String, ZsClass> classes_short = new HashMap<>();

    public static final ZsClass zsVoid = new ZsClass("void");
    public static final ZsClass zsInt = new ZsClass("int");
    public static final ZsClass zsFloat = new ZsClass("float");
    public static final ZsClass zsLong = new ZsClass("long");
    public static final ZsClass zsDouble = new ZsClass("double");
    public static final ZsClass zsBool = new ZsClass("bool");
    public static final ZsClass zsString = new ZsClass("string");

    static {
        new ZsClass("mods.gregtech.recipe.RecipeMap");
        new ZsClass("crafttweaker.item.IItemStack");
        new ZsClass("crafttweaker.item.IIngredient");
    }

    public static class ZsClass {
        public final String name;
        public final String shortName;
        public final Map<String, ZsFunction> methods;

        public ZsClass(String name) {
            this(name, Collections.emptyMap());
        }

        public ZsClass(String name, Map<String, ZsFunction> methods) {
            this.name = name;
            String[] parts = this.name.split("\\.");
            this.shortName = parts[parts.length - 1];
            this.methods = methods;
            classes.put(this.name, this);
            classes_short.put(this.shortName, this);
        }
    }

    public static class ZsFunction {
        public final String name;
        public final ZsClass returnType;
        public final List<ZsClass> params;

        public ZsFunction(String name, ZsClass returnType, List<ZsClass> params) {
            this.name = name;
            this.returnType = returnType;
            this.params = params;
        }
    }
}
