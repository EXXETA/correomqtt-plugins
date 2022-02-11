package org.correomqtt.plugins.contains_string_validator;

import org.correomqtt.plugin.spi.ExtensionId;
import org.correomqtt.plugin.spi.MessageValidatorHook;
import org.pf4j.Extension;

@Extension(points = {MessageValidatorHook.class})
@ExtensionId("ignoreCase")
public class IgnoreCaseContainsStringValidator extends ContainsStringValidator {

    @Override
    public Validation isMessageValid(String payload) {
        String tooltip = "Contains '" + text + "' - case ignored";
        return new Validation(payload.toLowerCase().contains(text.toLowerCase()), tooltip);
    }
}
