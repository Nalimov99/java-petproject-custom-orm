package sql;

import sql.annotations.AutoIncrement;
import sql.annotations.Column;
import sql.annotations.ColumnGetter;
import sql.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Processor {
	private static Connection connection;
	private static Statement statement;
	private static PreparedStatement preparedStatement;

	private static void connect() throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:main.db");
			statement = connection.createStatement();
		} catch (ClassNotFoundException | SQLException e) {
			throw new SQLException("Unable to connect");
		}
	}

	private static void disconnect() {
		try {
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void createTable(Class<?> clazz) {
		if (!clazz.isAnnotationPresent(Table.class)) {
			throw new RuntimeException("@Table annotations does not exist");
		}

		if (clazz.getDeclaredFields().length == 0) {
			throw new RuntimeException("Class does not have fields");
		}

		ArrayList<Field> annotatedFields = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Column.class)) {
				annotatedFields.add(field);
			}
		}

		if (annotatedFields.size() == 0) {
			throw new RuntimeException("@Column annotations does not exist");
		}

		StringJoiner stringJoinerSql = new StringJoiner(
				", ",
				"CREATE TABLE IF NOT EXISTS " + clazz.getAnnotation(Table.class).title() + " (",
				");"
		);
		for (Field field : annotatedFields) {
			if (field.isAnnotationPresent(AutoIncrement.class)) {
				stringJoinerSql.add(
								field.getName()
								+ " "
								+ field.getAnnotation(Column.class).type().getType()
								+ " PRIMARY KEY AUTOINCREMENT"
				);
				continue;
			}
			stringJoinerSql.add(field.getName() + " " + field.getAnnotation(Column.class).type().getType());
		}

		try {
			connect();
			statement.executeUpdate(stringJoinerSql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			disconnect();
		}
	}

	public static void instanceToTable(Object obj) {
		Class<?> clazz = obj.getClass();
		if (!clazz.isAnnotationPresent(Table.class)) {
			throw new RuntimeException("@Table does not present");
		}

		if (clazz.getDeclaredFields().length == 0) {
			throw new RuntimeException("Class does not have fields");
		}

		var annotatedMethods = Arrays.stream(clazz.getDeclaredMethods())
				.filter(value -> value.isAnnotationPresent(ColumnGetter.class))
				.collect(Collectors.toList());

		if (annotatedMethods.size() == 0) {
			throw new RuntimeException("@ColumnGetter does not present");
		}

		final String query = String.join(", ", Collections.nCopies(annotatedMethods.size(), "?"));
		final String names = annotatedMethods
				.stream()
				.map(method -> method.getAnnotation(ColumnGetter.class).title())
				.collect(Collectors.joining(", "));
		StringBuilder stringBuilder = new StringBuilder("INSERT INTO ")
				.append(clazz.getAnnotation(Table.class).title())
				.append(" (")
				.append(names)
				.append(") VALUES ")
				.append("(")
				.append(query)
				.append(");");

		try {
			connect();
			preparedStatement = connection.prepareStatement(stringBuilder.toString());
			for (int i = 1; i < annotatedMethods.size() + 1; i++) {
				preparedStatement.setObject(i, annotatedMethods.get(i - 1).invoke(obj));
			}
			preparedStatement.executeUpdate();
		} catch (SQLException | InvocationTargetException | IllegalAccessException e) {
			e.printStackTrace();
		} finally {
			disconnect();
		}
	}
}
