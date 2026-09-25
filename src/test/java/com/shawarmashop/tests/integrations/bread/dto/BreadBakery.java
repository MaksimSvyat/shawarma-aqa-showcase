package com.shawarmashop.tests.integrations.bread.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreadBakery {
    private String branch;

    @JsonAlias("master_chef")
    private String masterChef;
}
