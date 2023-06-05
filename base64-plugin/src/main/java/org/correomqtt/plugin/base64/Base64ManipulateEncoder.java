package org.correomqtt.plugin.base64;

import org.correomqtt.plugin.spi.DetailViewManipulatorHook;
import org.correomqtt.plugin.spi.ExtensionId;
import org.pf4j.Extension;

@Extension
@ExtensionId("encode")
public class Base64ManipulateEncoder implements DetailViewManipulatorHook {

    @Override
    public byte[] manipulate(byte[] bytes) {
        return Base64Utils.encode(bytes);
    }
}
