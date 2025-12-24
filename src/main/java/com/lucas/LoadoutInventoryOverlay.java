package com.lucas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class LoadoutInventoryOverlay extends Overlay
{
    private static final int INVENTORY_SIZE = 28;
    private static final Color CORRECT_FILL = new Color(0, 200, 0, 80);
    private static final Color CORRECT_BORDER = new Color(0, 200, 0, 160);
    private static final Color WRONG_FILL = new Color(200, 0, 0, 80);
    private static final Color WRONG_BORDER = new Color(200, 0, 0, 160);

    private final Client client;
    private final LucasLoadoutConfig config;
    private final LucasLoadoutWheelPlugin plugin;

    @Inject
    public LoadoutInventoryOverlay(Client client, LucasLoadoutConfig config, LucasLoadoutWheelPlugin plugin)
    {
        this.client = client;
        this.config = config;
        this.plugin = plugin;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.enableInventoryOverlay())
        {
            return null;
        }

        if (!plugin.isLoggedIn() || !plugin.isBankOpen())
        {
            return null;
        }

        Layout layout = plugin.getCurrentLayout();
        if (layout == null)
        {
            return null;
        }

        Widget inventoryWidget = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
        if (inventoryWidget == null || inventoryWidget.isHidden())
        {
            return null;
        }

        for (int i = 0; i < INVENTORY_SIZE; i++)
        {
            Widget slotWidget = inventoryWidget.getChild(i);
            if (slotWidget == null)
            {
                continue;
            }

            int expected = layout.getItemIdForSlot(i);
            int actual = slotWidget.getItemId();
            boolean correct = expected == actual;

            Rectangle bounds = slotWidget.getBounds();
            graphics.setColor(correct ? CORRECT_FILL : WRONG_FILL);
            graphics.fill(bounds);
            graphics.setColor(correct ? CORRECT_BORDER : WRONG_BORDER);
            graphics.draw(bounds);
        }

        return null;
    }
}
