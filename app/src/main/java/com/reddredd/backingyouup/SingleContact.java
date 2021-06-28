package com.reddredd.backingyouup;

public class SingleContact {

    private String recy_contact_name;
    private String recy_contact_phone;

    public String getRecy_contact_name() {
        return recy_contact_name;
    }

    public String getRecy_contact_phone() {
        return recy_contact_phone;
    }

    SingleContact(String recy_contact_name, String recy_contact_phone)
    {
        this.recy_contact_name = recy_contact_name;
        this.recy_contact_phone = recy_contact_phone;
    }
}
