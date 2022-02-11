package org.correomqtt.plugins.xml_xsd_validator;

import org.correomqtt.plugin.manager.PermissionPlugin;
import org.correomqtt.plugin.spi.MessageValidatorHook;
import org.jdom2.Element;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

@Extension
public class XmlXsdValidator implements MessageValidatorHook {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlXsdValidator.class);

    private String schemaFile;
    private PermissionPlugin plugin;

    public XmlXsdValidator(Plugin plugin) {
        this.plugin = (PermissionPlugin) plugin;
    }

    @Override
    public void onConfigReceived(Element config) {
        schemaFile = config.getChildText("schema");
    }

    @Override
    public Validation isMessageValid(String payload) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(plugin.getPluginConfigFolder(), schemaFile));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(payload)));
        } catch (IOException | SAXException e) {
            LOGGER.error(e.getMessage());
            return new Validation(false, e.getMessage());
        } catch (SecurityException e) {
            LOGGER.error("Please put your schema file into {}", plugin.getPluginConfigFolder());
            return new Validation(false, "XSD file not found");
        }
        return new Validation(true, schemaFile);
    }
}
