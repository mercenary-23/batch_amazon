package com.example.amazon.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Sale {
    private final int id;
    private final String orderId;
    private final String status;
    private final String fulfilment;
    private final String salesChannel;
    private final String shipServiceLevel;
    private final String style;
    private final String sku;
    private final String category;
    private final String size;
    private final String aSin;
}
