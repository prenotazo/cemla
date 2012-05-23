package com.cemla;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
	public static final Integer MAX_RECORDS_PER_PAGE = 10;

	public static final List<String> ABC = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "Ñ", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
	public static final List<String> DAYS = Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31");
	public static final List<String> MONTHS = Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");
	public static List<String> YEARS = new ArrayList<String>();

	static {
		// for (int i = 1882; i <= 1960; i++) {
		// YEARS.add((new Integer(i)).toString());
		// }
		for (int i = 1918; i >= 1888; i--) {
			YEARS.add((new Integer(i)).toString());
		}
	}

	public static void main(final String[] args) {
		try {
			BigDecimal currentFirstUrl = BigDecimal.ZERO;
			final BigDecimal amountOfLetters = new BigDecimal(ABC.size());
			final BigDecimal amountOfDays = new BigDecimal(DAYS.size());
			final BigDecimal amountOfMonths = new BigDecimal(MONTHS.size());
			final BigDecimal amountOfYears = new BigDecimal(YEARS.size());
			final BigDecimal totalFirstUrls = amountOfLetters.multiply(amountOfDays).multiply(amountOfMonths).multiply(amountOfYears);

			BigDecimal currentNUrl = BigDecimal.ZERO;
			BigDecimal totalNUrls = BigDecimal.ZERO;

			final Session session = new Session();
			final MysqlConnect mysql = new MysqlConnect();

			mysql.cleanNotCompleted();

			for (final String year : YEARS) {
				for (final String month : MONTHS) {
					for (final String day : DAYS) {
						for (final String lastNameInitial : ABC) {
							String url = URLGenerator.generateFirstPageUrl(lastNameInitial, day, month, year);
							Boolean isFirstPageUrl = true;
							List<String> nPageUrls = null;
							Iterator<String> nPageUrlsIterator = null;
							boolean hasNext = false;
							String totalRecords = "0";
							Boolean alreadyCompleted = false;
							do {
								// 0 resultados
								// url =
								// "http://www.cemla.com/busqueda/buscador_action.php?Apellido=c&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=30&h-mes=02&h-anio=1923";
								// 2 resultados
								// final String
								// url="http://www.cemla.com/busqueda/buscador_action.php?Apellido=cru&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923";
								// 1 resultados
								// url =
								// "http://www.cemla.com/busqueda/buscador_action.php?Apellido=crudo&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923";
								// 8 resultados
								// url =
								// "http://www.cemla.com/busqueda/buscador_action.php?Apellido=V&Nombre=&d-dia=20&d-mes=06&d-anio=1938&h-dia=20&h-mes=06&h-anio=1938";
								// 4 resultados con algunos campos vacios
								// final String
								// url="http://www.cemla.com/busqueda/buscador_action.php?Apellido=ZYLBERMANN&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923";

								// with error
								// url =
								// "http://www.cemla.com/busqueda/buscador_action.php?pageNum_Recordset1=81&totalRows_Recordset1=810&Apellido=D&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923";
								// url =
								// "http://www.cemla.com/busqueda/buscador_action.php?pageNum_Recordset1=13&totalRows_Recordset1=184&Apellido=A&Nombre=&d-dia=02&d-mes=01&d-anio=1882&h-dia=02&h-mes=01&h-anio=1882";

								Response res = null;
								final String groupUrl = lastNameInitial + day + month + year;
								final String date = new Date().toString();
								if (!mysql.checkAlreadyCompleted(url)) {
									alreadyCompleted = false;
									Thread.sleep(10000);
									res = session.get(url);
									if (isFirstPageUrl) {
										currentFirstUrl = currentFirstUrl.add(BigDecimal.ONE);
										final BigDecimal percentageFirstPage = currentFirstUrl.multiply(new BigDecimal(100)).divide(totalFirstUrls, BigDecimal.ROUND_FLOOR);
										final String progressFirstPage = currentFirstUrl + " of " + totalFirstUrls + " (" + percentageFirstPage + "%)";
										System.out.println(date + " -> Processing First Page (" + groupUrl + ") -> " + progressFirstPage + ": " + url);
										isFirstPageUrl = false;
									} else {
										currentNUrl = currentNUrl.add(BigDecimal.ONE);
										final BigDecimal percentageNPage = !totalNUrls.equals(BigDecimal.ZERO) ? currentNUrl.multiply(new BigDecimal(100)).divide(totalNUrls, BigDecimal.ROUND_FLOOR) : BigDecimal.ZERO;
										final String progressNPage = currentNUrl + " of " + totalNUrls + " (" + percentageNPage + "%)";
										System.out.println(date + " -> Processing n Page (" + groupUrl + ") -> " + progressNPage + ": " + url);
									}
								} else {
									alreadyCompleted = true;
									if (isFirstPageUrl) {
										currentFirstUrl = currentFirstUrl.add(BigDecimal.ONE);
										final BigDecimal percentageFirstPage = currentFirstUrl.multiply(new BigDecimal(100)).divide(totalFirstUrls, BigDecimal.ROUND_FLOOR);
										final String progressFirstPage = currentFirstUrl + " of " + totalFirstUrls + " (" + percentageFirstPage + "%)";
										System.out.println(date + " -> Already Completed First Page (" + groupUrl + ") -> " + progressFirstPage + ": " + url);
										isFirstPageUrl = false;
									} else {
										currentNUrl = currentNUrl.add(BigDecimal.ONE);
										final BigDecimal percentageNPage = !totalNUrls.equals(BigDecimal.ZERO) ? currentNUrl.multiply(new BigDecimal(100)).divide(totalNUrls, BigDecimal.ROUND_FLOOR) : BigDecimal.ZERO;
										final String progressNPage = currentNUrl + " of " + totalNUrls + " (" + percentageNPage + "%)";
										System.out.println(date + " -> Already Completed n Page (" + groupUrl + ") -> " + progressNPage + ": " + url);
									}
								}

								// System.out.print(session.getState().toString());
								if ((res != null) && (session.getState() == State.DONE)) {
									final String xml = res.toString();
									// System.out.println(xml);

									final Parser parser = Parser.createParser(xml, null);

									for (final NodeIterator nodeIterator = parser.elements(); nodeIterator.hasMoreNodes();) {
										final Node node = nodeIterator.nextNode();

										if (!node.getText().startsWith("html")) {
											continue;
										}

										final NodeList children = node.getChildren();
										if (children == null) {
											continue;
										}
										for (final SimpleNodeIterator simpleNodeIterator = children.elements(); simpleNodeIterator.hasMoreNodes();) {
											final Node nextNode = simpleNodeIterator.nextNode();

											if (!nextNode.getText().startsWith("body")) {
												continue;
											}

											final NodeList children2 = nextNode.getChildren();
											if (children2 == null) {
												continue;
											}
											final List<PassengerRecord> passengerRecords = new ArrayList<PassengerRecord>();
											for (final SimpleNodeIterator simpleNodeIterator2 = children2.elements(); simpleNodeIterator2.hasMoreNodes();) {
												final Node nextNode2 = simpleNodeIterator2.nextNode();

												if (!nextNode2.getText().startsWith("table")) {
													continue;
												}

												if (nextNode2.getText().contains("results-table")) {
													final NodeList children3 = nextNode2.getChildren();
													if (children3 == null) {
														continue;
													}
													for (final SimpleNodeIterator simpleNodeIterator3 = children3.elements(); simpleNodeIterator3.hasMoreNodes();) {
														final Node nextNode3 = simpleNodeIterator3.nextNode();

														if (!nextNode3.getText().contains("tr valign='top' class='txt'")) {
															continue;
														}

														final NodeList children4 = nextNode3.getChildren();
														if (children4 == null) {
															continue;
														}
														PassengerRecordField currentPassengerRecordField = PassengerRecordField.getByColumnPosition(1);
														String surname = null;
														String name = null;
														String age = null;
														String civilStatus = null;
														String profession = null;
														String religion = null;
														String nationality = null;
														String ship = null;
														String departure = null;
														String arrivalDate = null;
														String arrivalPort = null;
														String placeOfBirth = null;
														for (final SimpleNodeIterator simpleNodeIterator4 = children4.elements(); simpleNodeIterator4.hasMoreNodes();) {
															final Node nextNode4 = simpleNodeIterator4.nextNode();

															if (!nextNode4.getText().contains("td")) {
																continue;
															}

															if (!currentPassengerRecordField.getColumnPosition().equals(1)) {
																currentPassengerRecordField = currentPassengerRecordField.getNext();
															}

															final NodeList children5 = nextNode4.getChildren();
															if (children5 == null) {
																continue;
															}
															for (final SimpleNodeIterator simpleNodeIterator5 = children5.elements(); simpleNodeIterator5.hasMoreNodes();) {
																final Node nextNode5 = simpleNodeIterator5.nextNode();

																if (nextNode5.getText().contains("strong")) {
																	continue;
																}

																switch (currentPassengerRecordField) {
																case SURNAME:
																	surname = trim(nextNode5.getText());
																	// System.out.println("surname = "
																	// +
																	// surname);
																	currentPassengerRecordField = currentPassengerRecordField.getNext();
																	break;
																case NAME:
																	name = trim(nextNode5.getText().replace(", ", ""));
																	// System.out.println("name = "
																	// + name);
																	break;
																case AGE:
																	age = trim(nextNode5.getText());
																	// System.out.println("age = "
																	// + age);
																	break;
																case CIVIL_STATUS:
																	civilStatus = trim(nextNode5.getText());
																	// System.out.println("civilStatus = "
																	// +
																	// civilStatus);
																	break;
																case PROFESSION:
																	profession = trim(nextNode5.getText());
																	// System.out.println("profession = "
																	// +
																	// profession);
																	break;
																case RELIGION:
																	religion = trim(nextNode5.getText());
																	// System.out.println("religion = "
																	// +
																	// religion);
																	break;
																case NATIONALITY:
																	nationality = trim(nextNode5.getText());
																	// System.out.println("nationality = "
																	// +
																	// nationality);
																	break;
																case SHIP:
																	ship = trim(nextNode5.getText());
																	// System.out.println("ship = "
																	// + ship);
																	break;
																case DEPARTURE:
																	departure = trim(nextNode5.getText());
																	// System.out.println("departure = "
																	// +
																	// departure);
																	break;
																case ARRIVAL_DATE:
																case ARRIVAL_PORT:
																	final String arrivalDateAndPort = trim(nextNode5.getText());

																	if ((arrivalDateAndPort != null) && (arrivalDateAndPort.length() > 12)) {
																		arrivalDate = arrivalDateAndPort.substring(0, 10);
																		arrivalPort = arrivalDateAndPort.substring(13);
																	} else {
																		arrivalDate = arrivalDateAndPort;
																		arrivalPort = arrivalDateAndPort;
																	}

																	// System.out.println("arrivalDate = "
																	// +
																	// arrivalDate);
																	// System.out.println("arrivalPort = "
																	// +
																	// arrivalPort);
																	break;
																case PLACE_OF_BIRTH:
																	placeOfBirth = trim(nextNode5.getText());
																	// System.out.println("placeOfBirth = "
																	// +
																	// placeOfBirth);
																	break;
																}
															}
														}
														passengerRecords.add(new PassengerRecord(surname, name, age, civilStatus, profession, religion, nationality, ship, departure, arrivalDate, arrivalPort, placeOfBirth));
														// System.out.println();
													}
												}

												if (nextNode2.getText().contains("navigation")) {
													final NodeList children3 = nextNode2.getChildren();
													if (children3 == null) {
														continue;
													}
													for (final SimpleNodeIterator simpleNodeIterator3 = children3.elements(); simpleNodeIterator3.hasMoreNodes();) {
														final Node nextNode3 = simpleNodeIterator3.nextNode();

														if (!nextNode3.getText().contains("tr class=")) {
															continue;
														}

														final NodeList children4 = nextNode3.getChildren();
														if (children4 == null) {
															continue;
														}
														for (final SimpleNodeIterator simpleNodeIterator4 = children4.elements(); simpleNodeIterator4.hasMoreNodes();) {
															final Node nextNode4 = simpleNodeIterator4.nextNode();

															if (!nextNode4.getText().contains("td")) {
																continue;
															}

															final NodeList children5 = nextNode4.getChildren();
															if (children5 == null) {
																continue;
															}
															for (final SimpleNodeIterator simpleNodeIterator5 = children5.elements(); simpleNodeIterator5.hasMoreNodes();) {
																final Node nextNode5 = simpleNodeIterator5.nextNode();

																final String footer = nextNode5.getText();

																final StringTokenizer stringTokenizer = new StringTokenizer(footer, " ");

																stringTokenizer.nextToken();
																final String fromRecord = stringTokenizer.nextToken();
																stringTokenizer.nextToken();
																final String toRecord = stringTokenizer.nextToken();
																stringTokenizer.nextToken();
																totalRecords = stringTokenizer.nextToken();

																// System.out.println(fromRecord
																// + " to " +
																// toRecord +
																// " of " +
																// totalRecords);
																mysql.getInsertUrl(url, Integer.valueOf(totalRecords), lastNameInitial, day, month, year);

																for (final PassengerRecord pr : passengerRecords) {
																	mysql.getInsertPassengerRecord(pr.getSurname(), pr.getName(), pr.getAge(), pr.getCivilStatus(), pr.getProfession(), pr.getReligion(), pr.getNationality(), pr.getShip(), pr.getDeparture(), pr.getArrivalDate(), pr.getArrivalPort(), pr.getPlaceOfBirth(), url, Integer.valueOf(totalRecords), lastNameInitial, day, month, year);
																}
															}
														}
													}
												}
											}
										}
									}

									if (!mysql.checkAlreadyProcessed(url)) {
										mysql.getInsertUrl(url, 0, lastNameInitial, day, month, year);
									}

									if ((nPageUrls == null) || !nPageUrls.contains(url)) {
										nPageUrls = URLGenerator.generateNPageUrls(lastNameInitial, day, month, year, totalRecords);
										currentNUrl = BigDecimal.ZERO;
										totalNUrls = new BigDecimal(nPageUrls.size());
										nPageUrlsIterator = nPageUrls.iterator();
									}
								}

								if (nPageUrlsIterator != null) {
									hasNext = nPageUrlsIterator.hasNext();
									if (hasNext) {
										url = nPageUrlsIterator.next();
									}
								}
							} while ((nPageUrlsIterator != null) && hasNext);

							if (!alreadyCompleted) {
								final String groupUrl = lastNameInitial + day + month + year;
								final Integer insertedRecords = mysql.getInsertedRecords(groupUrl);
								if (Integer.valueOf(totalRecords) > insertedRecords) {
									System.out.println("Changed " + groupUrl + " from " + totalRecords + " to " + insertedRecords + "");
									mysql.updateUrlTotalRecordsByGroupUrl(groupUrl, insertedRecords);
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

	public static String trim(final String s) {
		if (s == null) {
			return null;
		}

		return s.trim();
	}
}