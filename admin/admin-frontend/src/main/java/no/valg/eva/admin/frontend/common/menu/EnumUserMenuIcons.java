package no.valg.eva.admin.frontend.common.menu;

public enum EnumUserMenuIcons {

	PRELIMINARIES("eva-icon-signup"), VOTING("eva-icon-paper"), COUNTING("eva-icon-box"), SETTLEMENT("eva-icon-bars");
        
    private String icon;
    
    private EnumUserMenuIcons(String icon) {
        this.icon = icon;
    }
    
    public String getValue() {
        return icon;
    }
}
