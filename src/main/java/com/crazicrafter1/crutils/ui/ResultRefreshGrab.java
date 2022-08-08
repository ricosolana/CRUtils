package com.crazicrafter1.crutils.ui;

import org.bukkit.event.inventory.InventoryClickEvent;

class ResultRefreshGrab extends Result {

    @Override
    public void invoke(AbstractMenu menu, InventoryClickEvent event) {
        new ResultGrab().invoke(menu, event);
        new ResultRefresh().invoke(menu, event);
    }

}
