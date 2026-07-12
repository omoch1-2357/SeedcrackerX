package kaptainwutax.seedcrackerX.hud;

import kaptainwutax.seedcrackerX.SeedCracker;
import kaptainwutax.seedcrackerX.cracker.storage.DataStorage;
import kaptainwutax.seedcrackerX.cracker.storage.TimeMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.CommonColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Read-only HUD for the local seed-identification state.
 *
 * This class only reads in-memory cracker state and submits text to Minecraft's
 * client HUD extractor. It does not send packets or expose data to the server.
 */
public final class SeedProgressHud {
    private static final int X = 6;
    private static final int Y = 6;
    private static final int LINE_HEIGHT = 10;
    private static final int PADDING = 4;
    private static final int BACKGROUND = 0x90000000;
    private static final int TITLE = 0xFF9EDCFF;
    private static final int LABEL = 0xFFB8B8B8;
    private static final int VALUE = CommonColors.WHITE;
    private static final int SUCCESS = 0xFF65E572;

    private SeedProgressHud() {
    }

    public static void extract(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        SeedCracker cracker = SeedCracker.get();
        if (minecraft.player == null || cracker == null) return;

        DataStorage storage = cracker.getDataStorage();
        TimeMachine machine = storage.getTimeMachine();

        List<Line> lines = buildLines(storage, machine);
        int width = 0;
        for (Line line : lines) {
            width = Math.max(width, minecraft.font.width(line.text));
        }

        int height = lines.size() * LINE_HEIGHT + PADDING * 2;
        graphics.fill(X, Y, X + width + PADDING * 2, Y + height, BACKGROUND);

        int textY = Y + PADDING;
        for (Line line : lines) {
            graphics.text(minecraft.font, line.text, X + PADDING, textY, line.color);
            textY += LINE_HEIGHT;
        }
    }

    private static List<Line> buildLines(DataStorage storage, TimeMachine machine) {
        List<Line> lines = new ArrayList<>();
        lines.add(new Line("Lucent Pipeline — Seed", TITLE));

        int worldCandidates = machine.worldSeeds.size();
        int structureCandidates = machine.structureSeeds.size();
        int observations = storage.getObservationCount();
        int biomeObservations = storage.getBiomeObservationCount();
        double baseBits = storage.getBaseBits();
        double wantedBits = storage.getWantedBits();
        boolean hashedSeed = storage.hashedSeedData != null && storage.hashedSeedData.getHashedSeed() != 0;

        if (worldCandidates == 1) {
            long seed = machine.worldSeeds.iterator().next();
            lines.add(new Line("Status: identified", SUCCESS));
            lines.add(new Line("World seed: " + seed, SUCCESS));
            return lines;
        }

        lines.add(new Line("Status: " + status(storage, machine), VALUE));
        lines.add(new Line("Observations: " + observations, LABEL));
        lines.add(new Line(String.format(Locale.ROOT, "Structure data: %.1f / %.1f bits", baseBits, wantedBits), LABEL));
        lines.add(new Line("Biome observations: " + biomeObservations, LABEL));
        lines.add(new Line("Structure candidates: " + candidateText(structureCandidates), VALUE));
        lines.add(new Line("World candidates: " + candidateText(worldCandidates), VALUE));
        lines.add(new Line("Hashed seed: " + (hashedSeed ? "yes" : "no"), hashedSeed ? SUCCESS : LABEL));

        int progress = (int) Math.round(Math.min(1.0, baseBits / wantedBits) * 100.0);
        lines.add(new Line("Collection progress: " + progress + "%", VALUE));
        return lines;
    }

    private static String status(DataStorage storage, TimeMachine machine) {
        if (!machine.worldSeeds.isEmpty()) return "filtering world seeds";
        if (!machine.structureSeeds.isEmpty()) {
            return storage.notEnoughBiomeData() ? "collecting biome data" : "searching world seed";
        }
        if (machine.isRunning && storage.getBaseBits() >= storage.getWantedBits()) {
            return "searching structure seed";
        }
        if (machine.isRunning) return "processing observations";
        return "collecting observations";
    }

    private static String candidateText(int count) {
        return count == 0 ? "not generated" : Integer.toString(count);
    }

    private record Line(String text, int color) {
    }
}
