import sql.annotations.AutoIncrement;
import sql.annotations.Column;
import sql.SqlTypeEnum;
import sql.annotations.ColumnGetter;
import sql.annotations.Table;

@Table(title = "person")
public class Person {
	@Column(type = SqlTypeEnum.INTEGER)
	@AutoIncrement()
	private int id;
	@Column(type = SqlTypeEnum.INTEGER)
	private int age;
	@Column(type = SqlTypeEnum.TEXT)
	private String name;

	public Person(int age, String name) {
		this.age = age;
		this.name = name;
	}

	@ColumnGetter(title = "age")
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@ColumnGetter(title = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
