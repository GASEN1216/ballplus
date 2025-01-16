package com.gasen.usercenterbackend.model.respond;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class indexEvent implements Serializable {
    private Long id;
    private String avatar;
    private String name;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String location;
    private Integer participants;
    private Integer totalParticipants;
    private String remarks;
}
