package kaptainwutax.seedcrackerX;

import com.mojang.logging.LogUtils;
import kaptainwutax.seedcrackerX.api.SeedCrackerAPI;
import kaptainwutax.seedcrackerX.config.Config;
import kaptainwutax.seedcrackerX.cracker.storage.DataStorage;
import kaptainwutax.seedcrackerX.init.ClientCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;

import java.util.ArrayList;

public class SeedCracker implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    // Kept only for compatibility with the cracking core. This list is never
    // populated or exposed through automatic third-party entrypoint discovery.
    public static final ArrayList<SeedCrackerAPI> entrypoints = new ArrayList<>();

    private static SeedCracker INSTANCE;
    private final DataStorage dataStorage = new DataStorage();

    public static SeedCracker get() {
        return INSTANCE;
    }

    @Override
    public void onInitialize() {
        INSTANCE = this;
        Config.load();
        Features.init(Config.get().getVersion());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> ClientCommands.registerCommands(dispatcher));
    }

    public DataStorage getDataStorage() {
        return this.dataStorage;
    }

    public void reset() {
        this.dataStorage.clear();
    }
}
