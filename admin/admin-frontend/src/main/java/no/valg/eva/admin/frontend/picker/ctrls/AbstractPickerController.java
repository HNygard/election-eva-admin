package no.valg.eva.admin.frontend.picker.ctrls;

import no.evote.presentation.components.Action;
import no.valg.eva.admin.frontend.ConversationScopedController;
import no.valg.eva.admin.frontend.cdi.BeanManager;
import no.valg.eva.admin.frontend.picker.cfg.AreaCfg;
import no.valg.eva.admin.frontend.picker.cfg.ContextPickerCfg;
import no.valg.eva.admin.frontend.picker.cfg.ElectionCfg;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractPickerController extends ConversationScopedController implements Action {

    protected ContextPickerCfg cfg;
    protected Map<String, Object> requestParameterMap;

    @PostConstruct
    @SuppressWarnings("unchecked")
    protected void doInit() {

        try {
            Class<? extends ContextPickerCfg> cfgClass = (Class<? extends ContextPickerCfg>) Class.forName("no.valg.eva.admin.frontend.picker.cfg." + getRequestParameter("cfg"));
            cfg = BeanManager.lookup(cfgClass);

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }

        requestParameterMap = new HashMap<>();
        if (cfg.isKeepRequestParams()) {
            for (String parameterName : getCfg().getKeepRequestParams()) {
                String parameterValue = getRequestParameter(parameterName);
                if (parameterValue != null && !parameterValue.isEmpty()) {
                    requestParameterMap.put(parameterName, parameterValue);
                }
            }
        }
    }

    protected void appendRequestParam(StringBuilder result, String key, String value) {
        if (result.toString().contains("?")) {
            result.append("&").append(key).append("=").append(value);
        } else {
            result.append("?").append(key).append("=").append(value);
        }
    }

    @SuppressWarnings("unchecked")
    protected void appendExistingRequestParams(StringBuilder result) {

        List<String> parametersToKeep = cfg.getKeepRequestParams();

        if (requestParameterMap.size() > 0) {

            for (Object o : requestParameterMap.entrySet()) {
                Map.Entry<String, String> paramsPair = (Map.Entry<String, String>) o;
                if (parametersToKeep.contains(paramsPair.getKey())) {
                    appendRequestParam(result, paramsPair.getKey(), paramsPair.getValue());
                }
            }
        }
    }

    public ContextPickerCfg getCfg() {
        return cfg;
    }

    public AreaCfg getAreaCfg() {
        return cfg.getAreaCfg();
    }

    public ElectionCfg getElectionCfg() {
        return cfg.getElectionCfg();
    }
}
