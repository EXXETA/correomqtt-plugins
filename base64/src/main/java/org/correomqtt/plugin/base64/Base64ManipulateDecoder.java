package org.correomqtt.plugin.base64;

import org.correomqtt.plugin.spi.DetailViewManipulatorHook;
import org.correomqtt.plugin.spi.ExtensionId;
import org.pf4j.Extension;

@Extension
@ExtensionId("decode")
public class Base64ManipulateDecoder implements DetailViewManipulatorHook {

    @Override
    public byte[] manipulate(byte[] selection) {
        return Base64Utils.decode(selection);
    }
}
