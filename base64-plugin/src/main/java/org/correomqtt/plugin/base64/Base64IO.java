package org.correomqtt.plugin.base64;

import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import org.correomqtt.plugin.model.MessageExtensionDTO;
import org.correomqtt.plugin.spi.ExtensionId;
import org.correomqtt.plugin.spi.IncomingMessageHook;
import org.correomqtt.plugin.spi.OutgoingMessageHook;
import org.pf4j.Extension;

@Extension
@ExtensionId("base64.io")
public class Base64IO implements OutgoingMessageHook<Base64IOConfigDTO>, IncomingMessageHook<Base64IOConfigDTO> {

    private Base64IOConfigDTO config;

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

    @Override
    public Base64IOConfigDTO getConfig() {
        return config;
    }

    @Override
    public void onConfigReceived(Base64IOConfigDTO config) {
        this.config = config;
    }


    @Override
    public Class<Base64IOConfigDTO> getConfigClass() {
        return Base64IOConfigDTO.class;
    }
}
