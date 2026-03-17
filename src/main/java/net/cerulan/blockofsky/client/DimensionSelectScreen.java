package net.cerulan.blockofsky.client;

import net.cerulan.blockofsky.ModNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class DimensionSelectScreen extends Screen {
    private final BlockPos targetPos;
    private DimensionList dimensionList;
    private Button selectButton;

    public DimensionSelectScreen(BlockPos targetPos) {
        super(Component.translatable("gui.blockofsky.select_dimension"));
        this.targetPos = targetPos;
    }

    @Override
    protected void init() {
        this.dimensionList = new DimensionList(this.minecraft);
        this.addWidget(this.dimensionList);

        List<ResourceLocation> dims = new ArrayList<>();

        dims.add(new ResourceLocation("minecraft", "overworld"));
        dims.add(new ResourceLocation("minecraft", "the_nether"));
        dims.add(new ResourceLocation("minecraft", "the_end"));

        var connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            Set<ResourceKey<Level>> levels = connection.levels();
            List<ResourceLocation> modded = new ArrayList<>();
            for (ResourceKey<Level> key : levels) {
                ResourceLocation id = key.location();
                if (!dims.contains(id)) {
                    modded.add(id);
                }
            }
            modded.sort(Comparator.comparing(ResourceLocation::toString));
            dims.addAll(modded);
        }

        for (ResourceLocation dim : dims) {
            dimensionList.addDimensionEntry(new DimensionEntry(dim));
        }

        selectButton = Button.builder(Component.translatable("gui.blockofsky.select"),
                        button -> {
                            DimensionEntry selected = dimensionList.getSelected();
                            if (selected != null) {
                                sendDimensionSelection(selected.dimensionId);
                                onClose();
                            }
                        })
                .bounds(width / 2 - 75, height - 28, 150, 20)
                .build();
        this.addRenderableWidget(selectButton);
    }

    private void sendDimensionSelection(ResourceLocation dimensionId) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(targetPos);
        buf.writeResourceLocation(dimensionId);
        ClientPlayNetworking.send(ModNetworking.SET_DIMENSION_PACKET, buf);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        this.dimensionList.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    class DimensionList extends ObjectSelectionList<DimensionEntry> {
        public DimensionList(Minecraft mc) {
            super(mc, DimensionSelectScreen.this.width,
                    DimensionSelectScreen.this.height, 32,
                    DimensionSelectScreen.this.height - 36, 20);
        }

        public int addDimensionEntry(DimensionEntry entry) {
            return super.addEntry(entry);
        }
    }

    class DimensionEntry extends ObjectSelectionList.Entry<DimensionEntry> {
        final ResourceLocation dimensionId;
        private final String displayName;

        DimensionEntry(ResourceLocation dimensionId) {
            this.dimensionId = dimensionId;
            this.displayName = formatDimensionName(dimensionId);
        }

        @Override
        public Component getNarration() {
            return Component.literal(displayName);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left,
                           int width, int height, int mouseX, int mouseY,
                           boolean hovering, float partialTick) {
            guiGraphics.drawString(DimensionSelectScreen.this.font,
                    displayName, left + 5, top + 4, 0xFFFFFF);

            if (!"minecraft".equals(dimensionId.getNamespace())) {
                guiGraphics.drawString(DimensionSelectScreen.this.font,
                        dimensionId.toString(), left + 5, top + 14, 0x808080);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            DimensionSelectScreen.this.dimensionList.setSelected(this);
            return true;
        }

        private String formatDimensionName(ResourceLocation id) {
            String path = id.getPath();
            StringBuilder sb = new StringBuilder();
            for (String word : path.replace('_', ' ').split(" ")) {
                if (!word.isEmpty()) {
                    if (!sb.isEmpty()) sb.append(' ');
                    sb.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) sb.append(word.substring(1));
                }
            }
            if (!"minecraft".equals(id.getNamespace())) {
                return id.getNamespace() + ": " + sb;
            }
            return sb.toString();
        }
    }
}
