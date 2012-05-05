
public enum PassengerRecordField {
	SURNAME(1),
	NAME(2),
	AGE(3),
	CIVIL_STATUS(4),
	PROFESSION(5),
	RELIGION(6),
	NATIONALITY(7),
	SHIP(8),
	DEPARTURE(9),
	ARRIVAL_DATE(10),
	ARRIVAL_PORT(10),
	PLACE_OF_BIRTH(11);
	
	private Integer columnPosition;
	
	private PassengerRecordField(Integer columnPosition) {
		this.columnPosition = columnPosition;
	}

	public static PassengerRecordField getByColumnPosition(Integer columnPosition) {
		for (PassengerRecordField v : values()) {
			if (v.getColumnPosition().equals(columnPosition)) {
				return v;
			}
		}
		
		return null;
	}
	
	public PassengerRecordField getNext() {
		PassengerRecordField prf = getByColumnPosition(getColumnPosition()+1);
		
		if (prf == null) {
			return getByColumnPosition(1);
		}
		
		return prf;
	}
	
	public Integer getColumnPosition() {return columnPosition;}
}