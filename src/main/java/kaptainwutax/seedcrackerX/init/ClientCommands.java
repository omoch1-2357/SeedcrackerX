package kaptainwutax.seedcrackerX.init;

import com.mojang.brigadier.CommandDispatcher;
import kaptainwutax.seedcrackerX.command.ClientCommand;
import kaptainwutax.seedcrackerX.command.CrackerCommand;
import kaptainwutax.seedcrackerX.command.DataCommand;
import kaptainwutax.seedcrackerX.command.FinderCommand;
import kaptainwutax.seedcrackerX.command.VersionCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.ArrayList;
import java.util.List;

public class ClientCommands {

    public static final String PREFIX = "seedcracker";
    public static final List<ClientCommand> COMMANDS = new ArrayList<>();

    public static FinderCommand FINDER;
    public static DataCommand DATA;
    public static CrackerCommand CRACKER;
    public static VersionCommand VERSION;

    static {
        COMMANDS.add(FINDER = new FinderCommand());
        COMMANDS.add(DATA = new DataCommand());
        COMMANDS.add(CRACKER = new CrackerCommand());
        COMMANDS.add(VERSION = new VersionCommand());
    }

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        COMMANDS.forEach(clientCommand -> clientCommand.register(dispatcher));
    }
}
