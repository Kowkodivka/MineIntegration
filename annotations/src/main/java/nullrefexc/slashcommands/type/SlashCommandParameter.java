package nullrefexc.slashcommands.type;

import net.dv8tion.jda.api.interactions.commands.OptionType;

public class SlashCommandParameter {
    public final OptionType type;
    public final String name;
    public final String description;
    public final boolean required;
    public SlashCommandParameter(OptionType type, String name, String description, boolean required) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.required = required;
    }
}
