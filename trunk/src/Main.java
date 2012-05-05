import java.util.StringTokenizer;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;
import org.jdesktop.http.Response;
import org.jdesktop.http.Session;
import org.jdesktop.http.State;

public class Main {

	public static void main(final String[] args) {
		try {
			final Session session = new Session();

			// 0 resultados
//			final String url="http://www.cemla.com/busqueda/buscador_action.php?Apellido=c&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=30&h-mes=02&h-anio=1923";
			// 2 resultados
//			final String url="http://www.cemla.com/busqueda/buscador_action.php?Apellido=cru&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923";
			// 1 resultados
//			final String url="http://www.cemla.com/busqueda/buscador_action.php?Apellido=crudo&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923";
			// 8 resultados
//			final String url="http://www.cemla.com/busqueda/buscador_action.php?Apellido=V&Nombre=&d-dia=20&d-mes=06&d-anio=1938&h-dia=20&h-mes=06&h-anio=1938";
			// 4 resultados con algunos campos vacios -> ver este caso q no esta funcionando...
			final String url="http://www.cemla.com/busqueda/buscador_action.php?Apellido=ZYLBERMANN&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=30&h-mes=02&h-anio=1923";
			final Response res = session.get(url);

//			System.out.print(session.getState().toString());
			if (session.getState() == State.DONE) {
				final String xml = res.toString();
//				System.out.println(xml);

				final Parser parser = Parser.createParser(xml, null);

				for (final NodeIterator nodeIterator = parser.elements(); nodeIterator.hasMoreNodes();) {
					final Node node = nodeIterator.nextNode();

					if (!node.getText().startsWith("html")) {
						continue;
					}
					
					NodeList children = node.getChildren();
					if (children == null) {
						continue;
					}
					for (SimpleNodeIterator simpleNodeIterator = children.elements(); simpleNodeIterator.hasMoreNodes();) {
						Node nextNode = simpleNodeIterator.nextNode();
						
						if (!nextNode.getText().startsWith("body")) {
							continue;
						}
						
						NodeList children2 = nextNode.getChildren();
						if (children2 == null) {
							continue;
						}
						for (SimpleNodeIterator simpleNodeIterator2 = children2.elements(); simpleNodeIterator2.hasMoreNodes();) {
							Node nextNode2 = simpleNodeIterator2.nextNode();							
    
							if (!nextNode2.getText().startsWith("table")) {
								continue;
							}
							
							if (nextNode2.getText().contains("results-table")) {
								NodeList children3 = nextNode2.getChildren();
								if (children3 == null) {
									continue;
								}
								for (SimpleNodeIterator simpleNodeIterator3 = children3.elements(); simpleNodeIterator3.hasMoreNodes();) {
									Node nextNode3 = simpleNodeIterator3.nextNode();
									
									if (!nextNode3.getText().contains("tr valign='top' class='txt'")) {
										continue;
									}
									
									NodeList children4 = nextNode3.getChildren();
									if (children4 == null) {
										continue;
									}
									PassengerRecordField currentPassengerRecordField = PassengerRecordField.getByColumnPosition(1);
									for (SimpleNodeIterator simpleNodeIterator4 = children4.elements(); simpleNodeIterator4.hasMoreNodes();) {
										Node nextNode4 = simpleNodeIterator4.nextNode();
										
										if (!nextNode4.getText().contains("td")) {
											continue;
										}
										
										NodeList children5 = nextNode4.getChildren();
										if (children5 == null) {
											continue;
										}
										for (SimpleNodeIterator simpleNodeIterator5 = children5.elements(); simpleNodeIterator5.hasMoreNodes();) {
											Node nextNode5 = simpleNodeIterator5.nextNode();
											
											if (nextNode5.getText().contains("strong")) {
												continue;
											}
											
											switch (currentPassengerRecordField) {
												case SURNAME:												
													String surname = nextNode5.getText();
													System.out.println("surname = " + surname);
													currentPassengerRecordField = currentPassengerRecordField.getNext();
													break;
												case NAME:													
													String name = nextNode5.getText().replace(", ", "");												
													System.out.println("name = " + name);
													currentPassengerRecordField = currentPassengerRecordField.getNext();
													break;
												case AGE:
													String age = nextNode5.getText();
													System.out.println("age = " + age);
													currentPassengerRecordField = currentPassengerRecordField.getNext();													
													break;
												case CIVIL_STATUS:
													String civilStatus = nextNode5.getText();
													System.out.println("civilStatus = " + civilStatus);
													currentPassengerRecordField = currentPassengerRecordField.getNext();													
													break;
												case PROFESSION:
													String profession = nextNode5.getText();
													System.out.println("profession = " + profession);
													currentPassengerRecordField = currentPassengerRecordField.getNext();													
													break;
												case RELIGION:
													String religion = nextNode5.getText();
													System.out.println("religion = " + religion);
													currentPassengerRecordField = currentPassengerRecordField.getNext();													
													break;
												case NATIONALITY:
													String nationality = nextNode5.getText();
													System.out.println("nationality = " + nationality);
													currentPassengerRecordField = currentPassengerRecordField.getNext();													
													break;
												case SHIP:
													String ship = nextNode5.getText();
													System.out.println("ship = " + ship);
													currentPassengerRecordField = currentPassengerRecordField.getNext();													
													break;
												case DEPARTURE:
													String departure = nextNode5.getText();
													System.out.println("departure = " + departure);
													currentPassengerRecordField = currentPassengerRecordField.getNext();													
													break;
												case ARRIVAL_DATE:
												case ARRIVAL_PORT:
													String arrivalDateAndPort = nextNode5.getText();
													
													String arrivalDate = arrivalDateAndPort.substring(0, 10);
													String arrivalPort = arrivalDateAndPort.substring(13);
													
													System.out.println("arrivalDate = " + arrivalDate);
													System.out.println("arrivalPort = " + arrivalPort);
													currentPassengerRecordField = currentPassengerRecordField.getNext();													
													break;
												case PLACE_OF_BIRTH:
													String placeOfBirth = nextNode5.getText();
													System.out.println("placeOfBirth = " + placeOfBirth);
													currentPassengerRecordField = currentPassengerRecordField.getNext();													
													break;
											}
										}
									}
								}
							}

							if (nextNode2.getText().contains("navigation")) {
								NodeList children3 = nextNode2.getChildren();
								if (children3 == null) {
									continue;
								}
								for (SimpleNodeIterator simpleNodeIterator3 = children3.elements(); simpleNodeIterator3.hasMoreNodes();) {
									Node nextNode3 = simpleNodeIterator3.nextNode();
									
									if (!nextNode3.getText().contains("tr class=")) {
										continue;
									}
									
									NodeList children4 = nextNode3.getChildren();
									if (children4 == null) {
										continue;
									}
									for (SimpleNodeIterator simpleNodeIterator4 = children4.elements(); simpleNodeIterator4.hasMoreNodes();) {
										Node nextNode4 = simpleNodeIterator4.nextNode();
										
										if (!nextNode4.getText().contains("td")) {
											continue;
										}
										
										NodeList children5 = nextNode4.getChildren();
										if (children5 == null) {
											continue;
										}
										for (SimpleNodeIterator simpleNodeIterator5 = children5.elements(); simpleNodeIterator5.hasMoreNodes();) {
											Node nextNode5 = simpleNodeIterator5.nextNode();

											String footer = nextNode5.getText();
											
											StringTokenizer stringTokenizer = new StringTokenizer(footer, " ");
											
											stringTokenizer.nextToken();
											String fromRecord = stringTokenizer.nextToken();
											stringTokenizer.nextToken();
											String toRecord = stringTokenizer.nextToken();
											stringTokenizer.nextToken();
											String totalRecords = stringTokenizer.nextToken();
											
											System.out.println(fromRecord + " to " + toRecord + " of " + totalRecords);
										}
									}
								}
							}							
						}
					}
				}
			}
		} catch (final Exception e1) {
			e1.printStackTrace();
		}
	}
}