package com.exxeta.correomqtt.plugins.manipulator;

import org.correomqtt.plugin.spi.DetailViewManipulatorHook;
import org.correomqtt.plugin.spi.ExtensionId;
import org.pf4j.Extension;

@Extension
@ExtensionId("unzip")
public class Unzipper implements DetailViewManipulatorHook {

    @Override
    public byte[] manipulate(byte[] bytes) {
        return ZipUtils.unzip(bytes);
    }
}
