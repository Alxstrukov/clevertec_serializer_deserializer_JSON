package by.clevertec.serialization.model;

import by.clevertec.serialization.interfaces.JsonInitialize;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static by.clevertec.serialization.utils.ClassTypeName.BOOLEAN;
import static by.clevertec.serialization.utils.ClassTypeName.BOOLEAN_W;
import static by.clevertec.serialization.utils.ClassTypeName.CHAR;
import static by.clevertec.serialization.utils.ClassTypeName.CHAR_W;

public class Deserializer extends AbstractMapper implements JsonInitialize {

    public <T> T mapJsonToObject(String sourceJson, Class<T> classType)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException,
            NoSuchFieldException {

        if (isWrapperOrPrimitive(classType)) {
            return (T) mapPrimitiveToWrapper(sourceJson, classType);
        }

        Field[] declaredFields = classType.getDeclaredFields();

        T instance = classType.getDeclaredConstructor().newInstance();

        for (Field field : declaredFields) {

            Class<?> fieldType = field.getType();
            String fieldName = field.getName();
            String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            Method setter = classType.getMethod(setterName, fieldType);

            initField(fieldType, sourceJson, fieldName, instance, setter, classType);
            sourceJson = trimJson(sourceJson, fieldName);
        }
        return instance;
    }

    private void initField(Class<?> fieldType, String sourceJson, String fieldName, Object instance,
                           Method setter, Class<?> classType) throws InvocationTargetException, IllegalAccessException,
            NoSuchFieldException, InstantiationException, NoSuchMethodException {
        if (isWrapperOrPrimitive(fieldType)) {
            initializeWrapperOrPrimitive(fieldType, sourceJson, fieldName, instance, setter);
        } else {
            initializeCollection(fieldType, sourceJson, fieldName, instance, setter, classType);
        }
    }

    public void initializeWrapperOrPrimitive(Class<?> fieldType, String sourceJson, String fieldName, Object instance,
                                             Method setter)
            throws InvocationTargetException, IllegalAccessException {
        String value;
        if (fieldType.getTypeName().equals(BOOLEAN) || fieldType.getTypeName().equals(BOOLEAN_W)) {
            value = getSymbolString(sourceJson, fieldName);
            if (value.equals("null")) {
                setter.invoke(instance, (Object) null);
            } else {
                setter.invoke(instance, Boolean.valueOf(value));
            }
        } else {
            if (fieldType.getTypeName().equals(CHAR) || fieldType.getTypeName().equals(CHAR_W)) {

                value = getStringValue(sourceJson, fieldName);
                if (value.equals("null")) {
                    setter.invoke(instance, (Object) null);
                } else {
                    setter.invoke(instance, value.charAt(0));
                }
            } else {
                value = getNumberString(sourceJson, fieldName);
                Number number = mapPrimitiveToWrapper(value, fieldType);
                setter.invoke(instance, number);
            }
        }
    }


    public void initializeCollection(Class<?> fieldType, String sourceJson, String fieldName, Object instance,
                                     Method setter, Class<?> classType) throws NoSuchFieldException,
            InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        if (isCollection(fieldType)) {
            Class<?> parameter = (Class<?>) ((ParameterizedType) setter
                    .getGenericParameterTypes()[0])
                    .getActualTypeArguments()[0];
            if (isWrapperOrPrimitive(parameter)) {
                try {
                    initializeListFieldThatNumbers(sourceJson, fieldName, setter, instance, parameter);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    initializeListFieldThatObjects(sourceJson, fieldName, setter, instance, parameter);
                } catch (InvocationTargetException
                        | IllegalAccessException | NoSuchFieldException
                        | InstantiationException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        } else {
            initializeMap(fieldType, sourceJson, fieldName, setter, instance, classType);
        }

    }

    public void initializeIntegratedClassObject(Class<?> fieldType, String sourceJson, String fieldName, Object instance,
                                                Method setter) throws InvocationTargetException, IllegalAccessException,
            NoSuchFieldException, InstantiationException, NoSuchMethodException {
        if (isIntegratedClassObject(fieldType.getPackageName())) {
            String value;
            if (fieldType.equals(UUID.class)) {
                value = getUuidValue(sourceJson, fieldName);
                if (value == null) {

                    setter.invoke(instance, (Object) null);
                } else {
                    setter.invoke(instance, UUID.fromString(value));
                }
            } else {
                if (fieldType.equals(LocalDate.class)) {
                    initializeLocalDateField(setter, instance, sourceJson, fieldName);
                } else {
                    value = getStringValue(sourceJson, fieldName);
                    setter.invoke(instance, value);
                }
            }
        } else {
            String providedJson = getProvidedJson(sourceJson, fieldName);
            setter.invoke(instance, mapJsonToObject(providedJson, fieldType));
        }
    }

    public void initializeLocalDateField(Method method, Object instance, String json, String fieldName)
            throws InvocationTargetException, IllegalAccessException {
        String[] value = getLocalDateValue(json, fieldName);

        LocalDate date = LocalDate.of(Integer.parseInt(value[0]), Integer.parseInt(value[1]), Integer.parseInt(value[2]));
        method.invoke(instance, date);
    }

    public void initializeListFieldThatNumbers(String json, String fieldName, Method setter,
                                               Object instance, Class<?> parameter)
            throws InvocationTargetException, IllegalAccessException {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\":(\\[.+?])");
        Matcher matcher = pattern.matcher(json);
        matcher.find();

        String[] elements = matcher.group(1)
                .replaceAll("\\[", "")
                .replaceAll("\\]", "")
                .split(",");

        List<Object> list = new ArrayList<>();

        for (String element : elements) {
            list.add(mapPrimitiveToWrapper(element, parameter));
        }
        setter.invoke(instance, list);
    }

    public void initializeMap(Class<?> fieldType, String sourceJson, String fieldName, Method setter, Object instance,
                              Class<?> classType)
            throws NoSuchFieldException, InvocationTargetException, InstantiationException, IllegalAccessException,
            NoSuchMethodException {
        if (isMap(fieldType)) {
            Pattern pattern = Pattern.compile("\"" + fieldName + "\":(\\{\\\".+?\\\":\\{.+?\\}\\})");
            Matcher matcher = pattern.matcher(sourceJson);
            matcher.find();
            String jsonForMap = matcher.group(1);

            ParameterizedType genericType = (ParameterizedType) classType.getDeclaredField(fieldName).getGenericType();
            List<Type> mapKeyValueTypes = Arrays.stream(genericType.getActualTypeArguments()).toList();

            Class<?> mapKeyType = (Class<?>) mapKeyValueTypes.get(0);
            Class<?> mapValueType = (Class<?>) mapKeyValueTypes.get(1);

            Map<Object, Object> mapForInstance = new HashMap<>();
            Map<String, String> keysValues = parseKeyAndValueForMapInObject(jsonForMap);

            for (Map.Entry<String, String> item : keysValues.entrySet()) {
                Object key = mapJsonToObject(item.getKey(), mapKeyType);
                Object value = mapJsonToObject(item.getValue(), mapValueType);
                mapForInstance.put(key, value);
            }
            setter.invoke(instance, mapForInstance);
        } else {
            initializeIntegratedClassObject(fieldType, sourceJson, fieldName, instance, setter);
        }
    }


    public void initializeListFieldThatObjects(String json, String fieldName, Method setter, Object instance,
                                               Class<?> parameter)
            throws InvocationTargetException, IllegalAccessException, NoSuchFieldException,
            InstantiationException, NoSuchMethodException {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\":(\\[.+?])");
        Matcher matcher = pattern.matcher(json);
        matcher.find();

        String[] elements = matcher.group(1)
                .replaceAll("\\[", "")
                .replaceAll("\\]", "")
                .split("\\}");

        List<Object> list = new ArrayList<>();

        for (String element : elements) {
            list.add(mapJsonToObject(element, parameter));
        }
        setter.invoke(instance, list);
    }
}
