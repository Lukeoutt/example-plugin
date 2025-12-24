package com.lucas;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import java.awt.event.KeyEvent;
import net.runelite.client.input.KeyListener;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
    name = "Lucas Loadout Wheel",
    description = "Inventory loadouts, overlays, and an optional wheel selector"
)
public class LucasLoadoutWheelPlugin extends Plugin implements KeyListener
{
    private static final int INVENTORY_SIZE = 28;
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Keybind.class, new KeybindAdapter())
        .create();
    private static final Type LAYOUT_LIST_TYPE = new TypeToken<List<Layout>>()
    {
    }.getType();

    @Inject
    private Client client;

    @Inject
    private LucasLoadoutConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private LoadoutInventoryOverlay inventoryOverlay;

    @Inject
    private LoadoutWheelOverlay wheelOverlay;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private KeyManager keyManager;

    private final List<Layout> layouts = new ArrayList<>();
    private Layout currentLayout;
    private boolean loggedIn;
    private boolean bankOpen;
    private boolean wheelActive;

    private LoadoutPanel panel;
    private NavigationButton navButton;

    @Provides
    LucasLoadoutConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(LucasLoadoutConfig.class);
    }

    @Override
    protected void startUp()
    {
        loggedIn = client.getGameState() == GameState.LOGGED_IN;
        bankOpen = client.getWidget(WidgetInfo.BANK_CONTAINER) != null;
        loadLayouts();

        panel = new LoadoutPanel(this, configManager, config);
        navButton = NavigationButton.builder()
            .tooltip("Lucas Loadout Wheel")
            .icon(ImageUtil.loadImageResource(LucasLoadoutWheelPlugin.class, "loadout_wheel.png"))
            .priority(6)
            .panel(panel)
            .build();
        clientToolbar.addNavigation(navButton);

        overlayManager.add(inventoryOverlay);
        overlayManager.add(wheelOverlay);
        keyManager.registerKeyListener(this);

        refreshPanel();
    }

    @Override
    protected void shutDown()
    {
        keyManager.unregisterKeyListener(this);
        overlayManager.remove(inventoryOverlay);
        overlayManager.remove(wheelOverlay);

        if (navButton != null)
        {
            clientToolbar.removeNavigation(navButton);
            navButton = null;
        }

        panel = null;
        wheelActive = false;
        bankOpen = false;
        loggedIn = false;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        loggedIn = event.getGameState() == GameState.LOGGED_IN;
        if (!loggedIn)
        {
            bankOpen = false;
            wheelActive = false;
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        if (event.getGroupId() == WidgetInfo.BANK_CONTAINER.getGroupId())
        {
            bankOpen = true;
        }
    }

    @Subscribe
    public void onWidgetClosed(WidgetClosed event)
    {
        if (event.getGroupId() == WidgetInfo.BANK_CONTAINER.getGroupId())
        {
            bankOpen = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (config.enableWheel() && config.wheelKey().matches(e))
        {
            wheelActive = true;
            wheelOverlay.setActive(true);
            return;
        }

        for (Layout layout : layouts)
        {
            Keybind hotkey = layout.getHotkey();
            if (hotkey != null && hotkey.matches(e))
            {
                setActiveLayout(layout);
                return;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        if (!wheelActive)
        {
            return;
        }

        if (config.enableWheel() && config.wheelKey().matches(e))
        {
            wheelActive = false;
            wheelOverlay.setActive(false);
            Layout selection = wheelOverlay.getSelectedLayout();
            if (selection != null)
            {
                setActiveLayout(selection);
            }
        }
    }

    public List<Layout> getLayouts()
    {
        return Collections.unmodifiableList(layouts);
    }

    public Layout getCurrentLayout()
    {
        return currentLayout;
    }

    public boolean isLoggedIn()
    {
        return loggedIn;
    }

    public boolean isBankOpen()
    {
        return bankOpen;
    }

    public boolean isWheelActive()
    {
        return wheelActive;
    }

    public List<Layout> getWheelLayouts()
    {
        if (layouts.isEmpty())
        {
            return Collections.emptyList();
        }
        int size = Math.min(layouts.size(), LoadoutWheelOverlay.MAX_LAYOUTS);
        return new ArrayList<>(layouts.subList(0, size));
    }

    public Layout addLayoutFromInventory(String name)
    {
        String layoutName = name == null ? "" : name.trim();
        if (layoutName.isEmpty())
        {
            layoutName = "Layout " + (layouts.size() + 1);
        }

        Layout layout = new Layout(UUID.randomUUID().toString(), layoutName, captureInventory(), null, null);
        layouts.add(layout);
        setActiveLayout(layout);
        persistLayouts();
        refreshPanel();
        return layout;
    }

    public void updateLayoutFromInventory(Layout layout)
    {
        if (layout == null)
        {
            return;
        }

        layout.setItems(captureInventory());
        persistLayouts();
        refreshPanel();
    }

    public void renameLayout(Layout layout, String name)
    {
        if (layout == null)
        {
            return;
        }

        String layoutName = name == null ? "" : name.trim();
        if (layoutName.isEmpty())
        {
            return;
        }

        layout.setName(layoutName);
        persistLayouts();
        refreshPanel();
    }

    public void deleteLayout(Layout layout)
    {
        if (layout == null)
        {
            return;
        }

        layouts.remove(layout);
        if (currentLayout == layout)
        {
            currentLayout = null;
        }
        persistLayouts();
        refreshPanel();
    }

    public void setLayoutHotkey(Layout layout, Keybind hotkey)
    {
        if (layout == null)
        {
            return;
        }

        if (hotkey != null && Keybind.NOT_SET.equals(hotkey))
        {
            hotkey = null;
        }

        layout.setHotkey(hotkey);
        persistLayouts();
        refreshPanel();
    }

    public void setActiveLayout(Layout layout)
    {
        currentLayout = layout;
        refreshPanel();
    }

    private void refreshPanel()
    {
        if (panel != null)
        {
            panel.refreshLayouts(getLayouts(), currentLayout);
        }
    }

    private List<LayoutItem> captureInventory()
    {
        List<LayoutItem> items = new ArrayList<>(INVENTORY_SIZE);
        ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
        Item[] inventory = container == null ? new Item[0] : container.getItems();

        for (int i = 0; i < INVENTORY_SIZE; i++)
        {
            int itemId = i < inventory.length ? inventory[i].getId() : -1;
            items.add(new LayoutItem(itemId, i));
        }

        return items;
    }

    private void loadLayouts()
    {
        layouts.clear();
        String json = config.layoutsJson();
        if (json == null || json.isEmpty())
        {
            return;
        }

        try
        {
            List<Layout> loaded = GSON.fromJson(json, LAYOUT_LIST_TYPE);
            if (loaded != null)
            {
                layouts.addAll(loaded);
            }
        }
        catch (Exception ex)
        {
            log.warn("Failed to load layouts", ex);
        }
    }

    private void persistLayouts()
    {
        String json = GSON.toJson(layouts, LAYOUT_LIST_TYPE);
        configManager.setConfiguration(LucasLoadoutConfig.GROUP, "layoutsJson", json);
    }
}

