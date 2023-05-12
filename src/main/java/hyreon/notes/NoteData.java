package hyreon.notes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NoteData {

	public static final String SIGNATURE_PREFIX = ChatColor.GRAY.toString() + ChatColor.ITALIC;
	public static final String SIGNATURE_SUFFIX = "*";
	
	private String name;

	private List<String> description;
	private List<String> signatures;
	
	public NoteData(ItemMeta itemMeta) {
		name = itemMeta.getDisplayName();
		description = itemMeta.getLore();
		if (description == null) description = new ArrayList<>();
		signatures = popSignatures(description);
	}

	private List<String> popSignatures(List<String> description) {
		
		List<String> signatures = new ArrayList<>();
		for (String line : description) {
			if (line.startsWith(SIGNATURE_PREFIX)) {
				signatures.add(line);
			}
		}
		description.removeAll(signatures);
		return signatures;
	}
	
	public String getName() {
		if (name.isEmpty()) return "Unnamed Note";
		else return name;
	}

	public List<String> getDescription() {
		return description;
	}

	public List<String> getSignatures() {
		return signatures;
	}
	
	public boolean isSigned() {
		return signatures.size() > 0;
	}

	public boolean hasSignature(String name) {
		for (String signature : signatures) {
			if (name.equals(ChatColor.stripColor(signature.replace("*", "").replaceAll(" ", "")))) return true;
		}
		return false;
	}

	public void stamp(ItemStack item) {
		ItemMeta itemMeta = item.getItemMeta();
		assert itemMeta != null;
		itemMeta.setDisplayName(getName());
		itemMeta.setLore(getFullCopiedLore());
		
		item.setItemMeta(itemMeta);
	}

	private List<String> getFullCopiedLore() {
		List<String> fullLore = new ArrayList<>(getDescription());
		for (String signature : getSignatures()) {
			String copiedSignature = copiedSignature(signature);
			if (copiedSignature == null) continue;
			fullLore.add(copiedSignature);
		}
		return fullLore;
	}

	private String copiedSignature(String signature) {
		if (signature.endsWith(SIGNATURE_SUFFIX)) {
			return signature.substring(0, signature.length() - 1).trim();
		} else {
			return null;
		}
	}

	public boolean isPlain() {
		return name.isEmpty() && description.size() == 0 && signatures.size() == 0;
	}

	public static String stripGenerations(String signature) {
		return signature.replace("*", "").replace(" ", "");
	}
	
}
