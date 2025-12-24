package com.lucas;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.ui.PluginPanel;

public class LoadoutPanel extends PluginPanel
{
    private final LucasLoadoutWheelPlugin plugin;
    private final ConfigManager configManager;
    private final LucasLoadoutConfig config;
    private final JPanel listPanel = new JPanel();
    private final JCheckBox overlayToggle = new JCheckBox("Inventory overlay");
    private final JCheckBox wheelToggle = new JCheckBox("Wheel overlay");

    public LoadoutPanel(LucasLoadoutWheelPlugin plugin, ConfigManager configManager, LucasLoadoutConfig config)
    {
        this.plugin = plugin;
        this.configManager = configManager;
        this.config = config;

        setLayout(new BorderLayout());

        JPanel controls = new JPanel();
        controls.setLayout(new GridLayout(0, 1, 0, 4));

        JButton captureButton = new JButton("Capture Layout");
        captureButton.addActionListener(e -> captureLayout());

        overlayToggle.setSelected(config.enableInventoryOverlay());
        overlayToggle.addActionListener(e -> configManager.setConfiguration(
            LucasLoadoutConfig.GROUP,
            "enableInventoryOverlay",
            overlayToggle.isSelected()
        ));

        wheelToggle.setSelected(config.enableWheel());
        wheelToggle.addActionListener(e -> configManager.setConfiguration(
            LucasLoadoutConfig.GROUP,
            "enableWheel",
            wheelToggle.isSelected()
        ));

        controls.add(captureButton);
        controls.add(overlayToggle);
        controls.add(wheelToggle);

        listPanel.setLayout(new GridLayout(0, 1, 0, 6));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(controls, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshLayouts(List<Layout> layouts, Layout activeLayout)
    {
        listPanel.removeAll();

        if (layouts.isEmpty())
        {
            listPanel.add(new JLabel("No layouts captured yet."));
        }
        else
        {
            for (Layout layout : layouts)
            {
                listPanel.add(createLayoutRow(layout, layout == activeLayout));
            }
        }

        revalidate();
        repaint();
    }

    private JPanel createLayoutRow(Layout layout, boolean isActive)
    {
        JPanel row = new JPanel(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JLabel nameLabel = new JLabel(isActive ? layout.getName() + " (active)" : layout.getName());
        row.add(nameLabel, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

        JButton activateButton = new JButton("Activate");
        activateButton.addActionListener(e -> plugin.setActiveLayout(layout));

        JButton recaptureButton = new JButton("Recapture");
        recaptureButton.addActionListener(e -> plugin.updateLayoutFromInventory(layout));

        JButton renameButton = new JButton("Rename");
        renameButton.addActionListener(e -> renameLayout(layout));

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteLayout(layout));

        JButton hotkeyButton = new JButton(hotkeyLabel(layout.getHotkey()));
        hotkeyButton.addActionListener(e -> setHotkey(layout));

        actions.add(activateButton);
        actions.add(recaptureButton);
        actions.add(renameButton);
        actions.add(deleteButton);
        actions.add(hotkeyButton);

        row.add(actions, BorderLayout.CENTER);
        return row;
    }

    private void captureLayout()
    {
        String name = JOptionPane.showInputDialog(this, "Layout name:");
        plugin.addLayoutFromInventory(name);
    }

    private void renameLayout(Layout layout)
    {
        String name = JOptionPane.showInputDialog(this, "Rename layout:", layout.getName());
        plugin.renameLayout(layout, name);
    }

    private void deleteLayout(Layout layout)
    {
        int result = JOptionPane.showConfirmDialog(this, "Delete layout '" + layout.getName() + "'?", "Delete Layout",
            JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION)
        {
            plugin.deleteLayout(layout);
        }
    }

    private void setHotkey(Layout layout)
    {
        Keybind keybind = KeybindDialog.captureKeybind(this, "Set Hotkey");
        if (keybind != null)
        {
            plugin.setLayoutHotkey(layout, keybind);
        }
    }

    private String hotkeyLabel(Keybind hotkey)
    {
        if (hotkey == null || Keybind.NOT_SET.equals(hotkey))
        {
            return "Set Hotkey";
        }

        return "Hotkey: " + hotkey.toString();
    }
}
