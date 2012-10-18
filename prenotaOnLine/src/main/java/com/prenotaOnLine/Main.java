package com.prenotaOnLine;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class Main {

	public static void main(final String[] args) throws Exception {
		sendEmail();

		// LOGIN PAGE
		final WebClient webClient = login("https://prenotaonline.esteri.it/login.aspx?cidsede=100064&ReturnUrl=%2f", "ricardo.castiglione@gmail.com", "030386rdc");

		// HOME PAGE
		final HtmlPage prenotaOnLinePage = webClient.getPage("https://prenotaonline.esteri.it/");
		final HtmlElement prenotaOnLineMenuItem = prenotaOnLinePage.getElementById("ctl00_repFunzioni_ctl00_btnMenuItem");

		// LISTA DI SERVIZI PAGE
		final HtmlPage listaDiServiziPage = prenotaOnLineMenuItem.click();
		final HtmlElement cittadinanzaMenuItem = listaDiServiziPage.getElementById("ctl00_ContentPlaceHolder1_rpServizi_ctl01_btnNomeServizio");

		// CITTADINANZA PAGE
		final HtmlPage cittadinanzaPage = cittadinanzaMenuItem.click();
		final HtmlElement primoAppuntamentoMenuItem = cittadinanzaPage.getElementById("ctl00_ContentPlaceHolder1_rpServizi_ctl02_btnNomeServizio");

		// PRIMO APPUNTAMENTO PAGE
		final HtmlPage primoAppuntamentoPage = primoAppuntamentoMenuItem.click();
		final HtmlElement primoAppuntamentoConfermaButton = primoAppuntamentoPage.getElementById("ctl00_ContentPlaceHolder1_acc_datiAddizionali1_btnContinua");

		// FIRST MONTH PAGE
		final HtmlPage firstMonthPage = primoAppuntamentoConfermaButton.click();
		final HtmlElement firstMonthComponent = firstMonthPage.getElementById("calendar");
		if (hasAvailable(firstMonthComponent)) {
			System.out.println(firstMonthComponent.asXml());
		}

		// SECOND MONTH PAGE
		final HtmlElement secondMonthNextButton = firstMonthPage.getElementByName("ctl00$ContentPlaceHolder1$acc_Calendario1$myCalendario1$ctl03");
		final HtmlPage secondMonthPage = secondMonthNextButton.click();
		final HtmlElement secondMonthComponent = secondMonthPage.getElementById("calendar");
		if (hasAvailable(secondMonthComponent)) {
			System.out.println(secondMonthComponent.asXml());
		}

		// THIRD MONTH PAGE
		final HtmlElement thirdMonthNextButton = secondMonthPage.getElementByName("ctl00$ContentPlaceHolder1$acc_Calendario1$myCalendario1$ctl03");
		final HtmlPage thirdMonthPage = thirdMonthNextButton.click();
		final HtmlElement thirdMonthComponent = thirdMonthPage.getElementById("calendar");
		if (hasAvailable(thirdMonthComponent)) {
			System.out.println(thirdMonthComponent.asXml());
		}

		// CLOSE ALL WINDOWS
		webClient.closeAllWindows();
	}

	private static boolean hasAvailable(final HtmlElement monthComponent) {
		boolean hasAvailable = false;

		final Iterable<HtmlElement> childElements = monthComponent.getChildElements();

		// base
		if (!childElements.iterator().hasNext()) {
			if (isNumber(monthComponent.getTextContent())) {
				if (monthComponent.getAttribute("class").equals("otherMonthDay") ||
						monthComponent.getAttribute("class").equals("noSelectableDay") ||
						monthComponent.getAttribute("class").equals("calendarCellRed")) {
					return false;
				}

				return true;
			}

			return false;
		}

		// recursive
		for (final HtmlElement child : childElements) {
			hasAvailable = hasAvailable || hasAvailable(child);
		}

		return hasAvailable;
	}

	private static boolean isNumber(final String s) {
		try {
			new Integer(s);
		} catch (final NumberFormatException nfe) {
			return false;
		}

		return true;
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

		final HtmlTextInput usernameField = form.getInputByName("UserName");
		usernameField.setValueAttribute(username);
		final HtmlPasswordInput passwordField = form.getInputByName("Password");
		passwordField.setValueAttribute(password);

		final HtmlSubmitInput button = form.getInputByName("BtnConfermaL");
		button.click();

		return webClient;
	}

	private static void sendEmail() {
		try {
			final String host = "smtp.gmail.com";
			final String from = "brainfields@gmail.com";
			final String pass = "grupo403";
			final Properties props = System.getProperties();
			props.put("mail.smtp.starttls.enable", "true"); // added this line
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.user", from);
			props.put("mail.smtp.password", pass);
			props.put("mail.smtp.port", "587");
			props.put("mail.smtp.auth", "true");

			final String[] to = {"ricardo.castiglione@gmail.com"}; // added this line

			final Session session = Session.getDefaultInstance(props, null);
			final MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));

			final InternetAddress[] toAddress = new InternetAddress[to.length];

			// To get the array of addresses
			for( int i=0; i < to.length; i++ ) { // changed from a while loop
				toAddress[i] = new InternetAddress(to[i]);
			}

			for( int i=0; i < toAddress.length; i++) { // changed from a while loop
				message.addRecipient(Message.RecipientType.TO, toAddress[i]);
			}
			message.setSubject("TURNO CONSULADO -> " + new Date() + "!!!");
			message.setText("Welcome to JavaMail");
			final Transport transport = session.getTransport("smtp");
			transport.connect(host, from, pass);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
}