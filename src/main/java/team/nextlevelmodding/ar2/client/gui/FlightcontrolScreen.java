package team.nextlevelmodding.ar2.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import team.nextlevelmodding.ar2.gui.FlightcontrolMenu;
import team.nextlevelmodding.ar2.network.AR2Packets;
import team.nextlevelmodding.ar2.network.ServerboundRunProgramPacket;

import java.util.ArrayList;
import java.util.List;

public class FlightcontrolScreen extends AbstractContainerScreen<FlightcontrolMenu> {
    private static final int BUTTON_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_MARGIN = 4;
    private static final int START_Y = 30;
    private static final int BOX_PADDING = 10;

    private final List<Button> programButtons = new ArrayList<>();

    public FlightcontrolScreen(FlightcontrolMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 220;
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        programButtons.clear();
        
        // Add program buttons dynamically
        List<String> programs = menu.getPrograms();
        for (int i = 0; i < programs.size(); i++) {
            final String programName = programs.get(i);
            int yPos = this.topPos + START_Y + BOX_PADDING + (i * (BUTTON_HEIGHT + BUTTON_MARGIN));

            Button button = new Button.Builder(
                    Component.literal(programName),
                    btn -> runProgram(programName))
                .bounds(
                    this.leftPos + (this.imageWidth - BUTTON_WIDTH) / 2,
                    yPos,
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT)
                .build();
            
            programButtons.add(button);
            addRenderableWidget(button);
        }
    }

    private void runProgram(String programName) {
        if (menu.getBlockEntity() != null) {
            AR2Packets.sendToServer(new ServerboundRunProgramPacket(menu.getPos(), programName));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        renderBackground(guiGraphics);

        // Draw default background (gray panel)
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFFC6C6C6);

        // Draw black box around buttons area
        List<String> programs = menu.getPrograms();
        if (!programs.isEmpty()) {
            int boxHeight = programs.size() * (BUTTON_HEIGHT + BUTTON_MARGIN) + (BOX_PADDING * 2) - BUTTON_MARGIN;
            int boxX = this.leftPos + (this.imageWidth - BUTTON_WIDTH) / 2 - BOX_PADDING;
            int boxY = this.topPos + START_Y;
            int boxWidth = BUTTON_WIDTH + (BOX_PADDING * 2);

            // Draw black background box
            guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF000000);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, (this.imageWidth - this.font.width(this.title)) / 2, 10, 0x404040, false);
    }
}
