package kaptainwutax.seedcrackerX.util;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Local-only stub.
 *
 * The upstream implementation can fetch and submit cracked seeds through
 * external services and can perform an authentication request. This fork is
 * intentionally passive: it never performs those network operations.
 */
public final class Database {

    private Database() {
    }

    public static Component joinFakeServerForAuth() {
        return null;
    }

    public static @Nullable Long getSeed(String connection, long hashedSeed) {
        return null;
    }

    public static void handleDatabaseCall(Long seed) {
        // Intentionally disabled in the passive local-only build.
    }

    public static void fetchSeeds() {
        // Intentionally disabled in the passive local-only build.
    }
}
