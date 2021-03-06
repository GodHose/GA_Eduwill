package net.eduwill.intern;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.model.ColumnHeader;
import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.google.api.services.analyticsreporting.v4.model.DateRangeValues;
import com.google.api.services.analyticsreporting.v4.model.Dimension;
import com.google.api.services.analyticsreporting.v4.model.DimensionFilter;
import com.google.api.services.analyticsreporting.v4.model.DimensionFilterClause;
import com.google.api.services.analyticsreporting.v4.model.GetReportsRequest;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Metric;
import com.google.api.services.analyticsreporting.v4.model.MetricHeaderEntry;
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;

import ga.common.DailyInformVO;
import ga.api.domain.GoodsVO;
import ga.common.EventVO;

public class ApiRunnerTest {
	
	//Authorization Information for Google Analytics
	private final String APPLICATION_NAME = "Eduwill_Internship_GA_Project";
	private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance(); // GsonFactory.getDefaultInstance()
	private final String KEY_FILE_LOCATION = "client_secrets_edw.json"; // /resources/secret_key 에 들어가는 인증 정보
	private final String VIEW_ID = "110007282"; // View ID 는 ga-dev-tools.appsport.com/account-explorer/

	@Test
	public void getDailyData() {
		ArrayList<DailyInformVO> result = new ArrayList<DailyInformVO>();
		
		String startDate = null;
		String endDate = null;
	
		try {
			//서비스를 초기화 시키고
			AnalyticsReporting service = initializeAnalyticsReporting();
				
			//쿼리를 실행 시켜서
			GetReportsResponse response = getDailyReport(service, startDate, endDate);
			
			//결과값을 파싱하여 뿌린다
			result = printDailyResponse(response);

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	   * 애널리틱스 보고서 API V4 서비스 객체를 초기화 시키는 메소드.
	   *
	   * @return An authorized Analytics Reporting API V4 service object.
	   * @throws IOException
	   * @throws GeneralSecurityException
	   
	   */
	
	private AnalyticsReporting initializeAnalyticsReporting() throws GeneralSecurityException, IOException {
		
		Resource resource = new ClassPathResource("/secret_key/client_secrets_edw.json"); 
		FileInputStream fileInputStream = new FileInputStream(resource.getFile());
		
		//HTTP 통신을 초기화 시키고 인증 파일(.json)을 통해서 서비스 객체를 구성하여 반환한다.
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		GoogleCredential credential = GoogleCredential
				.fromStream(fileInputStream)
				.createScoped(AnalyticsReportingScopes.all());
		
		// Construct the Analytics Reporting service object.
		return new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
	}	
	
	/**
	   * 애널리틱스 보고서 API V4 를 위한 쿼리를 Send 하는 메소드.
	   *
	   * @param service An authorized Analytics Reporting API V4 service object.
	   * @return GetReportResponse The Analytics Reporting API V4 response.
	   * @throws IOException
	*/
	private GetReportsResponse getDailyReport(AnalyticsReporting service, String startDate, String endDate) throws IOException {

		// 조사할 기간의 범위를 설정한다
		DateRange dateRange = new DateRange();
		if(startDate != null)dateRange.setStartDate(startDate);
		else dateRange.setStartDate("yesterday");
		if(endDate != null)dateRange.setEndDate(endDate);
		else dateRange.setEndDate("yesterday");
		

		/*
		 * 측정 항목과 측정기준의 객체를 만들고 내용물을 설정한다 내용물에 대한 쿼리 레퍼런스는 아래의 링크를 참고하자
		 * https://developers.google.com/analytics/devguides/reporting/core/dimsmets
		 */

		// 상품 페이지 보고서-----
		Metric pageviews = new Metric().setExpression("ga:pageviews").setAlias("pageviews");

		Metric uniqueviews = new Metric().setExpression("ga:uniquePageviews").setAlias("uniquePageviews");

		Metric sessions = new Metric().setExpression("ga:sessions").setAlias("sessions");

		Metric entrances = new Metric().setExpression("ga:entrances").setAlias("entrances");

		Metric bounces = new Metric().setExpression("ga:bounces").setAlias("bounces");

		// 디멘션 설정
		Dimension pageTitle = new Dimension().setName("ga:pagePath");
		Dimension pageLevel1 = new Dimension().setName("ga:pagePathLevel1");
		Dimension date = new Dimension().setName("ga:date");
		
		// 측정 기준 필터 적용~
		
		DimensionFilter tcodeFilter = new DimensionFilter().setDimensionName("ga:pagePath") // {{pagePath}} 에
				.setExpressions(Arrays.asList(".*[\\?|&]tcode=.*"));
		
		DimensionFilter teacherFilter = new DimensionFilter().setDimensionName("ga:pagePath") // {{pagePath}} 에
				.setExpressions(Arrays.asList("teacher"));
		
		DimensionFilter nBookFilter = new DimensionFilter().setDimensionName("ga:pagePath") // {{pagePath}} 에
				.setNot(true)
				.setExpressions(Arrays.asList("book"));
		
		DimensionFilterClause dFilterClause = new DimensionFilterClause()
				.setFilters(Arrays.asList(tcodeFilter, teacherFilter, nBookFilter))
				.setOperator("AND");
		
		// 위의 항목과 기준을 구글로 Request 하기 위한 객체를 만든다
		ReportRequest request = new ReportRequest().setViewId(VIEW_ID).setDateRanges(Arrays.asList(dateRange))
				.setMetrics(Arrays.asList(pageviews, uniqueviews, sessions, entrances, bounces))
				.setDimensions(Arrays.asList(pageTitle, date, pageLevel1)).setDimensionFilterClauses(Arrays.asList(dFilterClause));

		// 리스트에 모두 싣습니다~
		ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
		requests.add(request);

		// 아마 Request 한 것에 대한 결과를 Receive 하는 객체를 만드는 듯 하다
		GetReportsRequest getReport = new GetReportsRequest().setReportRequests(requests);

		// 상기의 리스트를 GA 서버로 Request 하여 결과 값을 Response 하는 메소드(Execute)
		GetReportsResponse response = service.reports().batchGet(getReport).execute();

		// 결과를 반환
		return response;
	}

	/**
	   * 애널리틱스로부터 response 한 데이터를 파싱하고 출력하는 메소드.
	   *
	   * @param response An Analytics Reporting API V4 response.
	   */
	private ArrayList<DailyInformVO> printDailyResponse(GetReportsResponse response) {
		  
		//하나의 response 에는 다수의 report 가 포함되어 있다.
		//List를 foreach를 통해 각 객체마다 접근한다.
		
		ArrayList<DailyInformVO> list = new ArrayList<DailyInformVO>();
		
		for (Report report: response.getReports()) {			
						
			//하나의 report 에는 하나의 header 밖에 없다.
			ColumnHeader header = report.getColumnHeader();			
			
			//헤더와 측정 기준, 항목 사이에 하나의 분류가 더 있는 모양이다.
			List<String> dimensionHeaders = header.getDimensions();
			List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
			List<ReportRow> rows = report.getData().getRows();
			
			int sumpv = 0;
			
			if (rows == null) {
				System.out.println("No data found for " + VIEW_ID);
				return null;
			}
			
			System.out.println("Success to find for " + VIEW_ID);
			
			for (ReportRow row: rows) {
				
				System.out.println("====================================");
				
				DailyInformVO entity = new DailyInformVO();
				String code = null;
				
				List<String> dimensions = row.getDimensions();
				List<DateRangeValues> metrics = row.getMetrics();
				
				for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
					if(dimensionHeaders.get(i).equals("ga:date")) {
						entity.setuDate(dimensions.get(i));
					}
					if(dimensionHeaders.get(i).equals("ga:pagePath")) {
						entity.setPagePath(dimensions.get(i));
						code = parseCodeInURL(dimensions.get(i));
						if(code != null)entity.setPageCode(code);
						
					}
					
				}
				
				if(code == null)continue;
				System.out.println("pageCode=" + code);
				
				for (int j = 0; j < metrics.size(); j++) {
					DateRangeValues values = metrics.get(j);
					
					
					for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
						
						if(metricHeaders.get(k).getName().equals("pageviews")) {
							entity.setPageviews(Integer.parseInt(values.getValues().get(k)));
							sumpv+=Integer.parseInt(values.getValues().get(k));
						}							
						else if(metricHeaders.get(k).getName().equals("uniquePageviews"))
							entity.setUniquePageviews(Integer.parseInt(values.getValues().get(k)));
						else if(metricHeaders.get(k).getName().equals("sessions"))
							entity.setSessions(Integer.parseInt(values.getValues().get(k)));
						else if(metricHeaders.get(k).getName().equals("entrances"))
							entity.setEntrances(Integer.parseInt(values.getValues().get(k)));
						else if(metricHeaders.get(k).getName().equals("bounces"))
							entity.setBounces(Integer.parseInt(values.getValues().get(k)));
					}
				}
				
				list.add(entity);
			}
			
			System.out.println("sumpv = " + sumpv);
		}
		
		
		return list;
	}

	private String parseCodeInURL(String url){
		  
		String progress = null, tcode = null, subj = null, extra = null;		
		Map<String, String> param = null;
		
		try {
			URL paramUrl = new URL("http://brand.eduwill.net"+url);
			param = splitQuery(paramUrl);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		if(param.get("progress") != null)progress = param.get("progress");
		if(param.get("tcode") != null)tcode = param.get("tcode").substring(0, 4);
		if(param.get("subj") != null)subj = param.get("subj");
		if(param.get("company") != null)extra = param.get("company");
		
		if (progress == null || tcode == null)
			return "---";

		String code = progress + "-" + tcode + "-";
		
		if(subj != null)code = code + subj;
		code = code + "-";
		if(extra != null)code = code + extra;
		
		if(code.length() >= 20) {
			System.out.println("pageCode : " + code);
			return code.substring(0, 19);
		}
		return code;	
	}
	
	public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
	    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
	    String query = url.getQuery();
	    String[] pairs = query.split("&");
	    for (String pair : pairs) {
	        int idx = pair.indexOf("=");
	        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8").toLowerCase(), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
	    }
	    return query_pairs;
	}
}
