import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class URLGenerator {

	public static final Integer MAX_RECORDS_PER_PAGE = 10;
	
	public static final List<String> ABC = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "Ñ", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
	public static final List<String> DAYS = Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31");
	public static final List<String> MONTHS = Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");
	public static List<String> YEARS = new ArrayList<String>();

	static {
		for (int i = 1882; i <= 1960; i++) {			
			YEARS.add((new Integer(i)).toString());
		}
	}
	
	public static void main(String[] args) {
		// Case -> 01/05/1923 c -> 604 resultados
		// Page 0
		// http://www.cemla.com/busqueda/buscador_action.php?Apellido=c&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923
		// Page 1
		// http://www.cemla.com/busqueda/buscador_action.php?pageNum_Recordset1=1&totalRows_Recordset1=604&Apellido=c&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923
		// Page 2
		// http://www.cemla.com/busqueda/buscador_action.php?pageNum_Recordset1=2&totalRows_Recordset1=604&Apellido=c&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923
		// Page Before Last
		// http://www.cemla.com/busqueda/buscador_action.php?pageNum_Recordset1=59&totalRows_Recordset1=604&Apellido=c&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923
		// Page Last
		// http://www.cemla.com/busqueda/buscador_action.php?pageNum_Recordset1=60&totalRows_Recordset1=604&Apellido=c&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923
	
		// Generate First Pages
		for (String year : YEARS) {
			for (String month : MONTHS) {
				for (String day : DAYS) {
					for (String lastNameInitial : ABC) {			
						String firstPage = generateFirstPage(lastNameInitial, day, month, year);
//						String fromRecord = "1";
						
						// do things: begin

						String totalRecords = "604";
//						String toRecord = Integer.valueOf(totalRecords) < MAX_RECORDS_PER_PAGE ? totalRecords : String.valueOf(MAX_RECORDS_PER_PAGE);
						
						// do things: end
						
						System.out.println(generateNPages(lastNameInitial, day, month, year, totalRecords));
					}
				}
			}
		}
	}

	// Case -> 01/05/1923 c -> 604 resultados
	// Page 0
	// http://www.cemla.com/busqueda/buscador_action.php?Apellido=c&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923
	public static String generateFirstPage(String lastNameInitial, String day, String month, String year) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("http://www.cemla.com/busqueda/buscador_action.php?Apellido=");
//		builder.append("C");
		builder.append(lastNameInitial);
		builder.append("&Nombre=&d-dia=");
//		builder.append("01");
		builder.append(day);
		builder.append("&d-mes=");
//		builder.append("05");
		builder.append(month);
		builder.append("&d-anio=");
//		builder.append("1923");
		builder.append(year);
		builder.append("&h-dia=");
//		builder.append("01");
		builder.append(day);
		builder.append("&h-mes=");
//		builder.append("05");
		builder.append(month);
		builder.append("&h-anio=");
//		builder.append("1923");
		builder.append(year);
		
		return builder.toString();
	}

	// Page 1
	// http://www.cemla.com/busqueda/buscador_action.php?pageNum_Recordset1=1&totalRows_Recordset1=604&Apellido=c&Nombre=&d-dia=01&d-mes=05&d-anio=1923&h-dia=01&h-mes=05&h-anio=1923
	public static String generateNPage(String lastNameInitial, String day, String month, String year, Integer pageNumber, String totalRecords) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("http://www.cemla.com/busqueda/buscador_action.php?pageNum_Recordset1=");
//		builder.append("1");
		builder.append(pageNumber);
		builder.append("&totalRows_Recordset1=");
//		builder.append("604");
		builder.append(totalRecords);
		builder.append("&Apellido=");
//		builder.append("c");
		builder.append(lastNameInitial);
		builder.append("&Nombre=&d-dia=");
//		builder.append("01");
		builder.append(day);
		builder.append("&d-mes=");
//		builder.append("05");
		builder.append(month);
		builder.append("&d-anio=");
//		builder.append("1923");
		builder.append(year);
		builder.append("&h-dia=");
//		builder.append("01");
		builder.append(day);
		builder.append("&h-mes=");
//		builder.append("05");
		builder.append(month);
		builder.append("&h-anio=");
//		builder.append("1923");
		builder.append(year);
		
		return builder.toString();
	}
	
	public static List<String> generateNPages(String lastNameInitial, String day, String month, String year, String totalRecords) {
		if (Integer.valueOf(totalRecords) < MAX_RECORDS_PER_PAGE) {
			return new ArrayList<String>();
		}		
		
		Integer amountOfPages = new Double(Math.ceil(Integer.valueOf(totalRecords) / MAX_RECORDS_PER_PAGE)).intValue(); 
		
		return null;
	}
}