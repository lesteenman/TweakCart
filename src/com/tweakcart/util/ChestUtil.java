package com.tweakcart.util;

import com.tweakcart.model.IntMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ContainerBlock;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Edoxile
 */
public class ChestUtil {
    public static final ItemStack[] putItems(ItemStack[] from, ContainerBlock containerBlock) {
        ItemStack[] to = containerBlock.getInventory().getContents();
        for (int i1 = 0; i1 < from.length; i1++) {
            if (from[i1] == null)
                continue;
            for (int i2 = 0; i2 < to.length; i2++) {
                if (to[i2] == null) {
                    to[i2] = from[i1];
                    from[i1] = null;
                    break;
                } else if (to[i2].getTypeId() == from[i1].getTypeId() && to[i2].getDurability() == from[i1].getDurability() && to[i2].getAmount() < 64) {
                    if ((from[i1].getAmount() + to[i2].getAmount()) > 64) {
                        from[i1].setAmount((to[i2].getAmount() + from[i1].getAmount()) - 64);
                        to[i2].setAmount(64);
                        i1--;

                    } else {
                        to[i2].setAmount(to[i2].getAmount() + from[i1].getAmount());
                        from[i1] = null;

                    }
                    break;
                }
            }
        }
        containerBlock.getInventory().setContents(to);
        return from;
    }

    public static ItemStack[] putItems(ItemStack from, ContainerBlock containerBlock) {
        ItemStack[] stacks = {from};
        return putItems(stacks, containerBlock);
    }
    
    public static IntMap moveItems(Inventory iFrom, Inventory iTo, IntMap settings) {
        ItemStack[] from = iFrom.getContents();
        ItemStack[] to = iTo.getContents();
        main:for(int index = 0; index < from.length; index++ ) { 
            if(from[index] == null) continue;
            byte data = from[index].getDurability() > Byte.MAX_VALUE ? 0 : IntMap.isAllowedMaterial(from[index].getTypeId(), (byte) from[index].getDurability()) ? (byte) from[index].getDurability() : 0;

            ItemStack itemFrom = from[index];
            if(settings.getInt(itemFrom.getTypeId(), data) <= 0) continue;
            /*
             * First we try to append an existing stack.
             */
            for(int indexto = 0; indexto < to.length; indexto++ ) {
                if(itemFrom.getAmount() <= 0) break;
                ItemStack itemTo = to[indexto];                
                if(itemTo == null) continue;
                if(itemTo.getAmount() == 0) {
                	to[indexto] = null;
                	continue;
                }
                if(itemTo.getTypeId() != itemFrom.getTypeId() || itemTo.getDurability() != itemFrom.getDurability()) continue;
                if(itemTo.getAmount() >= 64) continue;

                int maxamount = settings.getInt(itemFrom.getTypeId(), data);
                if(maxamount <= 0) continue main;
                
                int stackspace = 64 - itemTo.getAmount();
                int moveamount = (itemFrom.getAmount() >= stackspace && maxamount >= stackspace ? stackspace :
                                    itemFrom.getAmount() < stackspace && maxamount >= stackspace ? itemFrom.getAmount() :
                                        maxamount < stackspace && itemFrom.getAmount() >= stackspace ? maxamount :
                                            maxamount > itemFrom.getAmount() ? itemFrom.getAmount() : maxamount);
                itemFrom.setAmount(itemFrom.getAmount() - moveamount);
                itemTo.setAmount(itemTo.getAmount() + moveamount);
                if(maxamount != Integer.MAX_VALUE) {
                    settings.setInt(itemFrom.getTypeId(), data, maxamount-moveamount);
                }
                
            }
            
            if(itemFrom.getAmount() <= 0) {
                from[index] = null;
                itemFrom.setAmount(0);
                continue;
            }
            /*
             * Put item in an empty slot
             */
            for(int indexto = 0; indexto < to.length; indexto++ ) {             
                if(to[indexto] != null) break;
                int maxamount = settings.getInt(itemFrom.getTypeId(), data);
                if(maxamount <= 0) break; //FIX, PROFIT
                if(itemFrom.getAmount() > maxamount) {
                    itemFrom.setAmount(itemFrom.getAmount() - maxamount);
                    to[indexto] = new ItemStack(itemFrom.getTypeId(), maxamount, data);
                    settings.setInt(itemFrom.getTypeId(), data, 0);
                    break; // We can't put more of this item type so we skip to the next item.
                } else {
                    to[indexto] = itemFrom;
                    from[index] = null;
                    if(maxamount != Integer.MAX_VALUE){
                        maxamount -= itemFrom.getAmount();
                        settings.setInt(itemFrom.getTypeId(), data, maxamount);
                    }
                    break;
                }
            }
        }
        iFrom.setContents(from);
        iTo.setContents(to);
        return settings;
    }
//    public static void moveItems(Inventory iFrom, Inventory iTo, IntMap through) {
//        ItemStack[] from = iFrom.getContents();
//        ItemStack[] to = iTo.getContents();
//        int i1, i2;
//        for (i1 = 0; i1 < from.length; i1++) {
//            if (from[i1] == null) {
//                //Dat betekent dus dat er geen item in dat slot zit :)
//                continue;
//            }
//            int mapAmount = through.getInt(from[i1].getType(), (byte) from[i1].getDurability());
//            mapAmount =  (mapAmount > 64 && mapAmount < Integer.MAX_VALUE)? 64 : mapAmount; //64 stacksizes :)
//            int startAmount = from[i1].getAmount(); //De hoeveelheid die in de cart of chest zit
//            if (mapAmount == 0 || mapAmount == Integer.MIN_VALUE) {
//                continue;
//            }
//
//            int amountToMove = (mapAmount == Integer.MAX_VALUE ? startAmount : mapAmount); //de hoeveelheid die te moven is
//            from[i1].setAmount(from[i1].getAmount() - amountToMove + 1);
//            boolean hasPutSomethingIn = true;
//            for (i2 = 0; i2 < to.length; i2++) {
//                if (to[i2] == null) {
//                    to[i2] = from[i1].clone();
//                    to[i2].setAmount(amountToMove);
//                    amountToMove = 0;
//                    break;
//                } else if (to[i2].getTypeId() == from[i1].getTypeId() && to[i2].getDurability() == from[i1].getDurability() && to[i2].getAmount() < 64) {
//                    if (amountToMove + to[i2].getAmount() > 64) {
//                        //hier gaat iets mis
//                        
//                        amountToMove += to[i2].getAmount() - 64;
//                        to[i2].setAmount(64);
//                        
//                    } else {
//                        to[i2].setAmount(amountToMove + to[i2].getAmount());
//                        amountToMove = 0;
//                        
//                    }
//                    break;
//                }
//                
//                if(i2 == to.length -1){
//                    //OEEH, we konden dus niets terug plaatsen
//                    hasPutSomethingIn = false;
//                    Bukkit.getServer().broadcastMessage("vol is vol");
//                }
//                
//                
//            }
//            int amountToPlaceBack = from[i1].getAmount() + amountToMove - 1;
//            Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "" + amountToPlaceBack);
//            from[i1].setAmount(amountToPlaceBack);
//            //through.setInt(from[i1].getType(), (byte) from[i1].getDurability(), amountToMove);
//            //de bovenstaande regel slaat werkelijk waar nergens over
//            
//            if((amountToPlaceBack) > 0 && hasPutSomethingIn){
//                i1--;
//            }
//        }
//        iTo.setContents(to);
//        iFrom.setContents(from);
//    }

    public static List<Chest> getChestsAroundBlock(Block block, int sw) {
        int nsw = -sw;
        List<Chest> chestList = new ArrayList<Chest>();
        for (int dx = nsw; dx <= sw; dx++) {
            for (int dy = nsw; dy <= sw; dy++) {
                for (int dz = nsw; dz <= sw; dz++) {
                    if (block.getRelative(dx, dy, dz).getTypeId() == Material.CHEST.getId()){
                        chestList.add((Chest) block.getRelative(dx, dy, dz).getState());
                        chestList = getChestsAdjacent(chestList, block, dx, dy, dz);
                    }
                }
            }
        }
        
        return chestList;
    }
    
    public static List<Chest> getChestsAdjacent(List<Chest> chestList, Block block, int x, int y, int z){

        if((x == 1 || x == -1) && (z == 1 || z == -1)){
            if(block.getRelative(x+x, y, z).getTypeId() == Material.CHEST.getId()){
                chestList.add((Chest) block.getRelative(x+x, y, z).getState());
                //Bukkit.getServer().broadcastMessage("ik heb een kist gevonden op X: " + x + " Y: " + y + " Z: " + z);
            }
            else if(block.getRelative(x, y, z+z).getTypeId() == Material.CHEST.getId()){
                chestList.add((Chest) block.getRelative(x, y, z+z).getState());
                //Bukkit.getServer().broadcastMessage("ik heb een kist gevonden op X: " + x + " Y: " + y + " Z: " + z);
            }
        }else if(x == 1 || x == -1){
            if(block.getRelative(x+x, y, z).getTypeId() == Material.CHEST.getId()){
                chestList.add((Chest) block.getRelative(x+x, y, z).getState());
                //Bukkit.getServer().broadcastMessage("ik heb een kist gevonden op X: " + x + " Y: " + y + " Z: " + z);
            }
        }else if(z == 1 || z == -1){
            if(block.getRelative(x, y, z+z).getTypeId() == Material.CHEST.getId()){
                chestList.add((Chest) block.getRelative(x, y, z+z).getState());
                //Bukkit.getServer().broadcastMessage("ik heb een kist gevonden op X: " + x + " Y: " + y + " Z: " + z);
            }
        }
        
        return chestList;
    }
}
