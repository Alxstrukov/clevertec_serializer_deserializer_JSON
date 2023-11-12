package by.clevertec.serialization.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class PrimaryMeasuringDevice {
    int conditionalPassage;
    int nominalPressure;
    String type;
    Double price;
    LocalDate createdDate;
}
