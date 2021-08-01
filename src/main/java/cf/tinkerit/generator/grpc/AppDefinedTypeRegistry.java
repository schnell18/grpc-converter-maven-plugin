package cf.tinkerit.generator.grpc;

import cf.tinkerit.generator.grpc.impl.grpc.model.Type;
import cf.tinkerit.generator.grpc.impl.JavaClassCategory;

import java.util.HashMap;
import java.util.Map;

public class AppDefinedTypeRegistry {
    private final Map<Integer, Map<String, Object>> appDefinedTypeRegistry;

    public AppDefinedTypeRegistry(int size) {
        this.appDefinedTypeRegistry = new HashMap<>(size);
    }

    public Map<String, Object> getSubRegistry(JavaClassCategory cat) {
        return appDefinedTypeRegistry.get(cat.getCategory());
    }

    public void registerType(JavaClassCategory key, String qClassName, Object parsedModel) {
        Map<String, Object> map = this.appDefinedTypeRegistry.computeIfAbsent(
            key.getCategory(), k -> new HashMap<>());
        map.put(qClassName, parsedModel);
    }

    public Type resolveType(String fqCls) {
       return (Type) appDefinedTypeRegistry
            .get(JavaClassCategory.Constants.CAT_DUBBO_MODEL).get(fqCls);
    }

    public boolean isTypeDefined(String fqCls) {
        for (Integer cat : this.appDefinedTypeRegistry.keySet()) {

            if (this.appDefinedTypeRegistry.get(cat).containsKey(fqCls)) {
                return true;
            }
        }
        return false;
    }
}
