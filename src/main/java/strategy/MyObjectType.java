package strategy;

public enum MyObjectType{
	
    SHIP_0("myShip0"),
    
    SHIP_1("myShip1"),

    SHIP_ENEMY("enemy"), 

    ISLAND("island"),
    
    TORPEDO("torpedo");
  
    private final String value;
    MyObjectType(String v) {
        value = v;
    }
    public String value() {
        return value;
    }
    public static MyObjectType fromValue(String v) {
    	if(v.equals("0")){
    		return SHIP_0;
    	}
    	if(v.equals("1")){
    		return SHIP_1;
    	}
        for (MyObjectType c: MyObjectType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Value not found: " + v);
    }
}
