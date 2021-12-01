import sql.Processor;

public class MainApp {
	public static void main(String[] args) {
		Person p = new Person(15, "Ilia");
		Processor.instanceToTable(p);
		Processor.instanceToTable(new Person(22, "Nalimov"));
	}
}
