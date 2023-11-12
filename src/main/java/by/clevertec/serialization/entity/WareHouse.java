package by.clevertec.serialization.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class WareHouse {
    String location;
    Map<Integer, LiquidFlowMeter> flowMeterMap;
}
