package org.correomqtt.plugins.base64;

import org.correomqtt.plugin.model.MessageExtensionDTO;
import org.correomqtt.plugin.spi.IncomingMessageHook;
import org.correomqtt.plugin.spi.OutgoingMessageHook;
import org.pf4j.Extension;

@Extension
public class Base64IO implements OutgoingMessageHook, IncomingMessageHook {

    @Override
    public MessageExtensionDTO onMessageIncoming(String connectionId, MessageExtensionDTO extensionMessageDTO) {
        extensionMessageDTO.setPayload(new String(Base64Utils.decode(extensionMessageDTO.getPayload().getBytes())));
        return extensionMessageDTO;
    }

    @Override
    public MessageExtensionDTO onPublishMessage(String connectionId, MessageExtensionDTO extensionMessageDTO) {
        extensionMessageDTO.setPayload(new String(Base64Utils.encode(extensionMessageDTO.getPayload().getBytes())));
        return extensionMessageDTO;
    }
}
