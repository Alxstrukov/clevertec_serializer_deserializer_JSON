package by.clevertec.serialization.model;

import by.clevertec.serialization.entity.LiquidFlowMeter;
import by.clevertec.serialization.entity.PrimaryMeasuringDevice;
import by.clevertec.serialization.entity.PulseSensor;
import by.clevertec.serialization.entity.WareHouse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeserializerTest {
    static PulseSensor sensor1;
    static PulseSensor sensor2;
    static PrimaryMeasuringDevice device1;
    static PrimaryMeasuringDevice device2;
    static List<Double> coefficients;
    static LiquidFlowMeter flowMeter1;
    static LiquidFlowMeter flowMeter2;
    static Map<Integer, LiquidFlowMeter> flowMeterMap;
    static WareHouse expected;
    static String json;
    static Serializer serializer;

    @BeforeEach
    void setUp() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        sensor1 = new PulseSensor("Magnetic", 60.0);
        sensor2 = new PulseSensor("Magnetic induction", 70.0);

        device1 = new PrimaryMeasuringDevice(32,
                250,
                "TA",
                2000.5, LocalDate.of(2023, 10, 18));
        device2 = new PrimaryMeasuringDevice(50,
                63,
                "TA",
                3870.88, LocalDate.of(2023, 10, 18));

        coefficients = List.of(1.56, 7.89, 5.45, 3.43, 2.77);

        flowMeter1 = new LiquidFlowMeter(UUID.randomUUID(), device1, sensor1, coefficients);
        flowMeter2 = new LiquidFlowMeter(UUID.randomUUID(), device2, sensor2, coefficients);

        flowMeterMap = Map.of(1, flowMeter1, 2, flowMeter2);

        expected = new WareHouse("Gomel", flowMeterMap);

        serializer = new Serializer();
        json = serializer.mapObjectToJSON(expected);
    }

    @Test
    void mapJsonToObjectShouldReturnObject() throws NoSuchFieldException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {

        // when
        Deserializer deserializer = new Deserializer();
        WareHouse actual = deserializer.mapJsonToObject(json, WareHouse.class);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapJsonToObjectShouldReturnNoSuchMethodException() {

        // given
        Deserializer deserializer = new Deserializer();

        // when, then
        assertThrows(NoSuchMethodException.class, () -> deserializer.mapJsonToObject(json, String.class));
    }

    @Test
    void mapJsonToObjectShouldReturnIllegalStateException() {

        // given
        Deserializer deserializer = new Deserializer();

        // when, then
        assertThrows(IllegalStateException.class, () -> deserializer.mapJsonToObject("json", WareHouse.class));
    }

    @Test
    void mapJsonToObjectShouldEqualsObjectFromJackson() throws NoSuchFieldException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException, JsonProcessingException {

        // given
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        WareHouse expected = mapper.readValue(json, WareHouse.class);

        // when
        Deserializer deserializer = new Deserializer();
        WareHouse actual = deserializer.mapJsonToObject(json, WareHouse.class);

        // then
        assertEquals(expected, actual);
    }
}