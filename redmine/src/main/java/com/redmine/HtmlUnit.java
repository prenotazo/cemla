package com.redmine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public class HtmlUnit {

	private static final String MAIN_URL = "http://redmine.teracode.com";

	private static final String MAIN_FOLDER = "redmine";
	private static final String PROJECT_FOLDER = MAIN_FOLDER + "/" + "personnel";
	private static final String ISSUES_FOLDER = PROJECT_FOLDER + "/" + "issues";

	public static void main(final String[] args) throws Exception {
		// LOGIN
		final WebClient webClient = login(MAIN_URL + "/login", "ricardo.castiglione", "r1c@rd0.");

		createFolders();

		final List<String> issueNumbers = findIssueNumbers();

		// BACKUP PROJECT
		backupIssues(webClient, issueNumbers);

		// CLOSE ALL WINDOWS
		webClient.closeAllWindows();
	}

	private static List<String> findIssueNumbers() throws Exception {
		final File csv = new File(PROJECT_FOLDER + "/all_issues_export.xls");
		if (!csv.exists()) {
			throw new RuntimeException("The file " + csv.getCanonicalFile() + " doesn't exist.");
		}

		final List<String> issueNumbers = new ArrayList<String>();

		/** Creating Input Stream**/
		//InputStream myInput= ReadExcelFile.class.getResourceAsStream( fileName );
		final FileInputStream myInput = new FileInputStream(csv);

		/** Create a POIFSFileSystem object**/
		final POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

		/** Create a workbook using the File System**/
		final HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

		/** Get the first sheet from workbook**/
		final HSSFSheet mySheet = myWorkBook.getSheetAt(0);

		/** We now need something to iterate through the cells.**/
		final Iterator<Row> rowIter = mySheet.rowIterator();
		while(rowIter.hasNext()) {
			final HSSFRow myRow = (HSSFRow) rowIter.next();
			if (myRow.getRowNum() == 0) {
				continue;
			}
			final String issueNumber = String.valueOf(Double.valueOf(myRow.getCell(0).getNumericCellValue()).intValue());
			issueNumbers.add(issueNumber);
		}

		//		issueNumbers.clear();
		//		issueNumbers.add("87799");
		//		issueNumbers.add("87770");
		//		issueNumbers.add("87421");
		//		issueNumbers.add("88656");
		//		issueNumbers.add("61439");
		//		issueNumbers.add("88676");
		//		issueNumbers.add("88068");
		//		issueNumbers.add("87011");
		//		issueNumbers.add("84768");

		return issueNumbers;
	}

	private static void backupIssues(final WebClient webClient, final List<String> issueNumbers) throws Exception {
		final int total = issueNumbers.size();
		int counter = 0;
		for (final String issueNumber : issueNumbers) {
			System.out.println("Issue " + ++counter + " of " + total + " -> " + issueNumber);

			// Ticket
			final HtmlPage issuePage = webClient.getPage(MAIN_URL + "/issues/" + issueNumber);

			final File issuePageFile = new File(ISSUES_FOLDER + "/" + issueNumber + ".html");
			if (issuePageFile.exists()) {
				issuePageFile.delete();
			}
			issuePage.save(issuePageFile);

			// Attachments
			for (final HtmlElement div : issuePage.getElementsByTagName("div")) {
				if (!"attachments".equals(div.getAttribute("class"))) {
					continue;
				}

				// Attachments Div
				final HtmlDivision attachmentsDiv = (HtmlDivision) div;
				for (final HtmlElement p : attachmentsDiv.getElementsByTagName("p")) {
					for(final HtmlElement a : p.getElementsByTagName("a")) {
						if (!"icon icon-attachment".equals(a.getAttribute("class"))) {
							continue;
						}

						final String href = a.getAttribute("href");

						final File attachmentFile = new File(ISSUES_FOLDER + href);
						if (attachmentFile.exists()) {
							attachmentFile.delete();
						} else {
							attachmentFile.getParentFile().mkdirs();
						}
						final FileWriter attachmentFileWriter = new FileWriter(attachmentFile);
						Page attachmentPage = webClient.getPage(MAIN_URL + href);
						do {
							if (attachmentPage instanceof UnexpectedPage) {
								final InputStream inputStream = ((UnexpectedPage) attachmentPage).getInputStream();

								final FileOutputStream fileOutputStream = new FileOutputStream(attachmentFile);

								int read = 0;
								final byte[] bytes = new byte[1024];

								while ((read = inputStream.read(bytes)) != -1) {
									fileOutputStream.write(bytes, 0, read);
								}

								inputStream.close();
								fileOutputStream.flush();
								fileOutputStream.close();
								attachmentPage = null;
							} else if (attachmentPage instanceof HtmlPage) {
								attachmentPage = webClient.getPage(MAIN_URL + createDownloadPath(href));
							} else if (attachmentPage instanceof XmlPage) {
								attachmentFileWriter.write(((XmlPage) attachmentPage).getContent());
								attachmentPage = null;
							} else if (attachmentPage instanceof TextPage) {
								attachmentFileWriter.write(((TextPage) attachmentPage).getContent());
								attachmentPage = null;
							} else {
								throw new RuntimeException("Problem with attachment from ticket -> " + issueNumber);
							}
						} while (attachmentPage != null);

						attachmentFileWriter.flush();
						attachmentFileWriter.close();
					}
				}
			}

			// modify the attachments paths and issues paths
			final BufferedReader issuePageFileBufferedReader = new BufferedReader(new FileReader(issuePageFile));

			final File issuePageFile_ = new File(ISSUES_FOLDER + "/" + issueNumber + "_.html");
			if (issuePageFile_.exists()) {
				issuePageFile_.delete();
			}
			final PrintWriter issuePageFilePrintWriter = new PrintWriter(issuePageFile_);

			String line;
			while ((line = issuePageFileBufferedReader.readLine()) != null) {
				// attachment
				line = line.replaceAll("/attachments/", "attachments/");

				// issue
				final Pattern pattern = Pattern.compile("\"/issues/\\d*\"");
				final Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					final String issueUrl = matcher.group();
					final String issueFileName = issueUrl.replaceAll("/issues/", "").replaceAll("\"$", "").concat(".html\"");
					line = line.replaceAll(issueUrl, issueFileName);
				}

				issuePageFilePrintWriter.println(line);
			}

			issuePageFileBufferedReader.close();

			issuePageFilePrintWriter.flush();
			issuePageFilePrintWriter.close();

			// rename modified file
			final File issuePageFileOldName = new File(ISSUES_FOLDER + "/" + issueNumber + "_.html");
			final File issuePageFileNewName = new File(ISSUES_FOLDER + "/" + issueNumber + ".html");

			issuePageFileOldName.renameTo(issuePageFileNewName);
		}
	}

	private static String createDownloadPath(final String href) {
		final String[] split = href.split("/");
		final StringBuilder downloadPath = new StringBuilder("/");
		downloadPath.append(split[1]);
		downloadPath.append("/");
		downloadPath.append("download");
		downloadPath.append("/");
		downloadPath.append(split[2]);
		downloadPath.append("/");
		downloadPath.append(split[3]);

		return downloadPath.toString();
	}

	private static WebClient login(final String loginUrl, final String username, final String password) throws Exception {
		final WebClient webClient = new WebClient();

		// Get the first page
		final HtmlPage loginPage = webClient.getPage(loginUrl);

		// Get the form that we are dealing with and within that form,
		// find the submit button and the field that we want to change.
		HtmlForm form = null;
		for (final HtmlElement next : loginPage.getDocumentElement().getHtmlElementDescendants()) {
			if (next.getTagName().equals("form")) {
				form = (HtmlForm) next;
			}
		}

		if (form == null) {
			System.exit(0);
		}

		final HtmlTextInput usernameField = form.getInputByName("username");
		usernameField.setValueAttribute(username);
		final HtmlPasswordInput passwordField = form.getInputByName("password");
		passwordField.setValueAttribute(password);

		final HtmlSubmitInput button = form.getInputByName("login");
		button.click();

		return webClient;
	}

	private static void createFolders() {
		{
			final File dir = new File(MAIN_FOLDER);
			dir.mkdir();
		}

		{
			final File dir = new File(PROJECT_FOLDER);
			dir.mkdir();
		}

		{
			final File dir = new File(ISSUES_FOLDER);
			dir.mkdir();
		}
	}
}