package org.correomqtt.plugins.save;

import org.correomqtt.plugin.manager.PermissionPlugin;
import javafx.util.FXPermission;
import org.pf4j.PluginWrapper;

import java.io.FilePermission;
import java.security.Permissions;

public class SavePlugin extends PermissionPlugin {

    public SavePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public Permissions getPermissions() {
        Permissions permissions = new Permissions();
        permissions.add(new FXPermission("accessWindowList"));
        permissions.add(new FilePermission("<<ALL FILES>>", "write"));
        return permissions;
    }
}
