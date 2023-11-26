package org.dtomics.gameengine.specification.components;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dtomics.gameengine.specification.window.Window;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Entity {

    private boolean initialized;
    @Getter
    private final String name;
    private final List<Component> components = new ArrayList<>();

    public void addComponent(Component component) {
        if (component == null) {
            return;
        }
        components.add(component);
        if (initialized) {
            component.init();
        }
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (int i = 0; i < components.size(); i++) {
            final var component = components.get(i);
            if (!componentClass.isAssignableFrom(component.getClass())) {
                continue;
            }
            return componentClass.cast(component);
        }
        return null;
    }
    public <T extends Component> void removeComponent(Class<T> componentClass) {
        for (int i = 0; i < components.size(); i++) {
            final var component = components.get(i);
            if (!componentClass.isAssignableFrom(component.getClass())) {
                continue;
            }
            components.remove(i);
            --i;
        }
    }

    public void init() {
        if (initialized) {
            return;
        }
        components.forEach(Component::init);
        initialized = true;
    }

    public void update(Window window) {
        for (int i = 0; i < components.size(); i++) {
            components.get(i).update(window);
        }
    }
}
