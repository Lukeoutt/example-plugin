package com.lucas;

import java.util.ArrayList;
import java.util.List;
import net.runelite.client.config.Keybind;

public class Layout
{
    private String id;
    private String name;
    private List<LayoutItem> items = new ArrayList<>();
    private Keybind hotkey;
    private String category;

    public Layout()
    {
    }

    public Layout(String id, String name, List<LayoutItem> items, Keybind hotkey, String category)
    {
        this.id = id;
        this.name = name;
        if (items != null)
        {
            this.items = items;
        }
        this.hotkey = hotkey;
        this.category = category;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<LayoutItem> getItems()
    {
        return items;
    }

    public void setItems(List<LayoutItem> items)
    {
        this.items = items;
    }

    public Keybind getHotkey()
    {
        return hotkey;
    }

    public void setHotkey(Keybind hotkey)
    {
        this.hotkey = hotkey;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public int getItemIdForSlot(int slotIndex)
    {
        if (items == null)
        {
            return -1;
        }

        for (LayoutItem item : items)
        {
            if (item.getSlotIndex() == slotIndex)
            {
                return item.getItemId();
            }
        }

        return -1;
    }
}
