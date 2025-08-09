package hatim.plugings.veinMiner_Pro;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class VeinMiner_Pro extends JavaPlugin implements Listener {

    private final Set<UUID> oreEnabled = new HashSet<>();
    private final Set<UUID> treeEnabled = new HashSet<>();

    private final Set<Material> targetOres = EnumSet.of(
            Material.COAL_ORE, Material.IRON_ORE, Material.COPPER_ORE, Material.GOLD_ORE,
            Material.LAPIS_ORE, Material.REDSTONE_ORE, Material.EMERALD_ORE, Material.DIAMOND_ORE,
            Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.DEEPSLATE_LAPIS_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.DEEPSLATE_EMERALD_ORE, Material.DEEPSLATE_DIAMOND_ORE
    );
    private final Set<Material> targetLogs = EnumSet.of(
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG,
            Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.MANGROVE_LOG, Material.CHERRY_LOG
    );

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("VeinMinerPro has been enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is for players only.");
            return true;
        }
        Player player = (Player) sender;
        if (args.length != 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /vm <ores|trees> <on|off>");
            return true;
        }

        Set<UUID> targetSet = args[0].equalsIgnoreCase("ores") ? oreEnabled : (args[0].equalsIgnoreCase("trees") ? treeEnabled : null);
        if (targetSet == null) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /vm <ores|trees> <on|off>");
            return true;
        }

        if (args[1].equalsIgnoreCase("on")) {
            targetSet.add(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "VeinMinerPro for " + args[0] + " enabled!");
        } else if (args[1].equalsIgnoreCase("off")) {
            targetSet.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "VeinMinerPro for " + args[0] + " disabled!");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Usage: /vm <ores|trees> <on|off>");
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL || player.isSneaking()) {
            return;
        }

        Block block = event.getBlock();
        Material type = block.getType();

        Set<Material> blockSet = null;
        if (targetOres.contains(type) && oreEnabled.contains(player.getUniqueId())) {
            blockSet = targetOres;
        } else if (targetLogs.contains(type) && treeEnabled.contains(player.getUniqueId())) {
            blockSet = targetLogs;
        }

        if (blockSet != null) {
            // Cancel the original event to take full control.
            event.setCancelled(true);
            breakVein(player, block, blockSet);
        }
    }

    private void breakVein(Player player, Block startBlock, Set<Material> acceptedTypes) {
        Queue<Block> blocksToCheck = new LinkedList<>();
        Set<Block> vein = new HashSet<>();
        blocksToCheck.add(startBlock);
        vein.add(startBlock);

        Material materialType = startBlock.getType();
        int maxBlocks = 128; // Increased limit slightly

        while (!blocksToCheck.isEmpty() && vein.size() < maxBlocks) {
            Block current = blocksToCheck.poll();
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        Block neighbor = current.getRelative(x, y, z);
                        if (acceptedTypes.contains(neighbor.getType()) && neighbor.getType() == materialType && vein.add(neighbor)) {
                            blocksToCheck.add(neighbor);
                        }
                    }
                }
            }
        }

        ItemStack tool = player.getInventory().getItemInMainHand();
        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable) || tool.getType() == Material.AIR) {
            // If not holding a tool, just break the one block.
            startBlock.breakNaturally();
            return;
        }

        Damageable damageable = (Damageable) meta;
        int durabilityLost = 0;
        boolean hasSilkTouch = tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0;

        for (Block blockInVein : vein) {
            if (damageable.getDamage() + durabilityLost >= tool.getType().getMaxDurability()) {
                break; // Stop if the tool is about to break.
            }

            if (hasSilkTouch) {
                blockInVein.getWorld().dropItemNaturally(blockInVein.getLocation(), new ItemStack(blockInVein.getType()));
            } else {
                // This respects the Fortune enchantment on the tool!
                Collection<ItemStack> drops = blockInVein.getDrops(tool);
                for (ItemStack drop : drops) {
                    blockInVein.getWorld().dropItemNaturally(blockInVein.getLocation(), drop);
                }
            }

            blockInVein.setType(Material.AIR);
            durabilityLost++;
        }

        // Apply durability damage
        damageable.setDamage(damageable.getDamage() + durabilityLost);
        tool.setItemMeta(meta);

        // Apply hunger cost (1 hunger point per 10 blocks)
        player.setExhaustion(player.getExhaustion() + (0.1f * durabilityLost));

        // Play feedback effects
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
        player.getWorld().spawnParticle(Particle.CRIT, startBlock.getLocation().add(0.5, 0.5, 0.5), 30, 0.5, 0.5, 0.5);
    }
}