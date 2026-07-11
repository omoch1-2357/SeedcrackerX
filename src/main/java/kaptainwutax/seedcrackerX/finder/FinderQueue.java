package kaptainwutax.seedcrackerX.finder;

import kaptainwutax.seedcrackerX.config.Config;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class FinderQueue {

    private static final FinderQueue INSTANCE = new FinderQueue();
    private static final Logger log = LoggerFactory.getLogger(FinderQueue.class);
    public static final ExecutorService SERVICE = Executors.newFixedThreadPool(5);

    private FinderQueue() {
    }

    public static FinderQueue get() {
        return INSTANCE;
    }

    public void onChunkData(Level world, ChunkPos chunkPos) {
        if (!Config.get().active) return;

        getActiveFinderTypes().forEach(type -> SERVICE.submit(() -> {
            try {
                List<Finder> finders = type.finderBuilder.build(world, chunkPos);
                for (Finder finder : finders) {
                    if (finder.isValidDimension(world.dimensionType())) {
                        finder.findInChunk();
                    }
                }
            } catch (Exception e) {
                log.error("Lucent Pipeline seed finder failed", e);
            }
        }));
    }

    public List<Finder.Type> getActiveFinderTypes() {
        return Arrays.stream(Finder.Type.values())
                .filter(type -> type.enabled.get())
                .collect(Collectors.toList());
    }
}
