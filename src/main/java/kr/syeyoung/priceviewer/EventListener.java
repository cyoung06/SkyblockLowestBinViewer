package kr.syeyoung.priceviewer;


import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

public class EventListener {

    public static String apiKey = "";

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent e) {
        ItemStack hoveredItem = e.itemStack;

        Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();
        ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(Scoreboard.getObjectiveDisplaySlotNumber("sidebar"));
        if (scoreObjective != null && scoreObjective.getDisplayName().replaceAll("ยง.", "").contains("SKYBLOCK")) {
            NBTTagCompound compound = hoveredItem.getTagCompound();
            if (compound == null) return;
            if (!compound.hasKey("ExtraAttributes")) return;
            final String id = compound.getCompoundTag("ExtraAttributes").getString("id");

            if (id.equals("ENCHANTED_BOOK")) {
                final NBTTagCompound enchants = compound.getCompoundTag("ExtraAttributes").getCompoundTag("enchantments");
                Set<String> keys = enchants.getKeySet();
                Set<String> actualKeys = new TreeSet<String>(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        String id2 = id + "::" + o1 + "-" + enchants.getInteger(o1);
                        Prices.AuctionData auctionData = Prices.auctions.get(id2);
                        int price1 = auctionData == null ? 0: auctionData.prices.first();
                        String id3 = id + "::" + o2 + "-" + enchants.getInteger(o2);
                        Prices.AuctionData auctionData2 = Prices.auctions.get(id3);
                        int price2 = auctionData2 == null ? 0: auctionData2.prices.first();

                        return compare2(price1, price2) == 0 ? o1.compareTo(o2) : compare2(price1, price2);
                    }

                    public int compare2(int y, int x) {
                        return (x < y) ? -1 : ((x == y) ? 0 : 1);
                    }
                });
                actualKeys.addAll(keys);

                int totalLowestPrice = 0;
                int totalHighestPrice = 0;
                int iterations = 0;
                for (String key :actualKeys) {
                    iterations++;
                    String id2 = id + "::" + key + "-" + enchants.getInteger(key);
                    Prices.AuctionData auctionData = Prices.auctions.get(id2);
                    if (auctionData == null) {
                        if (iterations < 10)
                            e.toolTip.add("§f" + key + " " + enchants.getInteger(key) + "§7: §cn/a");
                    } else {
                        if (iterations < 10)
                            e.toolTip.add("§f"+key+" "+enchants.getInteger(key)+"§7: §e"+format(auctionData.prices.first())+" §7to §e"+format(auctionData.prices.last()));
                        totalLowestPrice += auctionData.prices.first();
                        totalHighestPrice += auctionData.prices.last();
                    }
                }
                if (iterations >= 10)
                    e.toolTip.add("§7"+(iterations - 10)+" more enchants... ");
                e.toolTip.add("§fTotal§7: §e"+format(totalLowestPrice)+" §7to §e"+format(totalHighestPrice));
            } else {
                Prices.AuctionData auctionData = Prices.auctions.get(id);
                e.toolTip.add("§f");
                if (auctionData == null) {
                    e.toolTip.add("§fLowest ah§7: §cn/a");
                    e.toolTip.add("§fHighest ah§7: §cn/a");
                    e.toolTip.add("§fBazaar sell price§7: §cn/a");
                    e.toolTip.add("§fBazaar buy price§7: §cn/a");
                } else {
                    e.toolTip.add("§fLowest ah§7: "+(auctionData.prices.size() != 0 ? "§e"+format(auctionData.prices.first()) : "§cn/a"));
                    e.toolTip.add("§fHighest ah§7: "+(auctionData.prices.size() != 0 ? "§e"+format(auctionData.prices.last()) : "§cn/a"));
                    e.toolTip.add("§fBazaar sell price§7: "+(auctionData.sellPrice == -1 ? "§cn/a" : "§e"+format(auctionData.sellPrice)));
                    e.toolTip.add("§fBazaar buy price§7: "+(auctionData.buyPrice == -1 ? "§cn/a" : "§e"+format(auctionData.buyPrice)));
                }
            }
        }
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<Long, String>();
    static {
        suffixes.put(1000L, "k");
        suffixes.put(1000000L, "m");
        suffixes.put(1000000000L, "b");
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
