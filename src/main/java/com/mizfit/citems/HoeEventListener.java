package com.mizfit.citems;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class HoeEventListener implements Listener {

    @EventHandler
    public void onHoeUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.DIAMOND_HOE && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof Damageable) {
                Damageable damageable = (Damageable) meta;
                if (damageable.getDamage() == item.getType().getMaxDurability()) {
                    CustomHoe customHoe = CustomHoe.fromItemStack(item);
                    if (customHoe != null && customHoe.getLevel() >= 0 && customHoe.getOwner().equals(player.getName())) {
                        if (Math.random() < (customHoe.getLevel() * 0.02)) {
                            // Replant the crop here (simulate replanting)
                            player.getWorld().dropItemNaturally(event.getClickedBlock().getLocation(), new ItemStack(Material.WHEAT));
                        }
                    }
                }
            }
        }
    }
}
