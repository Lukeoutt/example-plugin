package com.lucas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class LoadoutWheelOverlay extends Overlay
{
    public static final int MAX_LAYOUTS = 8;

    private static final Color SLICE_COLOR = new Color(20, 20, 20, 180);
    private static final Color SLICE_HIGHLIGHT = new Color(0, 160, 110, 210);
    private static final Color TEXT_COLOR = new Color(240, 240, 240, 220);

    private final Client client;
    private final LucasLoadoutConfig config;
    private final LucasLoadoutWheelPlugin plugin;

    private boolean active;
    private int selectedIndex = -1;

    @Inject
    public LoadoutWheelOverlay(Client client, LucasLoadoutConfig config, LucasLoadoutWheelPlugin plugin)
    {
        this.client = client;
        this.config = config;
        this.plugin = plugin;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public void setActive(boolean active)
    {
        this.active = active;
        if (!active)
        {
            selectedIndex = -1;
        }
    }

    public Layout getSelectedLayout()
    {
        List<Layout> layouts = plugin.getWheelLayouts();
        if (selectedIndex < 0 || selectedIndex >= layouts.size())
        {
            return null;
        }
        return layouts.get(selectedIndex);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!active || !config.enableWheel())
        {
            return null;
        }

        List<Layout> layouts = plugin.getWheelLayouts();
        if (layouts.isEmpty())
        {
            return null;
        }

        int canvasWidth = client.getCanvasWidth();
        int canvasHeight = client.getCanvasHeight();
        int radius = Math.max(80, Math.min(canvasWidth, canvasHeight) / 4);
        int diameter = radius * 2;

        int centerX = canvasWidth / 2;
        int centerY = canvasHeight / 2;

        Point mouse = client.getMouseCanvasPosition();
        int mouseX = mouse.getX();
        int mouseY = mouse.getY();

        double dx = mouseX - centerX;
        double dy = centerY - mouseY;
        double angle = Math.atan2(dy, dx);
        if (angle < 0)
        {
            angle += Math.PI * 2;
        }

        double sliceAngle = (Math.PI * 2) / layouts.size();
        selectedIndex = (int) Math.floor((angle + sliceAngle / 2) / sliceAngle) % layouts.size();

        int x = centerX - radius;
        int y = centerY - radius;

        graphics.setColor(SLICE_COLOR);
        graphics.fillOval(x, y, diameter, diameter);

        FontMetrics metrics = graphics.getFontMetrics();

        for (int i = 0; i < layouts.size(); i++)
        {
            double start = i * sliceAngle;
            int startDeg = (int) Math.toDegrees(start);
            int arcDeg = (int) Math.toDegrees(sliceAngle);
            graphics.setColor(i == selectedIndex ? SLICE_HIGHLIGHT : SLICE_COLOR);
            graphics.fillArc(x, y, diameter, diameter, startDeg, arcDeg);

            Layout layout = layouts.get(i);
            String label = layout.getName();
            double labelAngle = start + sliceAngle / 2;
            int labelX = (int) (centerX + Math.cos(labelAngle) * radius * 0.6);
            int labelY = (int) (centerY - Math.sin(labelAngle) * radius * 0.6);
            int textWidth = metrics.stringWidth(label);
            int textHeight = metrics.getAscent();
            graphics.setColor(TEXT_COLOR);
            graphics.drawString(label, labelX - textWidth / 2, labelY + textHeight / 2);
        }

        return null;
    }
}
