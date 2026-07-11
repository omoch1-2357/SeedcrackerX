package kaptainwutax.seedcrackerX.finder.decorator;

import com.seedfinding.mccore.version.MCVersion;
import kaptainwutax.seedcrackerX.Features;
import kaptainwutax.seedcrackerX.SeedCracker;
import kaptainwutax.seedcrackerX.config.Config;
import kaptainwutax.seedcrackerX.cracker.DataAddedEvent;
import kaptainwutax.seedcrackerX.cracker.decorator.Decorator;
import kaptainwutax.seedcrackerX.cracker.decorator.Dungeon;
import kaptainwutax.seedcrackerX.finder.BlockFinder;
import kaptainwutax.seedcrackerX.finder.Finder;
import kaptainwutax.seedcrackerX.util.BiomeFixer;
import kaptainwutax.seedcrackerX.util.PosIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DungeonFinder extends BlockFinder {

    protected static final Set<BlockPos> POSSIBLE_FLOOR_POSITIONS = PosIterator.create(
            new BlockPos(-4, -1, -4),
            new BlockPos(4, -1, 4)
    );

    public DungeonFinder(Level world, ChunkPos chunkPos) {
        super(world, chunkPos, Blocks.SPAWNER);
        this.searchPositions = CHUNK_POSITIONS;
    }

    public static List<Finder> create(Level world, ChunkPos chunkPos) {
        List<Finder> finders = new ArrayList<>();
        for (int chunkX = chunkPos.x() - 1; chunkX <= chunkPos.x() + 1; chunkX++) {
            for (int chunkZ = chunkPos.z() - 1; chunkZ <= chunkPos.z() + 1; chunkZ++) {
                if (surroundingChunksLoaded(chunkX, chunkZ, world)) {
                    finders.add(new DungeonFinder(world, new ChunkPos(chunkX, chunkZ)));
                }
            }
        }
        return finders;
    }

    private static boolean surroundingChunksLoaded(int chunkX, int chunkZ, Level world) {
        for (int x = chunkX - 1; x <= chunkX + 1; x++) {
            for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
                if (world.getChunkSource().getChunkNow(x, z) == null) return false;
            }
        }
        return true;
    }

    @Override
    public List<BlockPos> findInChunk() {
        List<BlockPos> result = super.findInChunk();
        if (result.size() != 1) return new ArrayList<>();

        result.removeIf(pos -> {
            BlockEntity blockEntity = this.world.getBlockEntity(pos);
            if (!(blockEntity instanceof SpawnerBlockEntity)) return true;
            int count = 0;
            for (BlockPos blockPos : POSSIBLE_FLOOR_POSITIONS) {
                Block currentBlock = this.world.getBlockState(pos.offset(blockPos)).getBlock();
                if (currentBlock == Blocks.COBBLESTONE || currentBlock == Blocks.MOSSY_COBBLESTONE) count++;
            }
            return count < 20;
        });

        if (result.size() != 1) return new ArrayList<>();

        Biome biome = this.world.getNoiseBiome((this.chunkPos.x() << 2) + 2, 0, (this.chunkPos.z() << 2) + 2).value();
        BlockPos pos = result.get(0);

        if (Config.get().getVersion().isNewerThan(MCVersion.v1_17_1)) {
            Decorator.Data<?> data;
            if (pos.getY() < 0) {
                data = Features.DEEP_DUNGEON.at(pos.getX(), pos.getY(), pos.getZ(), BiomeFixer.swap(biome));
            } else {
                data = Features.DUNGEON.at(pos.getX(), pos.getY(), pos.getZ(), null, null, BiomeFixer.swap(biome), null);
            }
            SeedCracker.get().getDataStorage().addBaseData(data, DataAddedEvent.POKE_BIOMES);
            return result;
        }

        Vec3i size = this.getDungeonSize(pos);
        int[] floorCalls = this.getFloorCalls(size, pos);
        Dungeon.Data data = Features.DUNGEON.at(
                pos.getX(), pos.getY(), pos.getZ(), size, floorCalls, BiomeFixer.swap(biome), heightContext
        );
        SeedCracker.get().getDataStorage().addBaseData(data, data::onDataAdded);
        return result;
    }

    public Vec3i getDungeonSize(BlockPos spawnerPos) {
        int x = PosIterator.create(spawnerPos.offset(4, 3, -4), spawnerPos.offset(4, 3, 4)).stream()
                .filter(pos -> world.getBlockState(pos).getBlock() == Blocks.COBBLESTONE).count() > 2 ? 4 : 3;
        int z = PosIterator.create(spawnerPos.offset(-4, 3, 4), spawnerPos.offset(4, 3, 4)).stream()
                .filter(pos -> world.getBlockState(pos).getBlock() == Blocks.COBBLESTONE).count() > 2 ? 4 : 3;
        return new Vec3i(x, 0, z);
    }

    public int[] getFloorCalls(Vec3i dungeonSize, BlockPos spawnerPos) {
        int[] floorCalls = new int[(dungeonSize.getX() * 2 + 1) * (dungeonSize.getZ() * 2 + 1)];
        int i = 0;
        for (int xo = -dungeonSize.getX(); xo <= dungeonSize.getX(); xo++) {
            for (int zo = -dungeonSize.getZ(); zo <= dungeonSize.getZ(); zo++) {
                Block block = this.world.getBlockState(spawnerPos.offset(xo, -1, zo)).getBlock();
                if (block == Blocks.MOSSY_COBBLESTONE) {
                    floorCalls[i++] = Dungeon.Data.MOSSY_COBBLESTONE_CALL;
                } else if (block == Blocks.COBBLESTONE) {
                    floorCalls[i++] = Dungeon.Data.COBBLESTONE_CALL;
                } else if (block != Blocks.AIR && block != Blocks.CAVE_AIR) {
                    floorCalls[i++] = 2;
                } else {
                    floorCalls[i++] = 3;
                }
            }
        }
        return floorCalls;
    }

    @Override
    public boolean isValidDimension(DimensionType dimension) {
        return this.isOverworld(dimension);
    }
}
