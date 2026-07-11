package kaptainwutax.seedcrackerX.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seedfinding.mccore.version.MCVersion;
import kaptainwutax.seedcrackerX.Features;
import kaptainwutax.seedcrackerX.util.FeatureToggle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

public class Config {
    private static final Logger logger = LoggerFactory.getLogger("config");

    private static final File file = new File(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().toFile(), "lucent-pipeline.json");
    private static Config INSTANCE = new Config();

    public FeatureToggle buriedTreasure = new FeatureToggle(true);
    public FeatureToggle desertTemple = new FeatureToggle(true);
    public FeatureToggle endCity = new FeatureToggle(true);
    public FeatureToggle jungleTemple = new FeatureToggle(true);
    public FeatureToggle monument = new FeatureToggle(true);
    public FeatureToggle swampHut = new FeatureToggle(true);
    public FeatureToggle shipwreck = new FeatureToggle(true);
    public FeatureToggle outpost = new FeatureToggle(true);
    public FeatureToggle igloo = new FeatureToggle(true);
    public FeatureToggle trialChambers = new FeatureToggle(true);
    public FeatureToggle endPillars = new FeatureToggle(true);
    public FeatureToggle endGateway = new FeatureToggle(false);
    public FeatureToggle dungeon = new FeatureToggle(true);
    public FeatureToggle emeraldOre = new FeatureToggle(false);
    public FeatureToggle desertWell = new FeatureToggle(false);
    public FeatureToggle warpedFungus = new FeatureToggle(false);
    public FeatureToggle biome = new FeatureToggle(false);

    public boolean active = true;

    // Internal diagnostic flag retained for compatibility with the cracking
    // core. There is no command or UI that enables it in this build.
    public boolean debug = false;

    private MCVersion version = MCVersion.latest();

    // Retained only because the upstream cracking core still references these
    // fields. Network/database implementations are absent and these values are
    // forced false on every load/save.
    public boolean databaseSubmits = false;
    public boolean anonymusSubmits = false;

    public static void save() {
        enforceLocalOnlyMode();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(INSTANCE, writer);
        } catch (IOException e) {
            logger.error("Lucent Pipeline couldn't save config", e);
        }
    }

    public static void load() {
        Gson gson = new Gson();

        if (!file.exists()) {
            enforceLocalOnlyMode();
            return;
        }

        try (Reader reader = new FileReader(file)) {
            INSTANCE = gson.fromJson(reader, Config.class);
            if (INSTANCE == null) INSTANCE = new Config();
        } catch (Exception e) {
            logger.error("Lucent Pipeline couldn't load config, deleting it...", e);
            file.delete();
            INSTANCE = new Config();
        }

        enforceLocalOnlyMode();
    }

    private static void enforceLocalOnlyMode() {
        INSTANCE.databaseSubmits = false;
        INSTANCE.anonymusSubmits = false;
        INSTANCE.debug = false;
    }

    public static Config get() {
        return INSTANCE;
    }

    public MCVersion getVersion() {
        return version;
    }

    public void setVersion(MCVersion version) {
        if (this.version == version) return;
        this.version = version;
        Features.init(version);
    }
}
