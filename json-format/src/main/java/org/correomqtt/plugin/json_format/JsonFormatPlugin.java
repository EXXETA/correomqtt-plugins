package org.correomqtt.plugin.json_format;

import org.correomqtt.plugin.manager.PermissionPlugin;
import org.pf4j.PluginWrapper;

import java.security.Permissions;

public class JsonFormatPlugin extends PermissionPlugin {

    public JsonFormatPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public Permissions getPermissions() {
        Permissions permissions = new Permissions();
        permissions.add(new RuntimePermission("accessDeclaredMembers"));
        return permissions;
    }
}
