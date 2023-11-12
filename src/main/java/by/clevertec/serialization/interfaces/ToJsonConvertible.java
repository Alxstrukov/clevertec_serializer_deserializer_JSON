package by.clevertec.serialization.interfaces;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public interface ToJsonConvertible {
    void convertCustomClassObjectToJson(String fieldName, StringBuilder stringJson, Field field, Object sourceObject)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;

    void convertIntegratedClassObjectToJson(String fieldName, StringBuilder stringJson, Field field,
                                            Object sourceObject) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;

    void convertNullToJson(String fieldName, StringBuilder stringJson, Field field, Object sourceObject)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;

    void convertWrapperOrPrimitiveToJson(Class<?> fieldType, String fieldName, StringBuilder stringJson, Field field,
                                         Object sourceObject) throws IllegalAccessException;

    void convertCollectionToJson(Class<?> fieldType,Field field, Object sourceObject, StringBuilder stringJson, String fieldName)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;

    void convertMapToJson(Class<?> fieldType,Field field, Object sourceObject, StringBuilder stringJson, String fieldName)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException;

    void convertArrayToJson(Class<?> fieldType, Field field, Object sourceObject, StringBuilder stringJson, String fieldName)
            throws IllegalAccessException;
}
