package nullrefexc.slashcommands.type;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SlashCommandWithSubcommands extends SlashCommand {
    public final List<SlashSubcommand> subcommands;

    public SlashCommandWithSubcommands(String name, String description, long timeout, TimeUnit timeoutUnit, List<SlashSubcommand> subcommands) {
        super(name, description, timeout, timeoutUnit);

        this.subcommands = subcommands;
    }
}
