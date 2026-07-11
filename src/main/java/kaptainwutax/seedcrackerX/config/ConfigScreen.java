package kaptainwutax.seedcrackerX.config;

import net.minecraft.client.gui.screens.Screen;

/**
 * Compatibility shim for older code paths.
 *
 * The local-only build does not bundle Cloth Config. Runtime configuration is
 * performed through the existing client commands and the local config file.
 */
public class ConfigScreen {

    public Screen getConfigScreenByCloth(Screen parent) {
        return parent;
    }
}
