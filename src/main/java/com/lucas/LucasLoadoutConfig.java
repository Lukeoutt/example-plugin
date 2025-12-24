package com.lucas;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup(LucasLoadoutConfig.GROUP)
public interface LucasLoadoutConfig extends Config
{
    String GROUP = "lucasloadout";

    @ConfigItem(
        keyName = "enableInventoryOverlay",
        name = "Enable inventory overlay",
        description = "Show inventory slot highlights while the bank is open"
    )
    default boolean enableInventoryOverlay()
    {
        return true;
    }

    @ConfigItem(
        keyName = "enableWheel",
        name = "Enable wheel",
        description = "Enable the radial layout wheel"
    )
    default boolean enableWheel()
    {
        return true;
    }

    @ConfigItem(
        keyName = "enableBankProjection",
        name = "Enable bank projection",
        description = "Show layout markers and preview inside the bank"
    )
    default boolean enableBankProjection()
    {
        return true;
    }

    @ConfigItem(
        keyName = "wheelKey",
        name = "Wheel key",
        description = "Hold to open the layout wheel"
    )
    default Keybind wheelKey()
    {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
        keyName = "layoutsJson",
        name = "Layouts JSON",
        description = "Serialized layout data",
        hidden = true
    )
    default String layoutsJson()
    {
        return "";
    }
}
