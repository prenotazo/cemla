import java.net.URL;
import java.net.URLConnection;

public class Main {

	public static void main(final String[] args) {
		try {
			final URL url = new URL("http://www.cemla.com/busqueda/buscador_action.php?Apellido=crudo&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923");

			final URLConnection openConnection = url.openConnection();

			final Object content = openConnection.getContent();

			System.out.println(content);
		} catch(final Exception e) {
			System.out.println(e);
		}
	}
}