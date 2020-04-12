package com.coolcrown.UHC;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.server.v1_15_R1.Blocks;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;


public class Death implements Listener {

    public ArrayList<String> remove = new ArrayList<String>();
    HashMap<String, Float> saturation = new HashMap<String, Float>();
    HashMap<String, Integer> foodlevel = new HashMap<String, Integer>();
    HashMap<String, String> killers= new HashMap<String, String>();

    @EventHandler
    public void onEntityDamage(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        if (CoolCrownUHC.injured.contains(player.getUniqueId())){
            event.setDeathMessage(killers.get(player.getDisplayName()));
            player.setSneaking(false);
            player.setGlowing(false);
            CoolCrownUHC.injured.remove(player.getUniqueId());
            player.sendTitle(ChatColor.DARK_RED+"" +ChatColor.BOLD+ "You died!" , ChatColor.RED + event.getDeathMessage() ,0,50,20);
            killers.remove(player.getDisplayName());
            return;
        }
        killers.put(player.getDisplayName(), event.getDeathMessage());
        if (CoolCrownUHC.injured.contains(player.getUniqueId())) {
            player.setGlowing(false);
            CoolCrownUHC.injured.remove(player.getUniqueId());
            if (!(player.getKiller() instanceof Player)) {
                return;
            }
            return;
        }
        if (player.getKiller() != null) {
            player.sendTitle(ChatColor.DARK_RED+"" +ChatColor.BOLD+ "You're injured!" , ChatColor.GOLD + "A team can revive you with 7 hearts and a golden block!" ,0,100,20);
        }
        event.setDeathMessage(null);
        player.setHealth(20);
        player.sendMessage(ChatColor.RED + "You have been injured!");
        CoolCrownUHC.injured.add(player.getUniqueId());
        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 10000000, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10000000, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10000000, 128));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10000000, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 10000000, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 10));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 10000000, 255));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 10000000, 255));


        saturation.put(player.getUniqueId().toString(), player.getSaturation());
        foodlevel.put(player.getUniqueId().toString(), player.getFoodLevel());
        player.setFoodLevel(1);
        player.setGlowing(true);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (entity instanceof Player) {
            if (CoolCrownUHC.injured.contains(entity.getUniqueId())) {
                Player injure = (Player) event.getRightClicked();
                if (CoolCrownUHC.injured.contains(player.getUniqueId())) return;
                Team playerTeam = null;
                for (Team t: player.getScoreboard().getTeams()){
                    if (t.hasEntry(player.getDisplayName())){
                        playerTeam = t;
                    }
                }
                if (!playerTeam.hasEntry(injure.getDisplayName())) return;
                if (player.getHealth() < 13.70){
                    player.sendTitle(ChatColor.GREEN+"" +ChatColor.BOLD+ "" , ChatColor.GOLD + "You need to have at least 7 hearts to revive a player!",0,50,20);
                    return;
                }
                if(player.getInventory().getItemInMainHand().getType() != Material.GOLD_BLOCK && player.getInventory().getItemInOffHand().getType() != Material.GOLD_BLOCK)
                {
                    player.sendTitle(ChatColor.GREEN+"" +ChatColor.BOLD+ "" , ChatColor.GOLD + "You need to hold a gold block to revive a player!",0,50,20);
                    return;
                }

                injure.setFoodLevel(injure.getFoodLevel() + 1);
                if (injure.getFoodLevel() == 20) {
                    for (PotionEffect pe : injure.getActivePotionEffects()) {
                        injure.removePotionEffect(pe.getType());
                    }
                    player.setHealth(player.getHealth()-10);
                    injure.setHealth(player.getHealth());
                    injure.setSneaking(false);
                    injure.setGlowing(false);
                    injure.sendTitle(ChatColor.GREEN+"" +ChatColor.BOLD+ "" , ChatColor.GREEN + "You have been revived by " + player.getDisplayName(),0,50,20);
                    player.sendTitle(ChatColor.GREEN+"" +ChatColor.BOLD+ "" , ChatColor.GREEN + "You have revived " + injure.getDisplayName(),0,50,20);
                    player.getInventory().removeItem(new ItemStack(Material.GOLD_BLOCK,1));
                    //ParticleEffect.VILLAGER_HAPPY.display(1, 1, 1, 1, 100, injure.getLocation(), 100);
                    CoolCrownUHC.injured.remove(injure.getUniqueId());
                    injure.setFoodLevel(foodlevel.get(injure.getUniqueId().toString()));
                    injure.setSaturation(saturation.get(injure.getUniqueId().toString()));
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        if (CoolCrownUHC.injured.contains(event.getPlayer().getUniqueId())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInjuredJump(PlayerMoveEvent event){
        if (CoolCrownUHC.injured.contains(event.getPlayer().getUniqueId())){
            if (event.getPlayer().getVelocity().getY() > 1){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (CoolCrownUHC.injured.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (CoolCrownUHC.injured.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerItemPickUp(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (CoolCrownUHC.injured.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (CoolCrownUHC.injured.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can't use commands while injured!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (CoolCrownUHC.injured.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can't teleport while injured!");
            event.setCancelled(true);
        }
    }

}