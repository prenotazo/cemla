import org.jdesktop.http.Response;
import org.jdesktop.http.Session;
import org.jdesktop.http.State;

public class Main {

	public static void main(final String[] args) {
		try {
			final Session session = new Session();

			final String url="http://www.cemla.com/busqueda/buscador_action.php?Apellido=crudo&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923";
			final Response res = session.get(url);

			System.out.print(session.getState().toString());
			if (session.getState() == State.DONE) {
				final String xml = res.toString();
				System.out.println(xml);
			}
		} catch (final Exception e1) {
			e1.printStackTrace();
		}
	}
}