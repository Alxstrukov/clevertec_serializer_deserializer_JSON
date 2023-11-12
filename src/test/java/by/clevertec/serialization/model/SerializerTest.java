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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SerializerTest {

    static PulseSensor sensor1;
    static PulseSensor sensor2;
    static PrimaryMeasuringDevice device1;
    static PrimaryMeasuringDevice device2;
    static List<Double> coefficients;
    static LiquidFlowMeter flowMeter1;
    static LiquidFlowMeter flowMeter2;
    static Map<Integer, LiquidFlowMeter> flowMeterMap;
    static WareHouse wareHouse;
    static ObjectMapper mapper;
    static String expected;

    @BeforeEach
    void setUp() throws JsonProcessingException {
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

        wareHouse = new WareHouse("Gomel", flowMeterMap);

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        expected = mapper.writeValueAsString(wareHouse);
    }


    @Test
    void mapObjectToJSONShouldReturnStringJson() {

        // given
        Serializer serializer = new Serializer();

        // when
        String actual = null;
        try {
            actual = serializer.mapObjectToJSON(wareHouse);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        // then
        assertEquals(expected, actual);
    }

    @Test
    void mapObjectToJSONShouldReturnNullPointerException() {

        // given
        Serializer serializer = new Serializer();

        // when, then
        assertThrows(NullPointerException.class, () -> serializer.mapObjectToJSON(null));
    }
}