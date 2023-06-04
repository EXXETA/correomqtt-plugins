package org.correomqtt.plugin.zip_manipulator;

import org.correomqtt.plugin.spi.DetailViewManipulatorHook;
import org.correomqtt.plugin.spi.ExtensionId;
import org.pf4j.Extension;

@Extension
@ExtensionId("zip")
public class Zipper implements DetailViewManipulatorHook {

    @Override
    public byte[] manipulate(byte[] bytes) {
        return ZipUtils.zip(bytes);
    }
}
