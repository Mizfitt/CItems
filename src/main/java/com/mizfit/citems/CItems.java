package com.mizfit.citems;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

    /*
    - Good Job on not adding the .idea/ folder for the following reason
    - Always try and have a .gitignore file in the root of the repository https://www.freecodecamp.org/news/gitignore-what-is-it-and-how-to-add-to-repo/,
        git ignore files are good practice to remove files that are specific to your machine such as .idea, jar files, etc...
    - No Dependencies in paper-plugin.yml https://docs.papermc.io/paper/dev/getting-started/paper-plugins

     */


    // Usage of 'static' variables is not recommended, instead use "dependency injection"
    // Read Constructor Injection at https://medium.com/groupon-eng/dependency-injection-in-java-9e9438aa55ae for info on Dependency Injection

    private static CItems instance;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;

        // This is a good way of doing configurations, however this is one of the more manual methods and you will eventually run into problems with it
        // I would recommend using a library such as Configurate https://github.com/SpongePowered/Configurate

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

        if (!setupEconomy()) {
            getLogger().warning("No Economy plugin found. Disabling Vault");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    public static CItems getInstance() {
        return instance;
    }

//    .\\ add Vault
    // The comment above is incorrectly formatted, java comments are formatted with // not .\\


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
    // Initialize and register other custom items

    // Register commands and other initialization here

    // command is never registered

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


    // Overall, a working method for the villager.
    // There is a better way to determine who the villager is, as eventually custom names could conflict, and someone
    // use a nametag or a plugin feature to rename a villager to the same name and cheat in items.

    // One of the best ways to do this is the Persistent Data Container (it's like a NBT tag)
    // https://docs.papermc.io/paper/dev/pdc

    // Personally, I would recommend using the Citizens plugin as a dependency, and then using the Citizens API to spawn the villager NPC and handle it.
    // Citizens is a great plugin for managing NPCs, and you'll already have it downloaded because i'll be using it for the minions plugin.
    // https://github.com/CitizensDev/Citizens2

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
            // ChatColor is a non-recommended method of coloring text, instead use the MiniMessage format that Paper now has
//     1.       player.sendMessage(Component.text("You don't have enough money to purchase the hoe.").color(TextColor.color(255, 0, 0)));
//     2.       player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You don't have enough money to purchase the hoe."));
//            I prefer example number 2, it is called the MiniMessage format, you can read more on it here
// https://docs.advntr.dev/minimessage/format.html
        }
    }
}
