package kaptainwutax.seedcrackerX.config;

import com.seedfinding.mcfeature.Feature;
import com.seedfinding.mcfeature.structure.RegionStructure;
import com.seedfinding.mcfeature.structure.Structure;
import kaptainwutax.seedcrackerX.Features;
import kaptainwutax.seedcrackerX.SeedCracker;
import kaptainwutax.seedcrackerX.cracker.HashedSeedData;
import kaptainwutax.seedcrackerX.cracker.storage.DataStorage;
import kaptainwutax.seedcrackerX.cracker.storage.ScheduledSet;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StructureSave {
    private static final Logger logger = LoggerFactory.getLogger("lucentPipelineStorage");

    public static final Path saveDir = Paths.get(FabricLoader.getInstance().getConfigDir().toFile().toString(), "Lucent Pipeline data");

    public static void saveStructures(ScheduledSet<DataStorage.Entry<Feature.Data<?>>> baseData) {
        try {
            Files.createDirectories(saveDir);
            Path saveFile = saveDir.resolve(getWorldName());
            Files.deleteIfExists(saveFile);
            Files.createFile(saveFile);
            try (FileWriter writer = new FileWriter(saveFile.toFile())) {
                for (DataStorage.Entry<Feature.Data<?>> dataEntry : baseData) {
                    if (dataEntry.data.feature instanceof Structure structure) {
                        String data = Structure.getName(structure.getClass()) +
                            ";" + dataEntry.data.chunkX +
                            ";" + dataEntry.data.chunkZ +
                            "\n";
                        writer.write(data);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Lucent Pipeline couldn't save local data", e);
        }
    }

    public static List<RegionStructure.Data<?>> loadStructures() {
        List<RegionStructure.Data<?>> result = new ArrayList<>();
        try {
            Files.createDirectories(saveDir);
            Path saveFile = saveDir.resolve(getWorldName());
            try (
                FileInputStream fis = new FileInputStream(saveFile.toFile());
                Scanner sc = new Scanner(fis)
            ) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    String[] info = line.split(";");
                    if (info.length != 3) continue;
                    String structureName = info[0];
                    for (RegionStructure<?,?> structure : Features.STRUCTURE_TYPES) {
                        if (structureName.equals(structure.getName())) {
                            result.add(structure.at(Integer.parseInt(info[1]), Integer.parseInt(info[2])));
                            break;
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            logger.warn("Lucent Pipeline couldn't find a local data file");
            return result;
        } catch (IOException e) {
            logger.error("Lucent Pipeline couldn't load local data", e);
        }
        return result;
    }

    private static String getWorldName() {
        HashedSeedData hashedSeedData = SeedCracker.get().getDataStorage().hashedSeedData;
        if (hashedSeedData == null) {
            return "unknown-world.txt";
        }
        return "world-cache-" + Long.toUnsignedString(hashedSeedData.getHashedSeed()) + ".txt";
    }
}
