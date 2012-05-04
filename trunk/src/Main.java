import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeIterator;
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

				System.out.println("XML Parsed!!!");
				//			final Parser parser = new Parser(url);
				final Parser parser = Parser.createParser(xml, null);

				for (final NodeIterator nodeIterator = parser.elements(); nodeIterator.hasMoreNodes();) {
					final Node node = nodeIterator.nextNode();

					System.out.println(node.getText());
				}
			}
		} catch (final Exception e1) {
			e1.printStackTrace();
		}
	}
}