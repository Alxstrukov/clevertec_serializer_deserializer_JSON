package by.clevertec.serialization.interfaces;

import java.util.Collection;

public interface ClassTypeVerifiable {
    boolean isIntegratedClassObject(Object object);

    boolean isIntegratedClassObject(String packageName);

    boolean isLocalDateClassObject(Object object);

    boolean isCollectionComponentWrapperOrPrimitive(Collection object);

    boolean isWrapperOrPrimitive(Class<?> fieldType);

    boolean isCollection(Class<?> fieldType);

    boolean isArray(Class<?> fieldType);

    boolean isMap(Class<?> fieldType);
}
