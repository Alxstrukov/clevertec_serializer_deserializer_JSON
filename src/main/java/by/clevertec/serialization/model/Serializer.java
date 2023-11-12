package by.clevertec.serialization.model;

import by.clevertec.serialization.interfaces.ToJsonConvertible;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class Serializer extends AbstractMapper implements ToJsonConvertible {

    public String mapObjectToJSON(Object sourceObject) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        if (isIntegratedClassObject(sourceObject)) {
            return String.valueOf(sourceObject);
        }

        StringBuilder stringJson = new StringBuilder("{");

        Class<?> objectClass = sourceObject.getClass();

        Field[] fields = objectClass.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Class<?> fieldType = fields[i].getType();
            String fieldName = fields[i].getName();

            fields[i].setAccessible(true);

            mapFieldToJson(fieldType, fieldName, stringJson, fields[i], sourceObject);

            if (i == (fields.length - 1)) stringJson.deleteCharAt(stringJson.length() - 1);
        }
        stringJson.append("}");
        return stringJson.toString();
    }

    private void mapFieldToJson(Class<?> fieldType, String fieldName, StringBuilder stringJson, Field field,
                                Object sourceObject) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (isWrapperOrPrimitive(fieldType)) {
            convertWrapperOrPrimitiveToJson(fieldType, fieldName, stringJson, field, sourceObject);
        } else {
            convertCollectionToJson(fieldType,field, sourceObject, stringJson, fieldName);
        }
    }


    public void convertCustomClassObjectToJson(String fieldName, StringBuilder stringJson, Field field, Object sourceObject)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        stringJson.append("\"")
                .append(fieldName)
                .append("\":")
                .append(mapObjectToJSON(field.get(sourceObject)))
                .append(",");
    }

    public void convertIntegratedClassObjectToJson(String fieldName, StringBuilder stringJson, Field field,
                                                   Object sourceObject)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (isIntegratedClassObject(field.get(sourceObject))) {
            if (isLocalDateClassObject(field.get(sourceObject))) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy,MM,dd");
                String dateOfFormat = ((LocalDate) field.get(sourceObject)).format(formatter);
                String[] dateElements = dateOfFormat.split(",");
                stringJson.append("\"")
                        .append(fieldName)
                        .append("\":")
                        .append(Arrays.toString(dateElements).replaceAll(" ", ""))
                        .append(",");
            } else {
                stringJson.append("\"")
                        .append(fieldName)
                        .append("\":")
                        .append("\"")
                        .append(field.get(sourceObject))
                        .append("\"")
                        .append(",");
            }
        } else {
            convertCustomClassObjectToJson(fieldName, stringJson, field, sourceObject);
        }
    }

    public void convertNullToJson(String fieldName, StringBuilder stringJson, Field field, Object sourceObject)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (field.get(sourceObject) == null) {
            stringJson.append("\"")
                    .append(fieldName)
                    .append("\":")
                    .append(field.get(sourceObject))
                    .append(",");
        } else {
            convertIntegratedClassObjectToJson(fieldName, stringJson, field, sourceObject);
        }
    }

    public void convertWrapperOrPrimitiveToJson(Class<?> fieldType, String fieldName, StringBuilder stringJson,
                                                Field field, Object sourceObject) throws IllegalAccessException {
        if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
            stringJson.append("\"")
                    .append(fieldName)
                    .append("\":\"")
                    .append(field.get(sourceObject))
                    .append("\",");
        } else {
            stringJson.append("\"")
                    .append(fieldName)
                    .append("\":")
                    .append(field.get(sourceObject))
                    .append(",");
        }
    }

    public void convertCollectionToJson(Class<?> fieldType, Field field, Object sourceObject, StringBuilder stringJson, String fieldName)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        if (isCollection(fieldType)) {
            Collection list = (Collection) field.get(sourceObject);
            stringJson.append("\"")
                    .append(fieldName)
                    .append("\":[");
            if (isCollectionComponentWrapperOrPrimitive(list)) {
                list.forEach(e -> stringJson.append(e).append(','));
                stringJson.deleteCharAt(stringJson.length() - 1).append("],");
            } else {
                list.forEach(e -> {
                    try {
                        stringJson.append(mapObjectToJSON(e)).append(',');
                    } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                        ex.printStackTrace();
                    }
                });
                stringJson.deleteCharAt(stringJson.length() - 1).append("],");
            }
        } else {
            convertMapToJson(fieldType,field, sourceObject, stringJson, fieldName);
        }

    }

    public void convertMapToJson(Class<?> fieldType, Field field, Object sourceObject, StringBuilder stringJson, String fieldName)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        if (isMap(fieldType)) {
            Map map = (Map) field.get(sourceObject);
            stringJson.append("\"")
                    .append(fieldName)
                    .append("\":{");

            Object[] keyObjects = map.keySet().toArray();
            for (Object key : keyObjects) {
                if (isIntegratedClassObject(map.get(key))) {
                    stringJson.append('\"').append(key).append('\"')
                            .append(":\"")
                            .append(mapObjectToJSON(map.get(key)))
                            .append("\",");
                } else {
                    stringJson.append('\"').append(key).append('\"')
                            .append(":")
                            .append(mapObjectToJSON(map.get(key)))
                            .append(",");
                }
            }
            stringJson.deleteCharAt(stringJson.length() - 1).append("},");
        } else {
            convertArrayToJson(fieldType, field, sourceObject, stringJson, fieldName);
        }
    }

    public void convertArrayToJson(Class<?> fieldType, Field field, Object sourceObject, StringBuilder stringJson, String fieldName)
            throws IllegalAccessException {

        if (isArray(fieldType)) {
            String primitiveArray = castPrimitiveArrayToString(fieldType, field.get(sourceObject));
            stringJson.append("\"")
                    .append(fieldName)
                    .append("\":")
                    .append(primitiveArray)
                    .append(',');
        } else {
            try {
                convertNullToJson(fieldName, stringJson, field, sourceObject);
            } catch (InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

    }
}
