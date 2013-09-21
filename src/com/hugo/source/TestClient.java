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

public class TestClient {
	static Logger logger = Logger.getLogger(TestClient.class);
	static Properties prop = new Properties();

	public static Double id;
	public static int i;
	public static int countno;
	public static int endno;
	public static Long planc;
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

	public static void createClient() throws FileNotFoundException, IOException {

		HttpClient httpClient = new DefaultHttpClient();
		httpClient = wrapClient(httpClient);
		String username = prop.getProperty("username");
		String password = prop.getProperty("password");
		String ashok = username.trim() + ":" + password.trim();

		// encoding byte array into base 64
		byte[] encoded = Base64.encodeBase64(ashok.getBytes());

		/*System.out.println("Original String: " + ashok);
		System.out.println("Base64 Encoded String : " + new String(encoded));*/

		JSONObject client = new JSONObject();
		client.put("officeId", bean.getOfficeId());
		client.put("firstname", bean.getFirstname());
		client.put("middlename", bean.getMiddlename());
		client.put("lastname", bean.getLastname());
		client.put("fullname", "");
		client.put("externalId", "");
		client.put("dateFormat", bean.getDateformat());
		client.put("locale", bean.getLocale());
		client.put("clientCategory", bean.getClientCategory());
		client.put("active", bean.getActive());
		client.put("activationDate", bean.getActivationDate());
		client.put("addressNo", bean.getAddressNo());
		client.put("street", bean.getStreet());
		client.put("city", bean.getCity());
		client.put("state", bean.getState());
		client.put("country", bean.getCountry());
		client.put("zipCode", bean.getZipCode());
		client.put("phone", bean.getPhone());
		client.put("email", bean.getEmail());

		/*System.out.println("------------" + client.toString());*/

		StringEntity se = new StringEntity(client.toString());

		HttpPost postRequest1 = new HttpPost(prop.getProperty("clientQuery")
				.trim());

		postRequest1.setHeader("Authorization", "Basic " + new String(encoded));
		postRequest1.setHeader("Content-Type", "application/json");

		postRequest1.addHeader("X-Mifos-Platform-TenantId", "test");
		postRequest1.setEntity(se);

		HttpResponse response1 = httpClient.execute(postRequest1);
		if (response1.getStatusLine().getStatusCode() != 200) {
			logger.error("Failed : HTTP error code : "
					+ response1.getStatusLine().getStatusCode());
			bean.setResult("failure");

			return;
		}
		BufferedReader br1 = new BufferedReader(new InputStreamReader(
				(response1.getEntity().getContent())));

		String output;
		String clientId = null;

		logger.info("Output from Server .... \n");
		while ((output = br1.readLine()) != null) {

			logger.info(output);

		clientId = Util.getStringFromJson("resourceIdentifier", output);

			logger.info(clientId);

		
			logger.info(output);
			logger.info("");
			logger.info("**********************************");
			logger.info("client is created");
			//logger.info("clientid is " + clientId);
			bean.setResult("success");
			bean.setClientid(clientId);
			logger.info("***********************************");

		}
		httpClient.getConnectionManager().shutdown();

	}

	@SuppressWarnings("rawtypes")
	public static void readClientFile(String fileName, int X, int Y)
			throws Exception {

		prop.load(new FileInputStream("Migrate.properties"));
		file = fileName;
		InputStream excelFileToRead = new FileInputStream(file);

		XSSFWorkbook wb = new XSSFWorkbook(excelFileToRead);

		XSSFSheet sheet = wb.getSheetAt(0);
		XSSFRow row;
		XSSFCell cell;
		countno = X;
		endno = Y;
		if (countno == 0) {
			countno = countno + 2;
		} else if (countno == 1) {
			countno = countno + 1;
		}
		
		Iterator rows = sheet.rowIterator();
		Vector<XSSFCell> v = new Vector<XSSFCell>();
		if (countno > 0) {
			countno = countno - 1;
		}
		while (rows.hasNext()) {
			row = (XSSFRow) rows.next();
			long ldate = System.currentTimeMillis();
			String date = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(
					ldate));
			i = row.getRowNum();

			if (i >= countno && i < endno) {
				Iterator cells = row.cellIterator();
				while (cells.hasNext()) {
					cell = (XSSFCell) cells.next();
                  
					  cell.setCellType(Cell.CELL_TYPE_STRING);
					//if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
						// System.out.print(cell.getStringCellValue() +
						// " ");
						v.add(cell);
					/*} else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
						// System.out.print(cell.getNumericCellValue() +
						// " ");
						v.add(cell);
					} else if (cell.getCellType() == XSSFCell.CELL_TYPE_BOOLEAN) {
						v.add(cell);
					} else if (cell.getCellType() == XSSFCell.CELL_TYPE_FORMULA) {
						v.add(cell);
					} else {
						
					}*/

				}
				logger.info(v);

				bean.setStartTime(date);
				bean.setDateformat(v.elementAt(1).toString());
				bean.setLocale(v.elementAt(2).toString());
				bean.setActivationDate(v.elementAt(3).toString());				
				bean.setFirstname(v.elementAt(4).toString());
				bean.setMiddlename(v.elementAt(5).toString());
				bean.setLastname(v.elementAt(6).toString());
				bean.setFullname(v.elementAt(7).toString());
				bean.setOfficeId(Double.valueOf(v.elementAt(8).toString()));
				bean.setExternalid(v.elementAt(9).toString());
				bean.setClientCategory(v.elementAt(10).toString());
				bean.setActive(v.elementAt(11).toString());
				bean.setAddressNo(v.elementAt(12).toString());
				bean.setStreet(v.elementAt(13).toString());
				bean.setCity(v.elementAt(14).toString());
				bean.setState(v.elementAt(15).toString());
				bean.setCountry(v.elementAt(16).toString());
				bean.setZipCode(v.elementAt(17).toString());
				bean.setPhone(v.elementAt(18).toString());
				bean.setEmail(v.elementAt(19).toString());
					createClient();
					//logger.info(bean.getClientid());
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

		cell = row.getCell(22, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(22);
		if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING
				|| cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
			return;
		} else {
			cell.setCellValue(bean.getStartTime());

		}

		cell = row.getCell(21, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(21);
		if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING
				|| cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
			return;
		} else {
                if(!(bean.getResult().equalsIgnoreCase("failure")))
			cell.setCellValue(bean.getClientid());

		}
		cell = row.getCell(20, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(20);
		if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING
				|| cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
			return;
		} else {

			cell.setCellValue(bean.getResult());

		}
		cell = row.getCell(23, row.CREATE_NULL_AS_BLANK);
		cell = row.getCell(23);
		if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING
				|| cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC
				|| cell.getCellType() == XSSFCell.CELL_TYPE_BOOLEAN) {
			return;
		} else {
			long ldate1 = System.currentTimeMillis();
			String date1 = new SimpleDateFormat("HH:mm:ss.SSS")
					.format(new Date(ldate1));
			bean.setLastTime(date1);
			cell.setCellValue(bean.getLastTime());

		}

		// write this workbook to an Outputstream.
		OutputStream fileOut = new FileOutputStream(file);
		wb.write(fileOut);
		fileOut.flush();
		fileOut.close();
		System.out.println(i+")  "+bean.getClientid()+" : "+bean.getResult());
		bean.setClientid(null);
		bean.setResult(null);
		bean.setLastTime(null);
		bean.setStartTime(null);

	}

	public static void exception() throws Exception {
		if (i >= countno && i < endno) {
			int a = i;
			a = a + 2;
			logger.info(a);
			try {
				readClientFile(file, a, endno);
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
