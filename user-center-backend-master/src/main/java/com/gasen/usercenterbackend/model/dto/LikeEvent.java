package com.gasen.usercenterbackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeEvent implements Serializable {

    @JsonProperty("type")
    private Integer type;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("likes")
    private Integer likes;
}