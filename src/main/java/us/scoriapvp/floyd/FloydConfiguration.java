package us.scoriapvp.floyd;

import us.scoriapvp.nightmare.util.Config;

public final class FloydConfiguration extends Config {

    public FloydConfiguration(Floyd plugin) {
        super(plugin, "config");
    }

    private <T> T getOrDefault(String path, Object def) {
        if (!contains(path)) {
            addDefault(path, def);
            options.copyDefaults(true);
            plugin.getLogger().warning("Configuration option '" + path + "' was missing in file " + name + ", restart the server to apply configuration changes!");
        }

        return (T) get(path);
    }
}