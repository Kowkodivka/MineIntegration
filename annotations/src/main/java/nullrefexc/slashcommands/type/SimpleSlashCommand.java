package nullrefexc.slashcommands.type;

import java.util.concurrent.TimeUnit;

public class SimpleSlashCommand extends SlashCommand {
    public final String methodName;
    public final SlashCommandParameter[] parameters;

    public SimpleSlashCommand(String methodName, String name, String description, SlashCommandParameter[] parameters, long timeout, TimeUnit timeoutUnit) {
        super(name, description, timeout, timeoutUnit);

        this.methodName = methodName;
        this.parameters = parameters;
    }
}
