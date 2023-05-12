package hyreon.notes;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class Notes extends JavaPlugin {
	
	static Notes instance;
	private boolean apiHasFailed = false;

	@Override
    public void onEnable() {
    	instance = this;
        getCommand("note").setExecutor(new CommandWrite());
        getCommand("note").setTabCompleter(new TabCompleterWrite());
        getServer().getPluginManager().registerEvents(new NotesListener(), this);
    }

	public static Notes getInstance() {
		return instance;
	}
	
	public static void showEditorGui(NoteData noteData, Player player, boolean fromCommand) {
    	if (!instance.apiHasFailed) {
			try {
				if (noteData.isPlain()) {
					player.spigot().sendMessage(newJsonText(noteData).create());
				} else if (noteData.isSigned()) {
					player.spigot().sendMessage(signedJsonText(noteData, player.getName()).create());
				} else {
					player.spigot().sendMessage(editorJsonText(noteData).create());
				}
			} catch (IllegalStateException e) { //connection implementation failed
				Notes.getInstance().getLogger().log(Level.WARNING, "Unable to update NoteWrite chat interface due to API changes.");
				player.sendMessage(ChatColor.RED + "Unable to load GUI. Contact server staff if you believe this is in error.");
				instance.apiHasFailed = true;
			}
		} else {
    		if (fromCommand) {
				player.sendMessage(ChatColor.WHITE + "Edit successful.");
			} else {
				player.sendMessage(ChatColor.WHITE + "Use commands to edit this note.");
			}
		}
	}

	public static void showStampGui(NoteData noteData, Player player) {
		ComponentBuilder jsonText = new ComponentBuilder();
		generateCopyComponent(jsonText);
		jsonText.append(" Copy '" + noteData.getName() + ChatColor.WHITE + "'");
		player.spigot().sendMessage(jsonText.create());
	}
	
	private static ComponentBuilder newJsonText(NoteData noteData) {
		ComponentBuilder jsonText = new ComponentBuilder();
		generateNameComponent(jsonText, true, "");
		return jsonText.append(" Create Note");
	}
	
	private static ComponentBuilder signedJsonText(NoteData noteData, String signature) {
		ComponentBuilder jsonText = new ComponentBuilder().append(noteData.getName() + "\n");
		for (String loreLine : noteData.getDescription()) {
			jsonText.append(loreLine + "\n");
		}
		
		if (!noteData.hasSignature(signature)) {
			generateSignComponent(jsonText);
			jsonText.append(" ");
		}
		
		jsonText.append(
				StringUtils.join(
						noteData.getSignatures()
								.stream().map(NoteData::stripGenerations).toArray(),
						", "
				)
		);
		
		return jsonText;
	}

	private static ComponentBuilder editorJsonText(NoteData noteData) {
		ComponentBuilder jsonText = new ComponentBuilder();
		generateDeleteComponent(jsonText);
		jsonText.append(" ");
		generateNameComponent(jsonText, false, noteData.getName());
		jsonText.append(" " + noteData.getName() + "\n");
		for (int i = 0; i < noteData.getDescription().size(); i++) {
			
			String loreLine = noteData.getDescription().get(i);
			generateLine(jsonText, loreLine, i);
			
		}
		generateEnd(jsonText, noteData.getDescription().size());
		return jsonText;
	}

	private static void generateLine(ComponentBuilder message, String loreLine, int index) {
		
		generateAddComponent(message, index, false);
		message.append(" ");
		generateSetComponent(message, index, loreLine);
		message.append(" ");
		generateRemoveComponent(message, index);
		message.append(" ")
			.append(loreLine)
			.append("\n");
		
	}
	
	private static void generateNameComponent(ComponentBuilder message, boolean newNote, String name) {
		TextComponent component = new TextComponent("[*]");
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Rename this note").create()));
		if (newNote) {
			component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/note name "));
		} else {
			component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/note name " + sanitize(name, false)));
		}
		component.setColor(ChatColor.AQUA.asBungee());
		message.append(component);
	}

	private static void generateAddComponent(ComponentBuilder message, int index, boolean lastLine) {
		TextComponent component = new TextComponent("[+]");
		component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/note add "+index+" "));
		if (lastLine) {
			component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Add new line").create()));
		} else {
			component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Add before this line").create()));
		}
		component.setColor(ChatColor.GREEN.asBungee());
		message.append(component);
	}
	
	private static void generateSetComponent(ComponentBuilder message, int index, String line) {
		TextComponent component = new TextComponent("[*]");
		component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/note set "+index+" "+sanitize(line, true)));
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Change this line").create()));
		component.setColor(ChatColor.YELLOW.asBungee());
		message.append(component);
	}
	
	private static void generateRemoveComponent(ComponentBuilder message, int index) {
		TextComponent component = new TextComponent("[-]");
		component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/note remove "+index));
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Remove this line").create()));
		component.setColor(ChatColor.RED.asBungee());
		message.append(component);
	}
	
	private static void generateDeleteComponent(ComponentBuilder message) {
		TextComponent component = new TextComponent("[-]");
		component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/note delete"));
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Delete the entire note").create()));
		component.setColor(ChatColor.DARK_RED.asBungee());
		message.append(component);
	}
	
	private static void generateSignComponent(ComponentBuilder message) {
		TextComponent component = new TextComponent("[x]");
		component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/note sign"));
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Irreversibly sign this note\n" + ChatColor.GRAY + "To approve for copies, put in the number of generations you'll allow").create()));
		component.setColor(ChatColor.GOLD.asBungee());
		message.append(component);
	}
	
	private static void generateCopyComponent(ComponentBuilder message) {
		TextComponent component = new TextComponent("[~]");
		component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/note copy"));
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Copy offhand to main hand").create()));
		component.setColor(ChatColor.LIGHT_PURPLE.asBungee());
		message.append(component);
	}
	
	private static void generateEnd(ComponentBuilder message, int index) {
		generateAddComponent(message, index, true);
		message.append(" ");
		generateSignComponent(message);
	}
	
	private static String sanitize(String text, boolean withSpace) {
		if (text.startsWith(ChatColor.WHITE.toString())) {
			text = text.substring(2);
		}
		if (withSpace) {
			text = text.substring(1);
		}
		return text.replace("§", "&");
	}
	
}
