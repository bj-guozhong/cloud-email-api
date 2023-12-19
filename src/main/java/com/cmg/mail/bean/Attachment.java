package com.cmg.mail.bean;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Attachment {

    public Attachment(String attName, String attType) {
        this.attName = attName;
        this.attType = attType;
    }

    private String attName;
    private String attType;
}
