package com.dominik.modid.client;

import com.dominik.modid.RadarData;
import com.dominik.modid.RadarFilter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.Map;

public class RadarRenderer {

    private static void addBoxLine(BufferBuilder b, Matrix4f m, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float bl, float a) {
        b.addVertex(m, (float)x1, (float)y1, (float)z1).setColor(r, g, bl, a);
        b.addVertex(m, (float)x2, (float)y2, (float)z2).setColor(r, g, bl, a);
    }

    private static void drawThickLine(BufferBuilder buffer, Matrix4f matrix, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
        double offset = 0.005; // Što je ovaj broj veći, to je linija deblja (ali i mutnija)

        // Originalna linija
        addBoxLine(buffer, matrix, x1, y1, z1, x2, y2, z2, r, g, b, a);

        // Dodatne linije s malim pomacima (offsetima)
        addBoxLine(buffer, matrix, x1 + offset, y1, z1, x2 + offset, y2, z2, r, g, b, a);
        addBoxLine(buffer, matrix, x1 - offset, y1, z1, x2 - offset, y2, z2, r, g, b, a);
        addBoxLine(buffer, matrix, x1, y1 + offset, z1, x2, y2 + offset, z2, r, g, b, a);
        addBoxLine(buffer, matrix, x1, y1 - offset, z1, x2, y2 - offset, z2, r, g, b, a);
    }

    public static void init() {

        WorldRenderEvents.LAST.register(context -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;

            Camera camera = context.camera();
            Vec3 camPos = camera.getPosition();
            float tickDelta = (float) context.tickCounter().getGameTimeDeltaPartialTick(false);

            Vec3 camDir = new Vec3(camera.getLookVector().x, camera.getLookVector().y, camera.getLookVector().z);
            Vec3 from = camPos.add(camDir.scale(15));

            PoseStack matrices = context.matrixStack();
            matrices.pushPose();
            matrices.translate(-camPos.x, -camPos.y, -camPos.z);
            Matrix4f matrix = matrices.last().pose();

            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            Tesselator tesselator = Tesselator.getInstance();

            // --- 1. CRTANJE LINIJA (Cijevi) ---
            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            boolean hasVertices = false;

            int segments = 8;
            float radius = 0.04f;

            for (Map.Entry<Entity, int[]> entry : RadarData.TARGETS.entrySet()) {
                Entity target = entry.getKey();
                int[] color = entry.getValue();
                if (!target.isAlive()) continue;

                Vec3 toPos = target.getPosition(tickDelta).add(0, target.getBbHeight() / 2.0, 0);
                Vec3 lineDir = toPos.subtract(from).normalize();
                Vec3 arbitrary = Math.abs(lineDir.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
                Vec3 perp1 = lineDir.cross(arbitrary).normalize();
                Vec3 perp2 = lineDir.cross(perp1).normalize();

                for (int i = 0; i < segments; i++) {
                    double angle1 = (2 * Math.PI * i) / segments;
                    double angle2 = (2 * Math.PI * (i + 1)) / segments;
                    Vec3 off1 = perp1.scale(Math.cos(angle1) * radius).add(perp2.scale(Math.sin(angle1) * radius));
                    Vec3 off2 = perp1.scale(Math.cos(angle2) * radius).add(perp2.scale(Math.sin(angle2) * radius));

                    buffer.addVertex(matrix, (float) (from.x + off1.x), (float) (from.y + off1.y), (float) (from.z + off1.z)).setColor(color[0], color[1], color[2], color[3]);
                    buffer.addVertex(matrix, (float) (from.x + off2.x), (float) (from.y + off2.y), (float) (from.z + off2.z)).setColor(color[0], color[1], color[2], color[3]);
                    buffer.addVertex(matrix, (float) (toPos.x + off2.x), (float) (toPos.y + off2.y), (float) (toPos.z + off2.z)).setColor(color[0], color[1], color[2], color[3]);
                    buffer.addVertex(matrix, (float) (toPos.x + off1.x), (float) (toPos.y + off1.y), (float) (toPos.z + off1.z)).setColor(color[0], color[1], color[2], color[3]);
                }
                hasVertices = true;
            }

            if (hasVertices) {
                BufferUploader.drawWithShader(buffer.buildOrThrow());
            }

            // --- 2. CRTANJE HITBOX KVADRATA (RUČNO) ---
            if (RadarFilter.SHOW_HITBOX) {
                BufferBuilder boxBuffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                boolean hasBoxes = false;

                for (Map.Entry<Entity, int[]> entry : RadarData.TARGETS.entrySet()) {
                    Entity target = entry.getKey();
                    int[] c = entry.getValue();
                    if (!target.isAlive()) continue;

                    float tickDelta1 = net.minecraft.client.Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);

// Izračunavamo koliko se entitet pomaknuo od zadnjeg ticka
                    double x = net.minecraft.util.Mth.lerp(tickDelta1, target.xo, target.getX());
                    double y = net.minecraft.util.Mth.lerp(tickDelta1, target.yo, target.getY());
                    double z = net.minecraft.util.Mth.lerp(tickDelta1, target.zo, target.getZ());

// Kreiramo novu kutiju koja je centrirana na interpoliranoj poziciji
                    float h = target.getBbHeight();
                    float w = target.getBbWidth() / 2.0f;
                    AABB b = new AABB(x - w, y, z - w, x + w, y + h, z + w);
                    float r = c[0]/255f, g = c[1]/255f, bl = c[2]/255f, a = c[3]/255f;

                    // Donji kvadrat
                    drawThickLine(boxBuffer, matrix, b.minX, b.minY, b.minZ, b.maxX, b.minY, b.minZ, r, g, bl, a);
                    drawThickLine(boxBuffer, matrix, b.maxX, b.minY, b.minZ, b.maxX, b.minY, b.maxZ, r, g, bl, a);
                    drawThickLine(boxBuffer, matrix, b.maxX, b.minY, b.maxZ, b.minX, b.minY, b.maxZ, r, g, bl, a);
                    drawThickLine(boxBuffer, matrix, b.minX, b.minY, b.maxZ, b.minX, b.minY, b.minZ, r, g, bl, a);

                    // Gornji kvadrat
                    drawThickLine(boxBuffer, matrix, b.minX, b.maxY, b.minZ, b.maxX, b.maxY, b.minZ, r, g, bl, a);
                    drawThickLine(boxBuffer, matrix, b.maxX, b.maxY, b.minZ, b.maxX, b.maxY, b.maxZ, r, g, bl, a);
                    drawThickLine(boxBuffer, matrix, b.maxX, b.maxY, b.maxZ, b.minX, b.maxY, b.maxZ, r, g, bl, a);
                    drawThickLine(boxBuffer, matrix, b.minX, b.maxY, b.maxZ, b.minX, b.maxY, b.minZ, r, g, bl, a);

                    // Vertikale
                    drawThickLine(boxBuffer, matrix, b.minX, b.minY, b.minZ, b.minX, b.maxY, b.minZ, r, g, bl, a);
                    drawThickLine(boxBuffer, matrix, b.maxX, b.minY, b.minZ, b.maxX, b.maxY, b.minZ, r, g, bl, a);
                    drawThickLine(boxBuffer, matrix, b.maxX, b.minY, b.maxZ, b.maxX, b.maxY, b.maxZ, r, g, bl, a);
                    drawThickLine(boxBuffer, matrix, b.minX, b.minY, b.maxZ, b.minX, b.maxY, b.maxZ, r, g, bl, a);

                    hasBoxes = true;
                }

                if (hasBoxes) {
                    BufferUploader.drawWithShader(boxBuffer.buildOrThrow());
                }
            }

            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            matrices.popPose();
        });

        // --- DRUGI DIO: TVOJE ORIGINALNE LABELE (Netaknuto) ---
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.level == null) return;

            PoseStack poseStack = context.matrixStack();
            Vec3 cam = context.camera().getPosition();
            var font = client.font;
            float pt = (float) context.tickCounter().getGameTimeDeltaPartialTick(false);

            for (Map.Entry<Entity, int[]> entry : RadarData.TARGETS.entrySet()) {
                Entity entity = entry.getKey();

                // --- DOHVAĆANJE PODATAKA O POKEMONU ---
                String text = ""; // Krenemo od praznog Stringa

                if (entity instanceof com.cobblemon.mod.common.entity.pokemon.PokemonEntity pEnt) {
                    var pokemon = pEnt.getPokemon();
                    int level = pokemon.getLevel();

                    // 1. Uzmi ČISTO ime vrste (npr. "Pikachu") umjesto entity.getName()
                    text = pokemon.getSpecies().getName();

                    // 2. Formatiranje spola (tvoj postojeći kod)
                    var gender = pokemon.getGender().toString().toLowerCase();
                    String genderSymbol = gender.equals("male") ? " §b♂" : (gender.equals("female") ? " §d♀" : "");

                    // 3. Dodaj Shiny ikonu na početak ako želiš (opcionalno)
                    if (pokemon.getShiny()) {
                        text = "✨ " + text;
                    }

                    // Finalni tekst: Pikachu §e[Lvl 50] §b♂
                    text = text + " §e[Lvl " + level + "]" + genderSymbol;
                } else {
                    // Ako entitet nije pokemon, koristi obično ime
                    text = entity.getName().getString();
                }

                double ex = entity.xOld + (entity.getX() - entity.xOld) * pt;
                double ey = entity.yOld + (entity.getY() - entity.yOld) * pt + entity.getBbHeight() + 0.5;
                double ez = entity.zOld + (entity.getZ() - entity.zOld) * pt;

                poseStack.pushPose();
                poseStack.translate(ex - cam.x, ey - cam.y, ez - cam.z);
                poseStack.mulPose(context.camera().rotation());

                float scale = 0.03f;
                poseStack.scale(scale, -scale, scale);

                int textWidth = font.width(text);
                float padding = 2f;
                int bgColor = 0x80000000;

                Matrix4f matrix = poseStack.last().pose();
                var buffer = client.renderBuffers().bufferSource().getBuffer(net.minecraft.client.renderer.RenderType.gui());

                // Pozadina (crni pravokutnik) prilagođena novoj širini teksta
                float x1 = -textWidth / 2f - padding;
                float y1 = -padding;
                float x2 = textWidth / 2f + padding;
                float y2 = font.lineHeight + padding;

                buffer.addVertex(matrix, x1, y2, 0).setColor(bgColor);
                buffer.addVertex(matrix, x2, y2, 0).setColor(bgColor);
                buffer.addVertex(matrix, x2, y1, 0).setColor(bgColor);
                buffer.addVertex(matrix, x1, y1, 0).setColor(bgColor);

                font.drawInBatch(
                        text,
                        -textWidth / 2f,
                        0f,
                        0xFFFFFF,
                        false,
                        poseStack.last().pose(),
                        client.renderBuffers().bufferSource(),
                        net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH,
                        0,
                        15728880
                );

                client.renderBuffers().bufferSource().endBatch();

                poseStack.popPose();
            }
        });
    }
}