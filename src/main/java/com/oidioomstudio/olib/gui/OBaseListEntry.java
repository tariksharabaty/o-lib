package com.oidioomstudio.olib.gui;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;

/**
 * Base abstract entry for items in {@link OOptionList}.
 * Allows both configurable options and section/group headers in the same selection list.
 */
public abstract class OBaseListEntry extends ContainerObjectSelectionList.Entry<OBaseListEntry> {
    public void updateWidgetState() {
        // Subclasses with interactive widgets can override this
    }
}
