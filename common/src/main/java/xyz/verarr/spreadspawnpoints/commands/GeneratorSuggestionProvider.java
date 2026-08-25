package xyz.verarr.spreadspawnpoints.commands;

import java.util.concurrent.CompletableFuture;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import xyz.verarr.spreadspawnpoints.spawnpoints.SpawnPointGenerator.SpawnPointGeneratorType;

public class GeneratorSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions>
    getSuggestions(CommandContext<CommandSourceStack> commandContext,
                   SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
        for (Identifier generator : SpawnPointGeneratorType.REGISTRY.keySet()) {
            suggestionsBuilder.suggest(generator.toString());
        }

        return suggestionsBuilder.buildFuture();
    }
}
