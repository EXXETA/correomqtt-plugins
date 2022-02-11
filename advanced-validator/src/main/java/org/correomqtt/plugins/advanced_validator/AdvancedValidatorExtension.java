package org.correomqtt.plugins.advanced_validator;

import org.correomqtt.plugin.manager.PluginManager;
import org.correomqtt.plugin.spi.MessageValidatorHook;
import org.jdom2.Element;
import org.pf4j.Extension;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Extension
public class AdvancedValidatorExtension implements MessageValidatorHook {

    private ArrayList<MessageValidatorHook> validators;

    @Override
    public void onConfigReceived(Element config) {
        // TODO come up with a nice protocol structure to combine different validators to support OR and AND algebra
        this.validators = PluginManager.getInstance().getExtensions(MessageValidatorHook.class, config).stream()
                .filter(v -> !v.getClass().isAssignableFrom(this.getClass()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Validation isMessageValid(String message) {
        return new Validation(validators.stream().allMatch(v -> v.isMessageValid(message).isValid()), "All valid");
    }
}
