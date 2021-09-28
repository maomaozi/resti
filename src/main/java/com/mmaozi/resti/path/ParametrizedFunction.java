package com.mmaozi.resti.path;

import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@AllArgsConstructor(staticName = "of")
public class ParametrizedFunction {
    private final Method method;
    private final List<String> parameters;

    public Object invoke(Object obj, Map<String, String> namedParameters) throws InvocationTargetException, IllegalAccessException {
        Object[] parameterValues = new Object[parameters.size()];

        for (int i = 0; i < parameters.size(); i++) {
            parameterValues[i] = namedParameters.get(parameters.get(i));
        }

        return method.invoke(obj, parameterValues);
    }
}
