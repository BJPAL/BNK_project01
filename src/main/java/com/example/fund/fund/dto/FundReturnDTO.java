package com.example.fund.fund.dto;

import java.math.BigDecimal;

public interface FundReturnDTO {
    BigDecimal getMonth1Return();
    BigDecimal getMonth3Return();
    BigDecimal getMonth6Return();
    BigDecimal getMonth12Return();
}
