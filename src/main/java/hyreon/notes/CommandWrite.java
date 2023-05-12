package hyreon.notes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandWrite implements CommandExecutor {

	final static String[] COMMANDS = {
			"name",
			"add",
			"set",
			"remove",
			"sign",
			"copy",
			"delete"
	};
	
	CommandInstance lastCommand;
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		lastCommand = new CommandInstance(sender, cmd, label, args);
		
		if (!lastCommand.validate()) {
			return true;
		}
		
		if (lastCommand.doCommand()) {
			lastCommand.updateInterface();
		}
		
		return true;
		
	}

	class CommandInstance {
		CommandSender sender;
		Command cmd;
		String label;
		String[] args;
		
		Player player;
		ItemStack item;
		ItemMeta itemMeta;
		List<String> itemLore;
		NoteData noteData;
		
		CommandInstance(CommandSender sender, Command cmd, String label, String[] args) {
			this.sender = sender;
			this.cmd = cmd;
			this.label = label;
			this.args = args;
		}

		/**
		 * Counts the sub-arguments to be expected in a command, including the text argument as one.
		 * @return
		 */
		private int numSubargs() {
			switch (args[0]) {
			case "add":
			case "set":
				return 2;
			case "name":
			case "sign":
			case "remove":
				return 1;
			case "copy":
			case "delete":
				return 0;
			default:
				return -1;
			}
		}

		private String getTextArgument(boolean name) {
			//ignores first argument (the command itself), so last subarg to array end
			
			if (numSubargs() > args.length - 1) {
				return "";
			}
			
			String[] subArray = Arrays.copyOfRange(args, numSubargs(), args.length);
			String prefix;
			if (name) {
				prefix = ChatColor.WHITE.toString();
			} else {
				prefix = ChatColor.WHITE + " ";
			}
			return prefix + ChatColor.translateAlternateColorCodes('&',StringUtils.join(subArray, " "));
		}
		
		private void updateInterface() {
			player.updateInventory();

			Notes.showEditorGui(new NoteData(itemMeta), player, true);
		}
		
		public void sendHelp() {
			sender.sendMessage("Unknown command. Try right-clicking paper?");
		}
		
		public boolean validate() {
			return validateSender() && validateCommand() && validateItem() && validatePaper() && validateSignStatus();
		}

		public boolean validateSender() {
			if (!(sender instanceof Player)) {
	            sender.sendMessage("Unfortunately, you cannot hold paper.");
	            return false;
	        }
			player = (Player) sender;
			return true;
		}
		
		public boolean validateCommand() {
			if (args.length <= 0) {
				sendHelp();
				return false;
			}
			for (String command : COMMANDS) {
				if (command.equalsIgnoreCase(args[0])) return true;
			}
			sendHelp();
			return false;
		}
		
		public boolean validateItem() {
			item = player.getInventory().getItemInMainHand();
			if (item == null || item.getType() == Material.AIR) {
	            player.sendMessage("Hey, you're kind of forgetting something. The paper?");
	            return false;
	        }
			return true;
		}

		private boolean validatePaper() {
			if (item.getType() != Material.PAPER) {
	            player.sendMessage("You probably don't want to try writing on that.");
	            return false;
	        }
			itemMeta = item.getItemMeta();
			itemLore = itemMeta.getLore();
			if (itemLore == null) itemLore = new ArrayList<String>();
			noteData = new NoteData(itemMeta);
			return true;
		}
		
		private boolean validateSignStatus() {
			if (noteData.getSignatures().size() > 0 && !legalAfterSigning(args[0])) {
	            return false;
	        }
			return true;
		}
		
		private boolean legalAfterSigning(String subcommand) {
			switch (subcommand) {
				case "sign":
					return true;
				case "delete":
					player.sendMessage("You can't just delete signed papers. Try burning it?");
					return false;
				case "copy":
					player.sendMessage("You can't copy over signed papers.");
					return false;
				default:
					player.sendMessage("This has been signed, no further changes can be made.");
					return false;
			}
		}
		
		private boolean doCommand() {
			switch (args[0]) {
			case "name":
				return name();
			case "add":
				return add();
			case "set":
				return set();
			case "remove":
				return remove();
			case "sign":
				return sign();
			case "copy":
				return copy();
			case "delete":
				return delete();
			default:
				player.sendMessage("Command is both valid and invalid! Congratulations?");
				return false;
			}
		}

		private boolean name() {
			String textArgument = getTextArgument(true);
			if (textArgument.equals("")) {
				player.sendMessage("You forgot the name.");
				return false;
			} else {
				itemMeta.setDisplayName(textArgument);
			}
			
			item.setItemMeta(itemMeta);
			
			return true;
			
		}

		private boolean add() {
			String textArgument = getTextArgument(false);
			if (textArgument.equals("")) {
				player.sendMessage("Please put the line you meant to add at the end of the command.");
				return false;
			} else {
				try {
					itemLore.add(Integer.parseInt(args[1]), textArgument);
					itemMeta.setLore(itemLore);
					item.setItemMeta(itemMeta);
				} catch (IndexOutOfBoundsException e) {
					player.sendMessage("Line not found.");
					return false;
				} catch (NumberFormatException e) {
					player.sendMessage("You forgot the line number.");
					return false;
				}
			}
			return true;
		}

		private boolean set() {
			String textArgument = getTextArgument(false);
			if (textArgument.equals("")) {
				player.sendMessage("Please put the line you meant to add at the end of the command.");
				return false;
			} else {
				try {
					itemLore.set(Integer.parseInt(args[1]), textArgument);
					itemMeta.setLore(itemLore);
					item.setItemMeta(itemMeta);
				} catch (IndexOutOfBoundsException e) {
					player.sendMessage("Line not found.");
					return false;
				} catch (NumberFormatException e) {
					player.sendMessage("You forgot the line number.");
					return false;
				}
			}
			return true;
		}

		private boolean remove() {
			try {
				itemLore.remove(Integer.parseInt(args[1]));
				itemMeta.setLore(itemLore);
				item.setItemMeta(itemMeta);
			} catch (IndexOutOfBoundsException e) {
				player.sendMessage("Line not found.");
				return false;
			} catch (NumberFormatException e) {
				player.sendMessage("You forgot the line number.");
				return false;
			}
			return true;
		}

		private boolean sign() {
			int stampLevel = 0; //how many times the stamp can be copied

            if (args.length > 1) {
                try {
                    stampLevel = Math.abs(Integer.parseInt(args[1]));
                } catch (NumberFormatException e) {
                    player.sendMessage("Please put in a number, or nothing at all.");
                    return false;
                }
            }

            if (noteData.hasSignature(player.getName())) {
                player.sendMessage("Remind me again why you need to sign twice?");
                return false;
            }

            String stampTrailingString;

            if (stampLevel == 0) {
                stampTrailingString = "";
            } else {
                stampTrailingString = " " + StringUtils.repeat(NoteData.SIGNATURE_SUFFIX, stampLevel);
            }

            itemLore.add(NoteData.SIGNATURE_PREFIX + player.getName() + stampTrailingString);
            itemMeta.setLore(itemLore);
            item.setItemMeta(itemMeta);
            
            return true;
		}

		private boolean copy() {
			ItemStack stampItem = player.getInventory().getItemInOffHand();
            if (stampItem == null || stampItem.getType() == Material.AIR) {
                player.sendMessage("Um, you're forgetting something. The stamp?");
                return false;
            }

            if (stampItem.getType() != Material.PAPER) {
                player.sendMessage("That doesn't work as a stamp. Grab a signed sheet of paper.");
                return false;
            }

            ItemMeta stampMeta = stampItem.getItemMeta();
            NoteData stampData = new NoteData(stampMeta);
            if (!stampData.isSigned()) {
                player.sendMessage("The paper must be signed to act as a stamp.");
                return false;
            }
            
            stampData.stamp(item);
            return true;
            
		}

		private boolean delete() {
			
			itemMeta.setDisplayName(null);
			itemMeta.setLore(null);
			item.setItemMeta(itemMeta);
			
			return true;
			
		}
		
	}

}
