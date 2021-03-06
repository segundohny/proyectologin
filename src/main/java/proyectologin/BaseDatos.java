package proyectologin;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BaseDatos {

	private static String CADENA_CONEXION;
	private static String USUARIO;
	private static String CONTRASENIA_BD;

	static {

		// esta sección es como para iniciaizar la clase y se puede usar
		// cuando se necesite. las instrucciones que metamos aquí en la sección
		// static se ejecutan automáticamente cuando aparece por primera vez
		// el nombre de la clase en el codigo
		// TODO CARGAR LAS PROPERTIES

		try {
			Properties properties = new Properties();
			properties.load(new FileReader("src/main/resources/db.properties"));
			CADENA_CONEXION = properties.getProperty("cadenaconexion");
			USUARIO = properties.getProperty("usuariodb");
			CONTRASENIA_BD = properties.getProperty("passworddb");

			System.out.println("estamos en la sección static");

			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void registrarDriver() {
		try {
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BaseDatos() {
		// TODO Auto-generated constructor stub
		System.out.println("pasa por el constructor");
	}

	public Connection obtenerConexion() throws SQLException {
		Connection connection = null;

		connection = DriverManager.getConnection(CADENA_CONEXION, USUARIO, CONTRASENIA_BD);

		return connection;
	}

	public void liberarRecursos(Connection connection, Statement statement, ResultSet resultSet) {
		try {
			resultSet.close();
			statement.close();
			connection.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void liberarRecursos(Connection connection, Statement statement) {
		try {

			statement.close();
			connection.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * método que busca lo usuarios cuyo nombre empiece por la cadena recibida
	 * 
	 * @param nombrebuscado el inicio del patrón de búsqueda
	 * @return una lista vacía si no se recuperan resultados o la lista con los
	 *         usuarios coincidentes
	 */
	public List<Usuario> buscarUsuariosPorNombre(String nombrebuscado) {
		List<Usuario> lu = null;
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		Usuario usuario_aux = null;

		try {
			lu = new ArrayList<Usuario>();
			connection = this.obtenerConexion();
			ps = connection.prepareStatement(InstruccionesSQL.BUSCAR_USUARIO_POR_NOMBRE);
			ps.setString(1, "%" + nombrebuscado + "%");
			resultSet = ps.executeQuery();
			while (resultSet.next()) {
				// crear el usuario
				usuario_aux = new Usuario(resultSet);
				// add a la lista
				lu.add(usuario_aux);
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			this.liberarRecursos(connection, ps, resultSet);

		}

		return lu;
	}

	public List<Usuario> obtenerListaUsuarios() {
		List<Usuario> lu = null;
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		Usuario usuario_aux = null;

		try {
			lu = new ArrayList<Usuario>();
			connection = this.obtenerConexion();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(InstruccionesSQL.SELECCIONAR_TODOS_USUARIOS);
			while (resultSet.next()) {
				// crear el usuario
				usuario_aux = new Usuario(resultSet);
				// add a la lista
				lu.add(usuario_aux);
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			this.liberarRecursos(connection, statement, resultSet);

		}

		return lu;
	}

	public boolean insertarUsuario(Usuario u) {
		boolean insertado = false;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = this.obtenerConexion();
			preparedStatement = connection.prepareStatement(InstruccionesSQL.INSERTAR_USUARIOS);
			preparedStatement.setString(1, u.getNombre());
			preparedStatement.setString(2, u.getPwd());
			int nfilas = preparedStatement.executeUpdate();// siempre executeUpdate para INSERTAR; DELETE; o UPDATE
			System.out.println("NFILAS afectadas = " + nfilas);
//				if (nfilas!=0)
//				{
//					insertado = true;
//				}
			insertado = (nfilas != 0);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		} finally {
			liberarRecursos(connection, preparedStatement);

		}

		return insertado;
	}

	public boolean borrarUsuario(int id) {

		boolean borrado = false;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = this.obtenerConexion();
			preparedStatement = connection.prepareStatement(InstruccionesSQL.BORRAR_USUARIO_POR_ID);
			preparedStatement.setInt(1, id);
			int nfilas = preparedStatement.executeUpdate();// siempre executeUpdate para INSERTAR; DELETE; o UPDATE
			System.out.println("NFILAS afectadas = " + nfilas);
//				if (nfilas!=0)
//				{
//					insertado = true;
//				}
			borrado = (nfilas != 0);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		} finally {
			liberarRecursos(connection, preparedStatement);

		}

		return borrado;

	}

	/**
	 * Método que consulta el usuario de la base de datos
	 * 
	 * @param nombre el nombre buscado
	 * @param pwd    la password del usuario
	 * @return el usuario si existe o null si no lo encontró
	 * @throws SQLException
	 * 
	 */
	public Usuario login(String nombre, String pwd) throws SQLException {
		Usuario usuario = null;
		PreparedStatement ps = null;
		Connection connection = null;
		ResultSet rs = null;

		// tengo que hacer la consulta
		// 1 pillo conexión
		try {
			connection = this.obtenerConexion();
			ps = connection.prepareStatement(InstruccionesSQL.LOGIN_USUARIOS);
			ps.setString(1, nombre);
			ps.setString(2, pwd);
			rs = ps.executeQuery();
			if (rs.next()) {
				usuario = new Usuario(nombre, pwd);
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw e;
		} finally {
			liberarRecursos(connection, ps, rs);
		}

		return usuario;
	}

}
