package com.moac.android.opensecretsanta.activity;

public class ContactModeRowDetails {

    int contactMode;
    String contactDetail;

    public ContactModeRowDetails(int contactMode, String contactDetail) {
        super();
        this.contactMode = contactMode;
        this.contactDetail = contactDetail;
    }

    @Override
    public String toString() {
        switch(contactMode) {
            case ContactModes.NAME_ONLY_CONTACT_MODE:
                return "View draw result on this phone";
            case ContactModes.SMS_CONTACT_MODE:
                return "(SMS) " + contactDetail;
            case ContactModes.EMAIL_CONTACT_MODE:
                return "(Email) " + contactDetail;
            default:
                return "Unsupported Mode";
        }
    }

    @Override
    public boolean equals(Object other) {
        if(this == other) return true;

        if(!(other instanceof ContactModeRowDetails)) return false;

        ContactModeRowDetails that = (ContactModeRowDetails) other;

        return this.contactMode == that.contactMode
          && (null == this.contactDetail ? (this.contactDetail == that.contactDetail) : this.contactDetail.equals(that.contactDetail));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.contactMode;
        hash = 31 * hash + (null == this.contactDetail ? 0 : this.contactDetail.hashCode());
        return hash;
    }
}
