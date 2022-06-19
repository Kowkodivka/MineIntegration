package nullrefexc.slashcommands.type;

public class SlashSubcommand {
    public final String name;
    public final String description;
    public final String methodName;
    public final SlashCommandParameter[] parameters;

    public SlashSubcommand(String name, String description, String methodName, SlashCommandParameter[] parameters) {
        this.name = name;
        this.description = description;
        this.methodName = methodName;
        this.parameters = parameters;
    }
}
