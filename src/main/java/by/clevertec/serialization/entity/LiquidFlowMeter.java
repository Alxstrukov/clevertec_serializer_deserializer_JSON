package by.clevertec.serialization.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class LiquidFlowMeter {
    UUID flowMeterId;
    PrimaryMeasuringDevice device;
    PulseSensor sensor;
    List<Double> coefficients;
}
