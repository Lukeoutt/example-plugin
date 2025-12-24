package com.lucas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class LoadoutBankOverlay extends Overlay
{
    private static final int INVENTORY_COLS = 7;
    private static final int INVENTORY_ROWS = 4;
    private static final int SLOT_WIDTH = 36;
    private static final int SLOT_HEIGHT = 32;
    private static final int SLOT_PADDING = 4;
    private static final int GRID_PADDING = 12;

    private static final Color BANK_MATCH_BORDER = new Color(0, 160, 110, 200);
    private static final Color GRID_SLOT_BORDER = new Color(255, 255, 255, 80);
    private static final Color GRID_PRESENT_BORDER = new Color(0, 200, 0, 200);
    private static final Color GRID_MISSING_BORDER = new Color(200, 0, 0, 200);
    private static final Color GRID_MISSING_FILL = new Color(200, 0, 0, 60);

    private final Client client;
    private final ItemManager itemManager;
    private final LucasLoadoutConfig config;
    private final LucasLoadoutWheelPlugin plugin;

    @Inject
    public LoadoutBankOverlay(Client client, ItemManager itemManager, LucasLoadoutConfig config, LucasLoadoutWheelPlugin plugin)
    {
        this.client = client;
        this.itemManager = itemManager;
        this.config = config;
        this.plugin = plugin;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.enableBankProjection())
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

        Set<Integer> layoutItemIds = collectLayoutItemIds(layout.getItems());
        if (layoutItemIds.isEmpty())
        {
            return null;
        }

        highlightBankItems(graphics, layoutItemIds);
        renderPreviewGrid(graphics, layout, layoutItemIds);

        return null;
    }

    private void highlightBankItems(Graphics2D graphics, Set<Integer> layoutItemIds)
    {
        Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        if (bankItemContainer == null || bankItemContainer.isHidden())
        {
            return;
        }

        Widget[] children = bankItemContainer.getChildren();
        if (children == null)
        {
            return;
        }

        graphics.setColor(BANK_MATCH_BORDER);
        for (Widget child : children)
        {
            if (child == null)
            {
                continue;
            }

            int itemId = child.getItemId();
            if (itemId <= 0 || !layoutItemIds.contains(itemId))
            {
                continue;
            }

            Rectangle bounds = child.getBounds();
            graphics.draw(bounds);
        }
    }

    private void renderPreviewGrid(Graphics2D graphics, Layout layout, Set<Integer> layoutItemIds)
    {
        Widget bankContent = client.getWidget(WidgetInfo.BANK_CONTENT_CONTAINER);
        if (bankContent == null)
        {
            return;
        }

        Rectangle bankBounds = bankContent.getBounds();
        int gridWidth = INVENTORY_COLS * SLOT_WIDTH + (INVENTORY_COLS - 1) * SLOT_PADDING;
        int gridHeight = INVENTORY_ROWS * SLOT_HEIGHT + (INVENTORY_ROWS - 1) * SLOT_PADDING;

        int baseX = bankBounds.x + bankBounds.width + GRID_PADDING;
        int baseY = bankBounds.y + GRID_PADDING;

        int canvasWidth = client.getCanvasWidth();
        int canvasHeight = client.getCanvasHeight();

        if (baseX + gridWidth > canvasWidth)
        {
            baseX = bankBounds.x - gridWidth - GRID_PADDING;
        }
        if (baseY + gridHeight > canvasHeight)
        {
            baseY = Math.max(GRID_PADDING, canvasHeight - gridHeight - GRID_PADDING);
        }

        Set<Integer> bankItemIds = collectBankItemIds();
        List<LayoutItem> items = layout.getItems();

        for (int slot = 0; slot < INVENTORY_COLS * INVENTORY_ROWS; slot++)
        {
            int col = slot % INVENTORY_COLS;
            int row = slot / INVENTORY_COLS;
            int x = baseX + col * (SLOT_WIDTH + SLOT_PADDING);
            int y = baseY + row * (SLOT_HEIGHT + SLOT_PADDING);
            Rectangle slotBounds = new Rectangle(x, y, SLOT_WIDTH, SLOT_HEIGHT);

            graphics.setColor(GRID_SLOT_BORDER);
            graphics.draw(slotBounds);

            int itemId = getItemIdForSlot(items, slot);
            if (itemId <= 0)
            {
                continue;
            }

            boolean presentInBank = bankItemIds.contains(itemId);
            drawItemIcon(graphics, itemId, slotBounds, presentInBank);
        }
    }

    private void drawItemIcon(Graphics2D graphics, int itemId, Rectangle slotBounds, boolean presentInBank)
    {
        BufferedImage itemImage = itemManager.getImage(itemId);
        if (itemImage == null)
        {
            return;
        }

        int x = slotBounds.x + (slotBounds.width - itemImage.getWidth()) / 2;
        int y = slotBounds.y + (slotBounds.height - itemImage.getHeight()) / 2;

        if (!presentInBank)
        {
            graphics.setColor(GRID_MISSING_FILL);
            graphics.fill(slotBounds);
        }

        graphics.drawImage(itemImage, x, y, null);
        graphics.setColor(presentInBank ? GRID_PRESENT_BORDER : GRID_MISSING_BORDER);
        graphics.draw(slotBounds);
    }

    private Set<Integer> collectLayoutItemIds(List<LayoutItem> items)
    {
        Set<Integer> ids = new HashSet<>();
        if (items == null)
        {
            return ids;
        }

        for (LayoutItem item : items)
        {
            if (item != null && item.getItemId() > 0)
            {
                ids.add(item.getItemId());
            }
        }

        return ids;
    }

    private Set<Integer> collectBankItemIds()
    {
        Set<Integer> ids = new HashSet<>();
        ItemContainer bankContainer = client.getItemContainer(InventoryID.BANK);
        if (bankContainer == null)
        {
            return ids;
        }

        for (Item item : bankContainer.getItems())
        {
            if (item != null && item.getId() > 0)
            {
                ids.add(item.getId());
            }
        }

        return ids;
    }

    private int getItemIdForSlot(List<LayoutItem> items, int slot)
    {
        if (items == null)
        {
            return -1;
        }

        for (LayoutItem item : items)
        {
            if (item != null && item.getSlotIndex() == slot)
            {
                return item.getItemId();
            }
        }

        return -1;
    }
}
