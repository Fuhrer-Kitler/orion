package com.avairebot.orion.cache;

import com.avairebot.orion.cache.adapters.MemoryAdapter;
import com.avairebot.orion.contracts.cache.CacheAdapter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public enum CacheType {

    MEMORY("Memory", true, MemoryAdapter.class);

    private static final Map<CacheType, CacheAdapter> INSTANCES = new HashMap<>();

    static {
        for (CacheType type : values()) {
            try {
                Object instance = type.getInstance().getDeclaredConstructor().newInstance();

                if (instance instanceof CacheAdapter) {
                    INSTANCES.put(type, (CacheAdapter) instance);
                }
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                System.out.printf("Invalid cache type given: %s", e.getMessage());
                System.exit(0);
            }
        }
    }

    private final String name;
    private final boolean isDefault;
    private final Class instance;

    CacheType(String name, boolean isDefault, Class instance) {
        this.name = name;
        this.isDefault = isDefault;
        this.instance = instance;
    }

    public static CacheType getDefault() {
        return MEMORY;
    }

    public static CacheType fromName(String name) {
        for (CacheType type : values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public <T> Class<T> getInstance() {
        return instance;
    }

    public CacheAdapter getAdapter() {
        return INSTANCES.get(this);
    }
}
