package com.mizfit.citems;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CustomHoe extends ItemStack {
    // Discourage use of static

    private static final NamespacedKey LEVEL_KEY = new NamespacedKey(CItems.getInstance(), "level");
    private static final NamespacedKey OWNER_KEY = new NamespacedKey(CItems.getInstance(), "owner");
    private static final NamespacedKey TOTAL_ITEMS_KEY = new NamespacedKey(CItems.getInstance(), "totalItems");

    public CustomHoe(int level, String owner) {
        super(Material.DIAMOND_HOE);
        setLevel(level);
        setOwner(owner);
        updateDisplayName();
        updateDurability();
        setTotalItems(getTotalItemsForLevel(level)); // Set total items when creating the hoe
        saveOwnerData();
    }
    private void updateDisplayName() {
        ItemMeta meta = getItemMeta();
        // change this to use the Component stuff like we saw in the other event handler for the villagers
        meta.setDisplayName("Level " + getLevel() + " Dynamic Hoe");
        setItemMeta(meta);
    }
    public void setCustomHoeName(String name) {
        ItemMeta meta = getItemMeta();
        meta.setDisplayName(name);
        setItemMeta(meta);
    }
    public static CustomHoe fromItemStack(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(LEVEL_KEY, PersistentDataType.INTEGER) ||
                !meta.getPersistentDataContainer().has(OWNER_KEY, PersistentDataType.STRING) ||
                !meta.getPersistentDataContainer().has(TOTAL_ITEMS_KEY, PersistentDataType.INTEGER)) {
            return null;  // Invalid custom hoe
        }

        int level = meta.getPersistentDataContainer().get(LEVEL_KEY, PersistentDataType.INTEGER);
        String owner = meta.getPersistentDataContainer().get(OWNER_KEY, PersistentDataType.STRING);
        int totalItems = meta.getPersistentDataContainer().get(TOTAL_ITEMS_KEY, PersistentDataType.INTEGER);

        CustomHoe customHoe = new CustomHoe(level, owner);
        customHoe.setItemMeta(meta);
        customHoe.updateDurability();  // Update durability when creating from ItemStack
        return customHoe;
    }

    private void setLevel(int level) {
        ItemMeta meta = getItemMeta();
        meta.getPersistentDataContainer().set(LEVEL_KEY, PersistentDataType.INTEGER, level);
        setItemMeta(meta);
    }

    private void setOwner(String owner) {
        ItemMeta meta = getItemMeta();
        meta.getPersistentDataContainer().set(OWNER_KEY, PersistentDataType.STRING, owner);
        setItemMeta(meta);
    }


    private void saveOwnerData() {
        ItemMeta meta = getItemMeta();
        meta.getPersistentDataContainer().set(OWNER_KEY, PersistentDataType.STRING, getOwner());
        setItemMeta(meta);
    }

    public int getLevel() {
        ItemMeta meta = getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(LEVEL_KEY, PersistentDataType.INTEGER)) {
            return meta.getPersistentDataContainer().get(LEVEL_KEY, PersistentDataType.INTEGER);
        }
        return 0; // Default level if not found
    }

    public String getOwner() {
        ItemMeta meta = getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(OWNER_KEY, PersistentDataType.STRING)) {
            return meta.getPersistentDataContainer().get(OWNER_KEY, PersistentDataType.STRING);
        }
        return ""; // Default owner if not found
    }
    private void setTotalItems(int totalItems) {
        ItemMeta meta = getItemMeta();
        meta.getPersistentDataContainer().set(TOTAL_ITEMS_KEY, PersistentDataType.INTEGER, totalItems);
        setItemMeta(meta);
    }
    public int getTotalItemsForLevel(int level) {
        FileConfiguration config = CItems.getInstance().getConfig();
        ConfigurationSection levelsSection = config.getConfigurationSection("levels");

        if (levelsSection != null && levelsSection.contains(String.valueOf(level))) {
            return levelsSection.getInt(String.valueOf(level), 0);
        }

        return 0; // Default to 0 if not found
    }

    public void updateDurability() {
        int maxDurability = getTotalItemsForLevel(getLevel());
        int currentDurability = (int) ((double) maxDurability * ((double) getLevel() / 100)); // Assuming max level is 100

        // Check if leveling up progress reaches the maximum
        if (currentDurability >= maxDurability) {
            // Reset durability to 0
            setDurability((short) 0);

            // Reset max durability to the amount needed for the next level
            int nextLevel = getLevel() + 1;
            setLevel(nextLevel);

            // Set max durability to the amount needed for the next level
            int nextLevelItems = getTotalItemsForLevel(nextLevel);
            setTotalItems(nextLevelItems);
        } else {
            // Update current durability
            setDurability((short) (maxDurability - currentDurability));
        }
    }
}
