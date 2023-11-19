package com.mizfit.citems;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CItems extends JavaPlugin {

    private static CItems instance;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        // Load configuration
        saveResource("config.yml", false); // Use false to not overwrite if the file already exists
        reloadConfig();
        getLogger().info("Plugin data folder: " + getDataFolder());
        getLogger().info("Plugin file: " + getFile());

        // Register event listeners
        getServer().getPluginManager().registerEvents(new HoeEventListener(), this);
        // Register other event listeners for additional custom items

        // Initialize and register custom items
        CustomHoe customHoe = new CustomHoe(1, getName());
        // Initialize and register other custom items

        // Register commands and other initialization here

        if (!setupEconomy()) {
            getLogger().warning("No Economy plugin found. Disabling Vault");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    public static CItems getInstance() {
        return instance;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public class SpawnVillagerCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can execute this command.");
                return true;
            }

            Player player = (Player) sender;
            Location spawnLocation = player.getLocation();

            spawnFarmerVillager(spawnLocation);
            player.sendMessage("Spawned a farmer villager at your location.");

            return true;
        }
    }

    // Method to spawn the "farmer" Villager in spawn location
    public void spawnFarmerVillager(Location spawnLocation) {
        Villager farmerVillager = (Villager) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.VILLAGER);
        farmerVillager.setProfession(Villager.Profession.FARMER);
        farmerVillager.setCustomName("Hoe Salesman");
        farmerVillager.setCustomNameVisible(true);

        // Add trades for the Villager (adjust as needed)
        farmerVillager.getInventory().addItem(new ItemStack(Material.DIAMOND_HOE));
    }

    // Event handler for player interacting with the Villager
    @EventHandler
    public void onVillagerInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            if (villager.getCustomName() != null && villager.getCustomName().equals("Hoe Salesman")) {
                Player player = event.getPlayer();
                purchaseCustomHoe(player);
            }
        }
    }

    // Method to handle purchasing the CustomHoe
    public void purchaseCustomHoe(Player player) {
        CustomHoe customHoe = new CustomHoe(1, player.getName()); // Create a CustomHoe instance, adjust parameters as needed

        if (econ != null && econ.has(player, 3000000)) {
            econ.withdrawPlayer(player, 3000000);
            player.getInventory().addItem(customHoe);
            player.sendMessage("You purchased a Level 1 Hoe!");
        } else {
            player.sendMessage(ChatColor.RED + "You don't have enough money to purchase the hoe.");
        }
    }
}
