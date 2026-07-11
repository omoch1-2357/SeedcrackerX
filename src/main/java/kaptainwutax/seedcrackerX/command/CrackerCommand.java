package kaptainwutax.seedcrackerX.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import kaptainwutax.seedcrackerX.config.Config;
import kaptainwutax.seedcrackerX.util.Log;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class CrackerCommand extends ClientCommand {

    @Override
    public String getName() {
        return "cracker";
    }

    @Override
    public void build(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        builder.then(literal("ON").executes(context -> this.setActive(true)))
                .then(literal("OFF").executes(context -> this.setActive(false)))
                .executes(context -> this.toggleActive());
    }

    private void feedback(boolean success, boolean flag) {
        String action = Log.translate(flag ? "cracker.enabled" : "cracker.disabled");
        if (success) {
            sendFeedback(Log.translate("cracker.successfully") + action, ChatFormatting.GREEN);
        } else {
            sendFeedback(Log.translate("cracker.already") + action, ChatFormatting.RED);
        }
        Config.save();
    }

    private int setActive(boolean flag) {
        feedback(Config.get().active != flag, flag);
        Config.get().active = flag;
        return 0;
    }

    private int toggleActive() {
        Config.get().active = !Config.get().active;
        feedback(true, Config.get().active);
        return 0;
    }
}
