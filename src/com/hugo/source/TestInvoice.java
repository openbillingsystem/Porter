package com.hugo.source;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.hugo.util.Bean;
import com.hugo.util.Util;

public class TestInvoice {
	static Logger logger = Logger.getLogger(TestInvoice.class);
	static Properties prop = new Properties();

	public static Long id;
	public static int i;
	public static int countno;
	public static int endno;
	public static String file;
	public static Long planc;
	static Bean bean = new Bean();

	public static HttpClient wrapClient(HttpClient base) {

		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				@SuppressWarnings("unused")
				public void checkClientTrusted(X509Certificate[] xcs,
						String string) throws CertificateException {
				}

				@SuppressWarnings("unused")
				public void checkServerTrusted(X509Certificate[] xcs,
						String string) throws CertificateException {
				}

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 8443));
			return new DefaultHttpClient(ccm, base.getParams());
		} catch (Exception ex) {
			return null;
		}
	}

	public static void createInvoice() throws FileNotFoundException,
			IOException {

		HttpClient httpClient = new DefaultHttpClient();

		httpClient = wrapClient(httpClient);

		String username = prop.getProperty("username");
		String password = prop.getProperty("password");
		String ashok = username.trim() + ":" + password.trim();

		// encoding byte array into base 64
		byte[] encoded = Base64.encodeBase64(ashok.getBytes());

		/*
		 * System.out.println("Original String: " + ashok);
		 * System.out.println("Base64 Encoded String : " + new String(encoded));
		 */

		JSONObject invoice = new JSONObject();

		invoice.put("dateFormat", bean.getDateformat());
		invoice.put("locale", bean.getLocale());
		invoice.put("systemDate", bean.getSystemDate());

		logger.info("------------" + invoice.toString());

		StringEntity se = new StringEntity(invoice.toString());
		Long clienti = (new Double(bean.getClientid())).longValue();
		HttpPost postRequest1 = new HttpPost(prop.getProperty(
				"InvoicePostQuery").trim()
				+ clienti);

		postRequest1.setHeader("Authorization", "Basic " + new String(encoded));
		postRequest1.setHeader("Content-Type", "application/json");

		postRequest1.addHeader("X-Mifos-Platform-TenantId", "test");
		postRequest1.setEntity(se);

		HttpResponse response1 = httpClient.execute(postRequest1);

		if (response1.getStatusLine().getStatusCode() != 200) {
			logger.error("Failed : HTTP error code : "
					+ response1.getStatusLine().getStatusCode());
			bean.setResult("Failure");
			return;
		}

		BufferedReader br1 = new BufferedReader(new InputStreamReader(
				(response1.getEntity().getContent())));

		String output1;
		logger.info("Output from Server .... \n");
		while ((output1 = br1.readLine()) != null) {
			logger.info(output1);
			String invoiceAmount1 = Util.getStringFromJson("resourceIdentifier",
					output1);

			Double invoiceAmount = Double.parseDouble(invoiceAmount1);
			logger.info(invoiceAmount);
			bean.setInvoiceAmount(invoiceAmount);
			bean.setResult("success");
			logger.info("invoice is created");

		}

		httpClient.getConnectionManager().shutdown();
	}

	@SuppressWarnings("rawtypes")
	public static void readInvoiceFile(String fileName, int X, int Y)
			throws Exception {
		prop.load(new FileInputStream("Migrate.properties"));
		file = fileName;
		InputStream excelFileToRead = new FileInputStream(file);

		XSSFWorkbook wb = new XSSFWorkbook(excelFileToRead);

		XSSFSheet sheet = wb.getSheetAt(0);
		XSSFRow row;
		XSSFCell cell;
		endno = Y;
		countno = X;
		if (countno == 0) {
			countno = countno + 2;
		} else if (countno == 1) {
			countno = countno + 1;
		}
		// System.out.println("Excel Row No is: " + countno);
		Iterator rows = sheet.rowIterator();
		Vector<XSSFCell> v = new Vector<XSSFCell>();
		if (countno > 0) {
			countno = countno - 1;
		}
		while (rows.hasNext()) {
			row = (XSSFRow) rows.next();
			long ldate = System.currentTimeMillis();
			String time = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(
					ldate));
			bean.setStartTime(time);
			i = row.getRowNum();
			logger.info(i);

			if (i >= countno && i < endno) {

				Iterator cells = row.cellIterator();
				while (cells.hasNext()) {
					cell = (XSSFCell) cells.next();
	                  
					  cell.setCellType(Cell.CELL_TYPE_STRING);
					
						v.add(cell);
					
				}
			    logger.info(v);
				Double d = Double.valueOf((v.elementAt(0).toString()));
				// Long l = (new Double(d).longValue());
				bean.setClientid(d.toString());
				bean.setDateformat(v.elementAt(1).toString());
				bean.setLocale(v.elementAt(2).toString());
				bean.setSystemDate(v.elementAt(3).toString());

				System.out.println();
				XSSFCell cell4 = row.getCell(4);
				XSSFCell cell5 = row.getCell(5);
				XSSFCell cell6 = row.getCell(6);
				XSSFCell cell7 = row.getCell(7);
				if (cell4 == null && cell5 == null && cell6 == null
						&& cell7 == null) {

					logger.info(bean.getClientid());
					createInvoice();
					writeXLSXFile();

				} else {
					System.out
							.println("output cells are not null in excel sheet. please null it");
				}
				v.removeAllElements();
				System.out.println();

			} else if (i < countno) {
				continue;

			} else {
				exception();
				break;
			}
		}
	}

	@SuppressWarnings("static-access")
	public static void writeXLSXFile() throws IOException {

		InputStream excelFileToRead = new FileInputStream(file);
		XSSFWorkbook wb = new XSSFWorkbook(excelFileToRead);

		XSSFSheet sheet = wb.getSheetAt(0);

		XSSFRow row = sheet.getRow(i);
		XSSFCell cell;

		cell = row.getCell(4, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(4);
		if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING
				|| cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
			return;
		} else {
			if(!bean.getResult().equalsIgnoreCase("failure"))
			cell.setCellValue(bean.getInvoiceAmount().toString());

		}

		cell = row.getCell(5, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(5);
		if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING
				|| cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
			return;
		} else {

			cell.setCellValue(bean.getResult());

		}
		cell = row.getCell(6, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(6);
		if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING
				|| cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
			return;
		} else {

			cell.setCellValue(bean.getStartTime());

		}
		cell = row.getCell(7, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(7);
		if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING
				|| cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
			return;
		} else {
			long ldate1 = System.currentTimeMillis();
			String time = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(
					ldate1));
			bean.setLastTime(time);
			cell.setCellValue(bean.getLastTime());

		}

		// write this workbook to an Outputstream.
		OutputStream fileOut = new FileOutputStream(file);
		wb.write(fileOut);
		fileOut.flush();
		fileOut.close();
		System.out.println(i + ")  " + bean.getClientid() + " : "
				+ bean.getResult());
		bean.setStartTime(null);
		bean.setResult(null);
		bean.setInvoiceAmount(null);
		bean.setLastTime(null);

	}

	public static void exception() throws Exception {
		if (i >= countno && i < endno) {
			int y = i;
			y = y + 2;
			logger.info(y);
			try {
				readInvoiceFile(file, y, endno);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				exception();
				e.printStackTrace();

			}
		} else {
			logger.info("records are over");
			return;
		}
	}

}
