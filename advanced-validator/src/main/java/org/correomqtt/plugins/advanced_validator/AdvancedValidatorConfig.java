package org.correomqtt.plugins.advanced_validator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.correomqtt.business.model.HooksDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvancedValidatorConfig {

    private List<AdvancedValidatorConfig> and;

    private List<AdvancedValidatorConfig> or;

    private List<HooksDTO.Extension> extensions;

}
