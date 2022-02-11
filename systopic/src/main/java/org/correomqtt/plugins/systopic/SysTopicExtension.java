package org.correomqtt.plugins.systopic;

import javafx.scene.layout.HBox;
import org.correomqtt.plugins.systopic.controller.SysTopicButtonController;
import org.correomqtt.plugin.spi.MainToolbarHook;
import org.pf4j.Extension;

@Extension
public class SysTopicExtension implements MainToolbarHook {

    @Override
    public void onInstantiateMainToolbar(String connectionId, HBox controllViewButtonHBox, int indexToInsert) {
        SysTopicButtonController controller = new SysTopicButtonController(connectionId);
        SysTopicPlugin.loadFXML("/org/correomqtt/plugins/systopic/controller/sysTopicButton.fxml", controller);
        controller.addItems(controllViewButtonHBox, indexToInsert);
    }
}
