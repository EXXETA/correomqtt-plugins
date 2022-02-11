package org.correomqtt.plugins.base64;

import org.correomqtt.plugin.model.MessageExtensionDTO;
import org.correomqtt.plugin.spi.MessageIncomingHook;
import org.correomqtt.plugin.spi.PublishMessageHook;
import org.pf4j.Extension;

@Extension
public class Base64IO implements PublishMessageHook, MessageIncomingHook {

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
