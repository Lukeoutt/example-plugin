package com.lucas;

public class LayoutItem
{
    private int itemId;
    private int slotIndex;

    public LayoutItem()
    {
    }

    public LayoutItem(int itemId, int slotIndex)
    {
        this.itemId = itemId;
        this.slotIndex = slotIndex;
    }

    public int getItemId()
    {
        return itemId;
    }

    public void setItemId(int itemId)
    {
        this.itemId = itemId;
    }

    public int getSlotIndex()
    {
        return slotIndex;
    }

    public void setSlotIndex(int slotIndex)
    {
        this.slotIndex = slotIndex;
    }
}
