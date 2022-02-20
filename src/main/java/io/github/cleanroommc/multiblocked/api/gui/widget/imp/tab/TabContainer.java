package io.github.cleanroommc.multiblocked.api.gui.widget.imp.tab;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.cleanroommc.multiblocked.api.gui.widget.WidgetGroup;

import java.util.Optional;

public class TabContainer extends WidgetGroup {

    protected final BiMap<TabButton, WidgetGroup> tabs = HashBiMap.create();
    protected final WidgetGroup buttonGroup;
    protected final WidgetGroup containerGroup;
    protected WidgetGroup focus;

    public TabContainer(int x, int y, int width, int height) {
        super(x, y, width, height);
        buttonGroup = new WidgetGroup(x, y, width, height);
        containerGroup = new WidgetGroup(x, y, width, height);
        this.addWidget(containerGroup);
        this.addWidget(buttonGroup);
    }

    public void switchTag(WidgetGroup tabWidget) {
        if (focus == tabWidget) return;
        if (focus != null) {
            tabs.inverse().get(focus).setPressed(false);
            focus.setVisible(false);
            focus.setActive(false);
        }
        focus= tabWidget;
        Optional.ofNullable(tabs.inverse().get(tabWidget)).ifPresent(tab -> {
            tab.setPressed(true);
            tabWidget.setActive(true);
            tabWidget.setVisible(true);
        });
    }

    public void addTab(TabButton tabButton, WidgetGroup tabWidget) {
        tabButton.setContainer(this);
        tabs.put(tabButton, tabWidget);
        containerGroup.addWidget(tabWidget);
        buttonGroup.addWidget(tabButton);
        if (focus == null) {
            focus = tabWidget;
        }
        tabButton.setPressed(focus == tabWidget);
        tabWidget.setVisible(focus == tabWidget);
        tabWidget.setActive(focus == tabWidget);
    }
}
