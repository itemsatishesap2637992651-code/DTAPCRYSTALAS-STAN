
package com.dtap;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class DTapCrystalAssist implements ClientModInitializer {

    private static KeyMapping rightKey;
    private static KeyMapping leftKey;

    @Override
    public void onInitializeClient() {

        KeyMapping.Category category =
            KeyMapping.Category.register(
                net.minecraft.resources.Identifier
                    .fromNamespaceAndPath("dtapcrystal", "main")
            );

        rightKey = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                "key.dtapcrystal.right",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                category
            )
        );

        leftKey = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                "key.dtapcrystal.left",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                category
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null ||
                client.gameMode == null) return;

            if (rightKey.consumeClick()) place(client, true);
            if (leftKey.consumeClick()) place(client, false);
        });
    }

    private static void place(Minecraft client, boolean right) {

        if (!(client.crosshairPickEntity instanceof Player target)) {
            client.player.displayClientMessage(
                Component.literal("Hedef oyuncuya nişan al!"), true
            );
            return;
        }

        if (!client.player.getMainHandItem().is(Items.OBSIDIAN)) {
            client.player.displayClientMessage(
                Component.literal("Elinde obsidyen olmalı!"), true
            );
            return;
        }

        Vec3 look = target.position().subtract(
            client.player.position()
        );

        Vec3 side = new Vec3(-look.z, 0, look.x).normalize();

        if (!right) side = side.scale(-1);

        Direction direction = Direction.getNearest(
            side.x, 0, side.z
        );

        BlockPos targetPos = BlockPos.containing(
            target.getX(), target.getY(), target.getZ()
        );

        BlockPos placePos = targetPos.relative(direction);
        BlockPos supportPos = placePos.below();

        if (!client.level.getBlockState(placePos).canBeReplaced()
            || !client.level.getBlockState(supportPos).isFaceSturdy(
                client.level, supportPos, Direction.UP
            )) {
            client.player.displayClientMessage(
                Component.literal("Uygun D-Tap yeri bulunamadı!"), true
            );
            return;
        }

        BlockHitResult hit = new BlockHitResult(
            Vec3.atCenterOf(supportPos).add(0, 0.5, 0),
            Direction.UP,
            supportPos,
            false
        );

        client.gameMode.useItemOn(
            client.player, InteractionHand.MAIN_HAND, hit
        );
    }
}
