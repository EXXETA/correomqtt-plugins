package org.correomqtt.plugins.systopic;

import javafx.fxml.FXMLLoader;
import javafx.util.FXPermission;
import org.correomqtt.plugin.manager.PermissionPlugin;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import java.io.FilePermission;
import java.io.IOException;
import java.security.Permissions;
import java.util.PropertyPermission;

public class SysTopicPlugin extends PermissionPlugin {

    public SysTopicPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public Permissions getPermissions() {
        Permissions permissions = new Permissions();
        permissions.add(new FXPermission("accessWindowList"));
        permissions.add(new FXPermission("accessClipboard"));
        permissions.add(new FilePermission("<<ALL FILES>>", "read,write"));
        permissions.add(new PropertyPermission("*", "read"));
        return permissions;
    }

     static void loadFXML(String resourceName, Object controller) {
        FXMLLoader loader = new FXMLLoader(controller.getClass().getResource(resourceName));
        loader.setController(controller);
        try {
            loader.load();
        } catch (IOException e) {
            throw new PluginRuntimeException("Failed to load layout file");
        }
    }
}
