package dev.pota.numismaticcompat;

import com.glisco.numismaticoverhaul.ModComponents;
import com.glisco.numismaticoverhaul.NumismaticOverhaul;
import com.glisco.numismaticoverhaul.currency.CurrencyComponent;
import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PotaNumismaticKeepInventoryCompat implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("PotA Numismatic KeepInventory Compat");

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayerEntity player) {
                handleTrueDeath(player);
            }
        });

        LOGGER.info("Initialized true-death currency drop compatibility.");
    }

    private static void handleTrueDeath(ServerPlayerEntity player) {
        List<ItemEntity> tentativeDrops = new ArrayList<>();
        boolean currencyCommitted = false;

        try {
            GameRules gameRules = player.getWorld().getGameRules();
            if (!gameRules.getBoolean(GameRules.KEEP_INVENTORY)) return;

            int percentage = Math.max(0, Math.min(100,
                    gameRules.getInt(NumismaticOverhaul.MONEY_DROP_PERCENTAGE)));

            if (percentage <= 0) {
                return;
            }

            CurrencyComponent currency = ModComponents.CURRENCY.get(player);
            long purseBefore = currency.getValue();

            if (purseBefore <= 0L) {
                return;
            }

            long amountToDrop = BigInteger.valueOf(purseBefore)
                    .multiply(BigInteger.valueOf(percentage))
                    .divide(BigInteger.valueOf(100L))
                    .longValueExact();

            if (amountToDrop <= 0L) {
                return;
            }

            List<ItemStack> stacks = CurrencyConverter.getAsValidStacks(amountToDrop);
            int expectedEntities = 0;

            // Match Numismatic's own native death-drop behavior: each physical coin is
            // dropped through PlayerEntity#dropItem rather than constructing ItemEntity
            // objects manually. This preserves vanilla/modded drop hooks, velocity,
            // pickup behavior and ItemEntity initialization at the actual death position.
            for (ItemStack stack : stacks) {
                if (stack == null || stack.isEmpty()) continue;

                int count = stack.getCount();
                for (int i = 0; i < count; i++) {
                    ItemStack single = stack.copy();
                    single.setCount(1);
                    expectedEntities++;

                    ItemEntity dropped = player.dropItem(single, true, false);
                    if (dropped == null) {
                        rollbackDrops(tentativeDrops);
                        error("Native player.dropItem failed for coin entity " + expectedEntities
                                + "; purse left untouched.", null);
                        return;
                    }

                    tentativeDrops.add(dropped);
                }
            }

            if (expectedEntities <= 0 || tentativeDrops.size() != expectedEntities) {
                rollbackDrops(tentativeDrops);
                error("Numismatic produced no complete native physical drop; purse left untouched.", null);
                return;
            }

            try {
                currency.modify(-amountToDrop);
                currencyCommitted = true;
            } catch (RuntimeException commitFailure) {
                restorePurse(currency, purseBefore);
                rollbackDrops(tentativeDrops);
                error("Failed to update Numismatic purse; purse restored and native drops removed.",
                        commitFailure);
                return;
            }

            LOGGER.debug("True-death currency drop in {} at ({}, {}, {}): purse={}, gamerule={}%,"
                    + " dropped={}, remaining={}, itemEntities={}",
                    player.getWorld().getRegistryKey().getValue(),
                    player.getX(), player.getY(), player.getZ(),
                    purseBefore, percentage, amountToDrop, currency.getValue(), tentativeDrops.size());
        } catch (RuntimeException failure) {
            if (!currencyCommitted) {
                rollbackDrops(tentativeDrops);
                error("Compatibility transaction failed before purse commit; native drops removed.", failure);
            } else {
                error("Compatibility transaction failed after purse commit; physical drops were retained "
                        + "to match the reduced purse.", failure);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static void restorePurse(CurrencyComponent currency, long purseBefore) {
        try {
            currency.setValue(purseBefore);
        } catch (RuntimeException restoreFailure) {
            boolean restored = currency.getValue() == purseBefore;
            error(restored
                    ? "Purse value restored server-side, but Numismatic failed while syncing rollback."
                    : "Purse rollback failed before the original value could be restored.",
                    restoreFailure);
        }
    }

    private static void rollbackDrops(List<ItemEntity> entities) {
        for (ItemEntity entity : entities) {
            if (entity == null || entity.isRemoved()) continue;
            try {
                entity.discard();
            } catch (RuntimeException ignored) {
                // Continue removing the remainder of the tentative transaction.
            }
        }
        entities.clear();
    }

    private static void error(String message, Throwable throwable) {
        if (throwable == null) {
            LOGGER.error(message);
        } else {
            LOGGER.error(message, throwable);
        }
    }
}
