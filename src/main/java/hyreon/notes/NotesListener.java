package hyreon.notes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class NotesListener implements Listener {
	
	@EventHandler
	public static void OnRightClickNote(PlayerInteractEvent e) {
		
		if (!e.getAction().equals(Action.RIGHT_CLICK_AIR) &&
				!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		
		if (e.getItem() == null ||
				e.getItem().getType() != Material.PAPER) {
			return;
		}
		
		NoteData noteData = new NoteData(e.getItem().getItemMeta());


		
		if (e.getHand().equals(EquipmentSlot.OFF_HAND)) {
			Player player = e.getPlayer();
			
			if (player.getInventory().getItemInMainHand() == null) {
				//pass
			} else if (player.getInventory().getItemInMainHand().getType() != Material.PAPER) {
				//pass
			} else if (noteData.isPlain()) {
				//pass
			} else {
				Notes.showStampGui(noteData, e.getPlayer());
			}
		} else {
			Notes.showEditorGui(noteData, e.getPlayer(), false);
		}
		
	}
	
}
