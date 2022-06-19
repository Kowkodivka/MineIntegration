package nullrefexc.slashcommands;

import com.squareup.javapoet.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import nullrefexc.slashcommands.annotations.SlashSubcommand;
import nullrefexc.slashcommands.annotations.parameters.Required;
import nullrefexc.slashcommands.annotations.parameters.ParameterDescription;
import nullrefexc.slashcommands.annotations.parameters.ParameterName;
import nullrefexc.slashcommands.type.SimpleSlashCommand;
import nullrefexc.slashcommands.type.SlashCommandParameter;
import nullrefexc.slashcommands.type.SlashCommandWithSubcommands;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.Writer;
import java.util.*;

@SupportedAnnotationTypes({"nullrefexc.slashcommands.annotations.SlashCommand"})
public class SlashCommandAnnotationProcessor extends AbstractProcessor {
    private ProcessingEnvironment processingEnv;
    private Types typesUtils;
    private Elements elementUtils;
    private TypeMirror slashCommandInteractedEventType;
    private Map<TypeMirror, OptionType> parametersTypes;
    private Map<OptionType, String> parametersGetMethodsNames;
    private MethodSpec.Builder registerSlashCommandsMethodBuilder;
    private MethodSpec.Builder onSlashCommandInteractionListenerMethodBuilder;
    @Override
    public void init(ProcessingEnvironment procEnv) {
        super.init(procEnv);

        this.processingEnv = procEnv;
        this.typesUtils = procEnv.getTypeUtils();
        this.elementUtils = procEnv.getElementUtils();

        this.slashCommandInteractedEventType = elementUtils.getTypeElement("net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent").asType();

        initMethodsBuilders();
        initParametersTypes();
        initParametersGetMethodsNames();
    }
    private void initMethodsBuilders() {
        Element onSlashCommandInteractionListenerMethod = elementUtils.getTypeElement("net.dv8tion.jda.api.hooks.ListenerAdapter").getEnclosedElements().stream().filter(element -> element.getSimpleName().contentEquals("onSlashCommandInteraction")).findFirst().get();

        this.registerSlashCommandsMethodBuilder = MethodSpec.methodBuilder("registerSlashCommands")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(TypeName.get(elementUtils.getTypeElement("net.dv8tion.jda.api.entities.Guild").asType()), "guild");

        this.onSlashCommandInteractionListenerMethodBuilder = MethodSpec.overriding((ExecutableElement) onSlashCommandInteractionListenerMethod);
        this.onSlashCommandInteractionListenerMethodBuilder.addCode("switch (event.getName().toLowerCase()) { ");
    }
    private void initParametersTypes() {
        this.parametersTypes = new HashMap<>();
        this.parametersTypes.put(elementUtils.getTypeElement("java.lang.String").asType(), OptionType.STRING);
        this.parametersTypes.put(elementUtils.getTypeElement("java.lang.Long").asType(), OptionType.INTEGER);
        this.parametersTypes.put(typesUtils.getPrimitiveType(TypeKind.LONG), OptionType.INTEGER);
        this.parametersTypes.put(elementUtils.getTypeElement("java.lang.Boolean").asType(), OptionType.BOOLEAN);
        this.parametersTypes.put(typesUtils.getPrimitiveType(TypeKind.BOOLEAN), OptionType.BOOLEAN);
        this.parametersTypes.put(elementUtils.getTypeElement("net.dv8tion.jda.api.entities.User").asType(), OptionType.USER);
        this.parametersTypes.put(elementUtils.getTypeElement("net.dv8tion.jda.api.entities.GuildChannel").asType(), OptionType.CHANNEL);
        this.parametersTypes.put(elementUtils.getTypeElement("net.dv8tion.jda.api.entities.Role").asType(), OptionType.ROLE);
        this.parametersTypes.put(elementUtils.getTypeElement("net.dv8tion.jda.api.entities.IMentionable").asType(), OptionType.MENTIONABLE);
        this.parametersTypes.put(elementUtils.getTypeElement("java.lang.Double").asType(), OptionType.NUMBER);
        this.parametersTypes.put(typesUtils.getPrimitiveType(TypeKind.DOUBLE), OptionType.NUMBER);
        this.parametersTypes.put(elementUtils.getTypeElement("net.dv8tion.jda.api.entities.Message.Attachment").asType(), OptionType.ATTACHMENT);
    }
    private void initParametersGetMethodsNames() {
        this.parametersGetMethodsNames = new HashMap<>();
        this.parametersGetMethodsNames.put(OptionType.STRING, "getAsString");
        this.parametersGetMethodsNames.put(OptionType.INTEGER, "getAsLong");
        this.parametersGetMethodsNames.put(OptionType.BOOLEAN, "getAsBoolean");
        this.parametersGetMethodsNames.put(OptionType.USER, "getAsUser");
        this.parametersGetMethodsNames.put(OptionType.CHANNEL, "getAsGuildChannel");
        this.parametersGetMethodsNames.put(OptionType.ROLE, "getAsRole");
        this.parametersGetMethodsNames.put(OptionType.MENTIONABLE, "getAsMentionable");
        this.parametersGetMethodsNames.put(OptionType.NUMBER, "getAsDouble");
        this.parametersGetMethodsNames.put(OptionType.ATTACHMENT, "getAsAttachment");
    }
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (final Element annotatedElement : roundEnv.getElementsAnnotatedWith(nullrefexc.slashcommands.annotations.SlashCommand.class)) {
            if (annotatedElement instanceof ExecutableElement) {
                SimpleSlashCommand slashCommand = handleExecutableElement(annotatedElement);

                registerSlashCommand(slashCommand);
            } else if (annotatedElement instanceof TypeElement) {
                SlashCommandWithSubcommands slashCommandWithSubcommands = handleTypeElement(annotatedElement);

                registerSlashSubcommands(slashCommandWithSubcommands);
            }
        }

        writeCode("nullrefexc.gen.SlashCommandListener", generateCode());

        return true;
    }
    private SimpleSlashCommand handleExecutableElement(Element element) {
        final List<? extends VariableElement> parameters = ((ExecutableElement) element).getParameters();

        checkClassModifiers(element.getEnclosingElement());
        checkMethodModifiers(element);
        checkFirstParameter(parameters);

        nullrefexc.slashcommands.annotations.SlashCommand slashCommandAnnotation = element.getAnnotation(nullrefexc.slashcommands.annotations.SlashCommand.class);

        SlashCommandParameter[] slashCommandParameters = getSlashCommandParameters(parameters);

        String methodName = getFullElementName(element);

        return new SimpleSlashCommand(
                methodName,
                slashCommandAnnotation.name(),
                slashCommandAnnotation.description(),
                slashCommandParameters,
                slashCommandAnnotation.timeout(),
                slashCommandAnnotation.unit()
        );
    }
    private SlashCommandWithSubcommands handleTypeElement(Element element) {
        List<nullrefexc.slashcommands.type.SlashSubcommand> slashSubcommands = new ArrayList<>();

        element.getEnclosedElements().stream().filter(enclosedElement -> enclosedElement.getAnnotation(SlashSubcommand.class) != null).forEach(enclosedElement -> {
            final List<? extends VariableElement> parameters = ((ExecutableElement) enclosedElement).getParameters();

            checkClassModifiers(enclosedElement);
            checkMethodModifiers(enclosedElement);
            checkFirstParameter(parameters);

            nullrefexc.slashcommands.annotations.SlashSubcommand slashCommandAnnotation = enclosedElement.getAnnotation(nullrefexc.slashcommands.annotations.SlashSubcommand.class);

            SlashCommandParameter[] slashCommandParameters = getSlashCommandParameters(parameters);

            String methodName = getFullElementName(enclosedElement);

            slashSubcommands.add(new nullrefexc.slashcommands.type.SlashSubcommand(
                    slashCommandAnnotation.name(),
                    slashCommandAnnotation.description(),
                    methodName,
                    slashCommandParameters
            ));
        });

        nullrefexc.slashcommands.annotations.SlashCommand slashCommandAnnotation = element.getAnnotation(nullrefexc.slashcommands.annotations.SlashCommand.class);

        return new SlashCommandWithSubcommands(
            slashCommandAnnotation.name(),
            slashCommandAnnotation.description(),
            slashCommandAnnotation.timeout(),
            slashCommandAnnotation.unit(),
            slashSubcommands
        );
    }
    private void checkFirstParameter(List<? extends VariableElement> parameters) {
        if (parameters.size() < 1) {
            throw new RuntimeException("Slash command method must have not less than one parameter");
        }

        if (!typesUtils.isSameType(parameters.get(0).asType(), slashCommandInteractedEventType)) {
            throw new RuntimeException("First parameter of slash command method must be SlashCommandInteractionEvent");
        }
    }
    private void checkMethodModifiers(Element element) {
        final Set<Modifier> modifiers = element.getModifiers();

        if (!modifiers.contains(Modifier.PUBLIC) || !modifiers.contains(Modifier.STATIC)) {
            throw new RuntimeException("Slash command method must be public and static");
        }
    }
    private void checkClassModifiers(Element element) {
        Element parentElement;

        while ((parentElement = element.getEnclosingElement()) instanceof TypeElement) {
            final Set<Modifier> modifiers = parentElement.getModifiers();

            if (!modifiers.contains(Modifier.PUBLIC)) {
                throw new RuntimeException("Класс, содержащий слэш команды, должен быть публичным");
            }

            element = parentElement;
        }
    }
    private String getFullElementName(Element element) {
        String fullElementName = element.getSimpleName().toString();

        Element parentElement;

        while ((parentElement = element.getEnclosingElement()) instanceof TypeElement) {
            fullElementName = parentElement.getSimpleName().toString() + "." + fullElementName;

            element = parentElement;
        }

        return elementUtils.getPackageOf(element).getQualifiedName() + "." + fullElementName;
    }
    private OptionType getSlashCommandParameterType(VariableElement parameter) {
        TypeMirror parameterType = parameter.asType();

        for (Map.Entry<TypeMirror, OptionType> entry : parametersTypes.entrySet()) {
            if (typesUtils.isSameType(parameterType, entry.getKey())) {
                return entry.getValue();
            }
        }

        throw new RuntimeException("Unknown parameter type");
    }
    private SlashCommandParameter[] getSlashCommandParameters(List<? extends VariableElement> parameters) {
        SlashCommandParameter[] slashCommandParameters = new SlashCommandParameter[parameters.size() - 1];

        for (int i = 0; i < slashCommandParameters.length; i++) {
            VariableElement variableElement = parameters.get(i + 1);

            ParameterName name = variableElement.getAnnotation(ParameterName.class);
            ParameterDescription description = variableElement.getAnnotation(ParameterDescription.class);
            Required isRequiredAnnotation = variableElement.getAnnotation(Required.class);

            OptionType parameterType = getSlashCommandParameterType(variableElement);
            String parameterName = name != null ? name.name() : variableElement.getSimpleName().toString();
            String parameterDescription = description != null ? description.description() : "";
            boolean isRequired = isRequiredAnnotation != null;

            slashCommandParameters[i] = new SlashCommandParameter(parameterType, parameterName, parameterDescription, isRequired);
        }

        return slashCommandParameters;
    }
    private void registerSlashCommand(SimpleSlashCommand slashCommand) {
        registerSlashCommandsMethodBuilder.addCode("guild.upsertCommand($1S, $2S)", slashCommand.name, slashCommand.description);

        for (SlashCommandParameter parameter : slashCommand.parameters) {
            registerSlashCommandsMethodBuilder.addCode(".addOption(net.dv8tion.jda.api.interactions.commands.OptionType.$1L, $2S, $3S, $4L)", parameter.type.toString(), parameter.name, parameter.description, parameter.required);
        }

        registerSlashCommandsMethodBuilder.addCode(".timeout($1L, java.util.concurrent.TimeUnit.$2L)", slashCommand.timeout, slashCommand.timeoutUnit.toString());
        registerSlashCommandsMethodBuilder.addCode(".queue();");

        onSlashCommandInteractionListenerMethodBuilder.addCode("case $1S -> $2N(event", slashCommand.name, slashCommand.methodName);

        for (SlashCommandParameter parameter : slashCommand.parameters) {
            onSlashCommandInteractionListenerMethodBuilder.addCode(", event.getOption($1S).$2N()", parameter.name, parametersGetMethodsNames.get(parameter.type));
        }

        onSlashCommandInteractionListenerMethodBuilder.addCode(");");
    }
    private void registerSlashSubcommands(SlashCommandWithSubcommands slashCommand) {
        registerSlashCommandsMethodBuilder.addCode("guild.upsertCommand($1S, $2S)", slashCommand.name, slashCommand.description);

        registerSlashCommandsMethodBuilder.addCode(".timeout($1L, java.util.concurrent.TimeUnit.$2L)", slashCommand.timeout, slashCommand.timeoutUnit.toString());

        for (nullrefexc.slashcommands.type.SlashSubcommand slashSubcommand : slashCommand.subcommands) {
            registerSlashCommandsMethodBuilder.addCode(".addSubcommands(new net.dv8tion.jda.api.interactions.commands.build.SubcommandData($1S, $2S)", slashSubcommand.name, slashSubcommand.description);
            for (SlashCommandParameter parameter : slashSubcommand.parameters) {
                registerSlashCommandsMethodBuilder.addCode(".addOption(net.dv8tion.jda.api.interactions.commands.OptionType.$1L, $2S, $3S, $4L)", parameter.type.toString(), parameter.name, parameter.description, parameter.required);
            }
            registerSlashCommandsMethodBuilder.addCode(")");
        }

        registerSlashCommandsMethodBuilder.addCode(".queue();");

        onSlashCommandInteractionListenerMethodBuilder.addCode("case $1S -> {", slashCommand.name);
        onSlashCommandInteractionListenerMethodBuilder.addCode("switch (event.getSubcommandName()) {");
        for (nullrefexc.slashcommands.type.SlashSubcommand slashSubcommand : slashCommand.subcommands) {
            onSlashCommandInteractionListenerMethodBuilder.addCode("case $1S -> $2N(event", slashSubcommand.name, slashSubcommand.methodName);

            for (SlashCommandParameter parameter : slashSubcommand.parameters) {
                onSlashCommandInteractionListenerMethodBuilder.addCode(", event.getOption($1S).$2N()", parameter.name, parametersGetMethodsNames.get(parameter.type));
            }

            onSlashCommandInteractionListenerMethodBuilder.addCode(");");
        }
        onSlashCommandInteractionListenerMethodBuilder.addCode("}");
        onSlashCommandInteractionListenerMethodBuilder.addCode("}");
    }

    private String generateCode() {
        registerSlashCommandsMethodBuilder.addCode("guild.updateCommands().queue();");
        onSlashCommandInteractionListenerMethodBuilder.addCode("}");

        TypeSpec slashCommandListenerClass = TypeSpec.classBuilder("SlashCommandListener")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(elementUtils.getTypeElement("net.dv8tion.jda.api.hooks.ListenerAdapter").asType())
                .addMethod(registerSlashCommandsMethodBuilder.build())
                .addMethod(onSlashCommandInteractionListenerMethodBuilder.build())
                .build();

        return JavaFile.builder("nullrefexc.gen", slashCommandListenerClass)
                .build()
                .toString();
    }

    private void writeCode(String filename, String code) {
        try (Writer file = processingEnv.getFiler().createSourceFile(filename).openWriter()) {
            file.write(code);
            file.flush();
            file.close();
        } catch (Exception e) {}
    }
}