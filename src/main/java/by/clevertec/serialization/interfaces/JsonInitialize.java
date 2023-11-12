package by.clevertec.serialization.interfaces;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public interface JsonInitialize {
    void initializeWrapperOrPrimitive(Class<?> fieldType, String sourceJson, String fieldName, Object instance,
                                      Method setter)
            throws InvocationTargetException, IllegalAccessException;

    void initializeCollection(String sourceJson, String fieldName, Object instance,
                              Method setter);

    void initializeIntegratedClassObject(Class<?> fieldType, String sourceJson, String fieldName, Object instance,

                                         Method setter) throws InvocationTargetException, IllegalAccessException;

    void initializeLocalDateField(Method method, Object instance, String json, String fieldName)
            throws InvocationTargetException, IllegalAccessException;

    void initializeListFieldThatNumbers(String json, String fieldName, Method setter,
                                        Object instance, Class<?> parameter)
            throws InvocationTargetException, IllegalAccessException;

    void initializeMap(String json, String fieldName, Method setter, Object instance, Class<?> classType)
            throws NoSuchFieldException, InvocationTargetException, InstantiationException, IllegalAccessException,
            NoSuchMethodException;

    void initializeListFieldThatObjects(String json, String fieldName, Method setter, Object instance,
                                        Class<?> parameter)
            throws InvocationTargetException, IllegalAccessException, NoSuchFieldException,
            InstantiationException, NoSuchMethodException;

    default Map<String, String> parseKeyAndValueForMapInObject(String jsonForMap) {

        Map<String, String> keysValues = new HashMap<>();
        char[] jsonChars = jsonForMap.toCharArray();
        int inputSymbol = 0, outputSymbol = 0;

        for (int i = 0; i < jsonChars.length; i++) {
            if (jsonChars[i] == '\"') {

                StringBuilder key = new StringBuilder();
                i++;
                while (jsonChars[i] != '\"') {
                    key.append(jsonChars[i]);
                    i++;
                }
                i += 2;

                StringBuilder value = new StringBuilder();
                if (jsonChars[i] == '{') {
                    inputSymbol++;
                    i++;
                }
                do {
                    if (jsonChars[i] == '{') {
                        inputSymbol++;
                    }
                    if (jsonChars[i] == '}') {
                        outputSymbol++;
                    }
                    value.append(jsonChars[i]);
                    i++;
                } while (inputSymbol != outputSymbol);
                keysValues.put(key.toString(), value.toString());
            }
        }
        return keysValues;
    }
}
