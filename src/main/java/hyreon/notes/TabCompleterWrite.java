package hyreon.notes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TabCompleterWrite implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        NoteData noteData;
        NoteData stampData;
        try {
            noteData =
                    new NoteData(Objects.requireNonNull(((Player) commandSender).getInventory().getItemInMainHand().getItemMeta()));
        } catch (NullPointerException e) {
            return new ArrayList<>();
        }

        try {
            stampData =
                    new NoteData(Objects.requireNonNull(((Player) commandSender).getInventory().getItemInOffHand().getItemMeta()));
        } catch (NullPointerException e) {
            stampData = null;
        }

        if (args.length == 1) {
            List<String> outputs = new ArrayList<>();
            if (!noteData.isSigned()) {
                if (noteData.isPlain()) {
                    if (stampData != null && stampData.isSigned()) {
                        outputs.add("copy");
                    }
                    outputs.add("name"); //first result if unnamed
                }
                outputs.add("add");
                if (!noteData.getDescription().isEmpty()) {
                    outputs.add("set");
                    outputs.add("remove");
                }
                if (!noteData.isPlain()) {
                    outputs.add("name");
                    outputs.add("delete");
                }
            }
            outputs.add("sign");
            return outputs;
        } else if ((args[0].contains("add") || args[0].contains("set") || args[0].contains("remove")) && args.length == 2) {
            //get number of lines
            int numLines = noteData.getDescription().size();
            if (!args[0].contains("add")) numLines--;
            return IntStream.rangeClosed(0, numLines)
                    .boxed().map(Object::toString).collect(Collectors.toList());
        } else if (args[0].contains("sign") && args.length == 2) {
            return Arrays.asList("0", "1");
        } else {
            return new ArrayList<>();
        }

    }
}
