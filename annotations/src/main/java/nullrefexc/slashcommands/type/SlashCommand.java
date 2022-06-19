package nullrefexc.slashcommands.type;

import java.util.concurrent.TimeUnit;

public class SlashCommand {
    public final String name;
    public final String description;
    public final long timeout;
    public final TimeUnit timeoutUnit;

    public SlashCommand(String name, String description, long timeout, TimeUnit timeoutUnit) {
        this.name = name;
        this.description = description;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
    }
}
