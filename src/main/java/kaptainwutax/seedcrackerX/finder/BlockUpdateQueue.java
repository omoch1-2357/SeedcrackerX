package kaptainwutax.seedcrackerX.finder;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Passive local-only queue.
 *
 * Upstream used ServerboundPlayerActionPacket packets here to force block
 * updates for an anti-xray bypass. This fork deliberately never sends packets
 * to the server. The queue is retained only for binary/source compatibility;
 * callers should not rely on active probing.
 */
public class BlockUpdateQueue {
    private final Queue<Pair<Thread, ArrayList<BlockPos>>> blocksAndAction = new LinkedList<>();
    private final HashSet<BlockPos> alreadyChecked = new HashSet<>();

    public boolean add(ArrayList<BlockPos> blockPoses, BlockPos originPos, Thread operationAtEnd) {
        if (alreadyChecked.add(originPos)) {
            blocksAndAction.add(new Pair<>(operationAtEnd, blockPoses));
            return true;
        }
        return false;
    }

    public void tick() {
        // Intentionally do nothing. Active block-update probing would require
        // sending serverbound packets, which is forbidden in this fork.
        blocksAndAction.clear();
    }
}
