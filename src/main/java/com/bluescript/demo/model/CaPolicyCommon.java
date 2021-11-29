package com.bluescript.demo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import java.util.*;
import java.sql.Date;
import java.sql.Timestamp;

import org.springframework.stereotype.Component;

@Data
@Component

public class CaPolicyCommon {
    private Date caIssueDate;
    private Date caExpiryDate;
    private Timestamp caLastchanged;
    private long caBrokerid;
    private String caBrokersref;
    private int caPayment;

    public String toFixedWidthString() {
        return caIssueDate.toString() + caExpiryDate + caLastchanged + caBrokerid + caBrokersref + caPayment;
    }

}