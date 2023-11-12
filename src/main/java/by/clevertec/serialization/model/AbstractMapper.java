package by.clevertec.serialization.model;

import by.clevertec.serialization.interfaces.ClassTypeVerifiable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static by.clevertec.serialization.utils.ClassTypeName.BOOLEAN;
import static by.clevertec.serialization.utils.ClassTypeName.BYTE;
import static by.clevertec.serialization.utils.ClassTypeName.BYTE_W;
import static by.clevertec.serialization.utils.ClassTypeName.CHAR;
import static by.clevertec.serialization.utils.ClassTypeName.DOUBLE;
import static by.clevertec.serialization.utils.ClassTypeName.DOUBLE_W;
import static by.clevertec.serialization.utils.ClassTypeName.FLOAT;
import static by.clevertec.serialization.utils.ClassTypeName.FLOAT_W;
import static by.clevertec.serialization.utils.ClassTypeName.INT;
import static by.clevertec.serialization.utils.ClassTypeName.INT_W;
import static by.clevertec.serialization.utils.ClassTypeName.LONG;
import static by.clevertec.serialization.utils.ClassTypeName.LONG_W;
import static by.clevertec.serialization.utils.ClassTypeName.SHORT;
import static by.clevertec.serialization.utils.ClassTypeName.SHORT_W;


public abstract class AbstractMapper implements ClassTypeVerifiable {
    @Override
    public boolean isIntegratedClassObject(String packageName) {
        return (packageName.matches("^java\\..+") || packageName.matches("^javax\\..+"));
    }

    @Override
    public boolean isIntegratedClassObject(Object object) {
        String packageName = object.getClass().getPackageName();
        return (packageName.matches("^java\\..+") || packageName.matches("^javax\\..+"));
    }

    @Override
    public boolean isLocalDateClassObject(Object object) {
        String packageName = object.getClass().getPackageName();
        return (packageName.matches("^java.time"));
    }

    @Override
    public boolean isCollectionComponentWrapperOrPrimitive(Collection object) {
        return object.stream().allMatch(e -> isWrapperOrPrimitive(e.getClass()));
    }

    @Override
    public boolean isWrapperOrPrimitive(Class<?> fieldType) {
        return (fieldType.isPrimitive()
                || (fieldType.equals(Integer.class))
                || (fieldType.equals(Byte.class))
                || (fieldType.equals(Short.class))
                || (fieldType.equals(Float.class))
                || (fieldType.equals(Double.class))
                || (fieldType.equals(Boolean.class))
                || (fieldType.equals(Character.class))
                || (fieldType.equals(BigDecimal.class)));
    }

    @Override
    public boolean isCollection(Class<?> fieldType) {
        return ((Collection.class.isAssignableFrom(fieldType)));
    }

    @Override
    public boolean isArray(Class<?> fieldType) {
        return (fieldType.isArray());
    }

    @Override
    public boolean isMap(Class<?> fieldType) {
        return ((Map.class.isAssignableFrom(fieldType)));
    }

    protected static String castPrimitiveArrayToString(Class<?> fieldType, Object object) {
        String mappedArray = "[]";
        switch (fieldType.getComponentType().toString()) {
            case CHAR -> mappedArray = Arrays.toString((char[]) object);
            case INT -> mappedArray = Arrays.toString((int[]) object);
            case BYTE -> mappedArray = Arrays.toString((byte[]) object);
            case SHORT -> mappedArray = Arrays.toString((short[]) object);
            case FLOAT -> mappedArray = Arrays.toString((float[]) object);
            case DOUBLE -> mappedArray = Arrays.toString((double[]) object);
            case LONG -> mappedArray = Arrays.toString((long[]) object);
            case BOOLEAN -> mappedArray = Arrays.toString((boolean[]) object);
        }
        return mappedArray.replaceAll("\\s", "");
    }

    public <T extends Number> T mapPrimitiveToWrapper(String primitive, Class<?> fieldType) {
        String type = fieldType.toString();
        if (type.equals(INT) || type.equals(INT_W)) return (T) Integer.valueOf(primitive);
        if (type.equals(BYTE) || type.equals(BYTE_W)) return (T) Byte.valueOf(primitive);
        if (type.equals(SHORT) || type.equals(SHORT_W)) return (T) Short.valueOf(primitive);
        if (type.equals(FLOAT) || type.equals(FLOAT_W)) return (T) Float.valueOf(primitive);
        if (type.equals(LONG) || type.equals(LONG_W)) return (T) Long.valueOf(primitive);
        if (type.equals(DOUBLE) || type.equals(DOUBLE_W)) return (T) (T) Double.valueOf(primitive);
        return (T) new BigDecimal(primitive);
    }

    protected String getProvidedJson(String sourceJson, String fieldName) {
        Pattern pattern = Pattern.compile("(\"" + fieldName + ".+)");
        Matcher matcher = pattern.matcher(sourceJson);
        matcher.find();
        return matcher.group(1);
    }

    protected String trimJson(String sourceJson, String fieldName) {
        Pattern pattern = Pattern.compile("(\"" + fieldName + "\":(.+))");
        Matcher matcher = pattern.matcher(sourceJson);
        matcher.find();
        return matcher.group(2);
    }

    protected String getNumberString(String json, String fieldName) {
        Pattern pattern = Pattern.compile("(\"" + fieldName + "\"):(\\d+\\.?\\d*)");
        Matcher matcher = pattern.matcher(json);
        matcher.find();
        return matcher.group(2);
    }

    protected String getSymbolString(String json, String fieldName) {
        Pattern pattern = Pattern.compile("(\"" + fieldName + "\"):(\\w+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) return matcher.group(2);
        return null;
    }

    protected String[] getLocalDateValue(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\":\\[(\\d{3,4},\\d{2},\\d{2})\\]");
        Matcher matcher = pattern.matcher(json);
        matcher.find();

        String[] elements = matcher.group(1).split(",");

        return elements;
    }

    protected String getStringValue(String json, String fieldName) {
        Pattern patternForString = Pattern.compile("(\"" + fieldName + "\"):\\\"([^\\\"]*)\\\"");
        Matcher matcher = patternForString.matcher(json);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    protected String getUuidValue(String json, String fieldName) {
        Pattern patternForString = Pattern.compile("(\"" + fieldName + "\"):(\".{36}\")");
        Matcher matcher = patternForString.matcher(json);
        if (matcher.find()) {
            String result = matcher.group(2);
            return result.substring(1, result.length() - 1);
        }
        return null;
    }
}
