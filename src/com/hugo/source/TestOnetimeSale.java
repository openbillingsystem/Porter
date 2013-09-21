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

public class TestOnetimeSale {
	static Logger logger = Logger.getLogger(TestOnetimeSale.class);
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

	public static void createOnetimeSale() throws FileNotFoundException,
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

		JSONObject onetimesale = new JSONObject();

		onetimesale.put("dateFormat", bean.getDateformat());
		onetimesale.put("locale", bean.getLocale());
		onetimesale.put("itemId", bean.getItemId());
		onetimesale.put("quantity", bean.getQuantity());
		onetimesale.put("chargeCode", bean.getChargeCode());
		onetimesale.put("totalPrice", bean.getTotalPrice());
		onetimesale.put("unitPrice", bean.getUnitPrice());
		onetimesale.put("discountId", bean.getDiscountId());
		onetimesale.put("saleDate", bean.getSaleDate());

		logger.info("------------" + onetimesale.toString());

		StringEntity se = new StringEntity(onetimesale.toString());
		Long clientid = (new Double(bean.getClientid())).longValue();
		HttpPost postRequest1 = new HttpPost(prop.getProperty(
				"OnetimeSalePostQuery").trim()
				+ clientid);

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
			String resourceId = Util.getStringFromJson("resourceIdentifier",output1);

			//Double invoiceAmount = Double.parseDouble(invoiceAmount1);
			logger.info(resourceId.toString());
		//	bean.setInvoiceAmount(invoiceAmount);
			bean.setResult("success");
			logger.info("Onetimesale is created");
			bean.setOnetimesaleResource(resourceId);
			
			JSONObject allocation = new JSONObject();

			
			allocation.put("itemMasterId", bean.getItemId());
			allocation.put("clientId",clientid);
			allocation.put("orderId",resourceId);
			allocation.put("serialNumber", bean.getSerailNumber());
			allocation.put("status","allocated");
			StringEntity se1 = new StringEntity(allocation.toString());
			
			
		HttpPost postRequestAllocation= new HttpPost(prop.getProperty("AllocationPostQuery").trim());
		
		postRequestAllocation.setHeader("Authorization", "Basic " + new String(encoded));
		postRequestAllocation.setHeader("Content-Type", "application/json");

		postRequestAllocation.addHeader("X-Mifos-Platform-TenantId", "test");
		postRequestAllocation.setEntity(se1);
			
			HttpResponse responseAllocation = httpClient.execute(postRequestAllocation);

			if (responseAllocation.getStatusLine().getStatusCode() != 200) {
				logger.error("Failed : HTTP error code : "
						+ responseAllocation.getStatusLine().getStatusCode());
				bean.setResult("Failure");
				return;
			}

			 br1 = new BufferedReader(new InputStreamReader(
					(responseAllocation.getEntity().getContent())));

		
			logger.info("Output from Server .... \n");
			while ((output1 = br1.readLine()) != null) {
				logger.info(output1);
			//	 resourceId = Util.getStringFromJson("commandId",output1);

				//Double invoiceAmount = Double.parseDouble(invoiceAmount1);
				logger.info(resourceId.toString());
			//	bean.setInvoiceAmount(invoiceAmount);
				bean.setResult("success");
				logger.info("hardware allocated");

		}

		httpClient.getConnectionManager().shutdown();
	}
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
				bean.setitemId(v.elementAt(3).toString());
				bean.setQuantity(v.elementAt(4).toString());
				bean.setSaleDate(v.elementAt(5).toString());
				bean.setChargeCode(v.elementAt(6).toString());
				bean.setTotalPrice(v.elementAt(7).toString());
				bean.setUnitPrice(v.elementAt(8).toString());
				bean.setDiscountId(v.elementAt(9).toString());
				bean.setSerailNumber(v.elementAt(10).toString());
				

					logger.info(bean.getClientid());
					createOnetimeSale();
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
	}

	@SuppressWarnings("static-access")
	public static void writeXLSXFile() throws IOException {

		InputStream excelFileToRead = new FileInputStream(file);
		XSSFWorkbook wb = new XSSFWorkbook(excelFileToRead);

		XSSFSheet sheet = wb.getSheetAt(0);

		XSSFRow row = sheet.getRow(i);
		XSSFCell cell;

		cell = row.getCell(11, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(11);
		if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING
				|| cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
			return;
		} else {
			cell.setCellValue(bean.getResourceId());

		}

		cell = row.getCell(12, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(12);
		if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING
				|| cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
			return;
		} else {

			cell.setCellValue(bean.getResult());

		}
		cell = row.getCell(13, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(13);
		if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING
				|| cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
			return;
		} else {

			cell.setCellValue(bean.getStartTime());

		}
		cell = row.getCell(14, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(14);
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
