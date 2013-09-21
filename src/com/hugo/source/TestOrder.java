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
import org.apache.http.client.methods.HttpGet;
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

public class TestOrder {

	static Logger logger = Logger.getLogger(TestOrder.class);
	static Properties prop = new Properties();
	public static Long planc;
	public static int i;
	public static int countno;
	public static int endno;
	public static String file;
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

	public static void createOrder() throws FileNotFoundException, IOException {

		//Long contract_period = null;
		// Long planid = null;
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

		/*HttpGet getRequest = new HttpGet(prop.getProperty("planGetQuery")
				.trim());

		getRequest.setHeader("Authorization", "Basic " + new String(encoded));
		getRequest.setHeader("Content-Type", "application/json");
		getRequest.addHeader("X-Mifos-Platform-TenantId", "test");

		HttpResponse response = httpClient.execute(getRequest);
		if (response.getStatusLine().getStatusCode() != 200) {
			logger.error("Failed : HTTP error code : "
					+ response.getStatusLine().getStatusCode());

			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(response.getEntity().getContent())));

		String output;
		String plancode = bean.getPlan();
		String plancode1 = null;

		while ((output = br.readLine()) != null) {

			plancode1 = Util.getStringFromJsonArray("id", output,
					plancode);
			planc =Long.valueOf(plancode);//Long.valueOf(plancode1);
			logger.info(output);
			logger.info(" ");
			logger.info("***********************************");

			logger.info(plancode1);

		}
*/
		//String contractperiod = bean.getContractPeriod();

	//	if (contractperiod.equalsIgnoreCase("perpectual")) {
			//contract_period = Long.valueOf(1);
		/*//} else if (contractperiod.equalsIgnoreCase("one week")) {
			contract_period = Long.valueOf(5);
		} else if (contractperiod.equalsIgnoreCase("bi-week")) {
			contract_period = Long.valueOf(6);
		} else if (contractperiod.equalsIgnoreCase("one month")) {
			contract_period = Long.valueOf(7);
		} else if (contractperiod.equalsIgnoreCase("bi-month")) {
			contract_period = Long.valueOf(8);
		} else if (contractperiod.equalsIgnoreCase("one quter")) {
			contract_period = Long.valueOf(9);
		}*/

		JSONObject order = new JSONObject();
		order.put("planCode", bean.getPlan());
		order.put("dateFormat", bean.getDateformat());
		order.put("locale", bean.getLocale());
		order.put("billAlign", bean.getBillingcycle());
		order.put("paytermCode", bean.getBillFrequency());
		order.put("start_date", bean.getStartDate());
		order.put("contractPeriod",bean.getContractPeriod());

		System.out.println("------------" + order.toString());

		StringEntity se = new StringEntity(order.toString());
		Long clienti = (new Double(bean.getClientid())).longValue();
		HttpPost postRequest1 = new HttpPost(prop.getProperty("OrderPostQuery")
				.trim() + clienti);

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
        String orderId;
		logger.info("Output from Server .... \n");
		while ((output1 = br1.readLine()) != null) {
			orderId= Util.getStringFromJson("resourceIdentifier", output1);
			bean.setOrderId(Double.valueOf(orderId));
			logger.info(output1);
			logger.info("");
			logger.info("**********************************");
			logger.info(" new order is created");
			bean.setResult("success");
			logger.info("***********************************");

		}

		httpClient.getConnectionManager().shutdown();
	}

	@SuppressWarnings("rawtypes")
	public static void readOrderFile(String fileName, int X, int Y)
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

			if (i >= countno && i < endno) {
				Iterator cells = row.cellIterator();
				while (cells.hasNext()) {
					cell = (XSSFCell) cells.next();
	                  
					  cell.setCellType(Cell.CELL_TYPE_STRING);
					
						v.add(cell);
					
				}
				logger.info(v);

				bean.setClientid(v.elementAt(0).toString());
				bean.setDateformat(v.elementAt(1).toString());
				bean.setLocale(v.elementAt(2).toString());
				bean.setPlan(v.elementAt(3).toString());
				bean.setStartDate(v.elementAt(4).toString());
				bean.setContractPeriod(v.elementAt(5).toString());
				bean.setBillFrequency(v.elementAt(6).toString());
				bean.setBillingcycle(v.elementAt(7).toString());

					createOrder();
					writeXLSXFile();
				
				v.removeAllElements();

				System.out.println();
			} else if (i < countno) {
				continue;
			} else {
				exception();
				break;
			}
		}

	};

	@SuppressWarnings("static-access")
	public static void writeXLSXFile() throws IOException {

		InputStream excelFileToRead = new FileInputStream(file);
		XSSFWorkbook wb = new XSSFWorkbook(excelFileToRead);

		XSSFSheet sheet = wb.getSheetAt(0);
		XSSFCell cell;

		XSSFRow row = sheet.getRow(i);

		cell = row.getCell(8, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(8);
			cell.setCellValue(bean.getResult());
		cell = row.getCell(9, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(9);
			cell.setCellValue(bean.getOrderId());	
		cell = row.getCell(10, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(10);
			cell.setCellValue(bean.getStartTime());	
		cell = row.getCell(11, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(11);
			long ldate1 = System.currentTimeMillis();
			String date1 = new SimpleDateFormat("HH:mm:ss.SSS")
					.format(new Date(ldate1));
			bean.setLastTime(date1);
			cell.setCellValue(bean.getLastTime());
	
		OutputStream fileOut = new FileOutputStream(file);

		// write this workbook to an Outputstream.
		wb.write(fileOut);
		fileOut.flush();
		fileOut.close();
		System.out.println(i + ")  " + bean.getClientid() + " : "
				+ bean.getResult());
		bean.setResult(null);
		bean.setStartTime(null);
		bean.setLastTime(null);
	}

	public static void exception() throws Exception {
		if (i >= countno && i < endno) {
			int y = i;
			y = y + 2;
			logger.info(y);
			try {
				readOrderFile(file, y, endno);
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
