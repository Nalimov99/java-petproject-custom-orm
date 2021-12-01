package sql;

public enum SqlTypeEnum {
	INTEGER("INTEGER"), TEXT("TEXT");

	private String type;
	SqlTypeEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
