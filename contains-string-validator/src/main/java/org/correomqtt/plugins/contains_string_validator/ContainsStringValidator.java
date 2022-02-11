package org.correomqtt.plugins.contains_string_validator;

import org.correomqtt.plugin.spi.MessageValidatorHook;
import org.jdom2.Element;

public abstract class ContainsStringValidator implements MessageValidatorHook {

    protected String text;

    @Override
    public void onConfigReceived(Element config) {
        text = config.getChildText("string");
    }
}
