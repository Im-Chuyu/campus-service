package com.twilight.campus.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeHeroConfigVO {
    private String title;
    private String content;
    private String background;
    private Boolean canUpdate;
}
