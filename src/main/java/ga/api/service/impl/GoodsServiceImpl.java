package ga.api.service.impl;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import ga.api.dao.GoodsDAO;
import ga.api.domain.DailyVO;
import ga.api.domain.GoodsVO;
import ga.api.domain.InformVO;
import ga.api.service.GoodsService;

//Implementing MercService;
@Service
public class GoodsServiceImpl implements GoodsService {

	//Inject "Merchandise Data Access Object" to access MyBatis mapper(=query) to process SQL Query for tbl_merchandise;
	@Autowired
	private GoodsDAO dao;
	
	/**
	 * 상품 자동화 페이지의 전체 목록을 가져오는 메소드
	 * @param param
	 * @param model 상품 자동화 페이지 목록이 반환됨
	 * @param request
	 * @param response
	 * */
	@Override
	public void ListAll(Map<String, Object> param, ModelMap model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		List<GoodsVO> list = replacePathToCode(dao.listAll());
		model.addAttribute("list", list);
	}
	
	/**
	 * DB 조회를 통해 검색 기간 내의 정보를 반환하는 메소드
	 * @param param seq, startDate, endDate
	 * @param model GA 화면 구성 정보를 반환
	 * @param request
	 * @param response
	 * */
	@Override
	public void readDataFromDB(Map<String, Object> param, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		
		//변수 선언 및 초기화---------------------------------------
		//파라미터 정보(코드, 기간)
		String seq = (String) param.get("seq");
		String startDate = (String) param.get("startDate");
		String endDate = (String) param.get("endDate");
		
		//기본 날짜 범위
		if(startDate == null || endDate == null) {
			SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date todayDate = new Date();
			String today = transFormat.format(todayDate);
			String lastmonth = transFormat.format(getMonthAgo(todayDate));
			startDate = lastmonth;
			endDate = today;
		}
		
		//한달 전, 일년 전 기간
		String startDate_lastmonth = null, endDate_lastmonth = null, startDate_lastyear = null, endDate_lastyear = null;
		//통합 데이터(표)
		List<InformVO> resultList = null, resultList_lastmonth = null, resultList_lastyear = null;
		//일일 데이터(그래프)
		List<DailyVO> dailyDataList = null, dailyDataList_lastmonth = null, dailyDataList_lastyear = null;
		
		System.out.println("==========================================");
		System.out.println("seq			: " + seq);
		System.out.println("startDate	: " + startDate);
		System.out.println("endDate		: " + endDate);
		System.out.println("==========================================");
		
		//(한달 전, 일년 전) 기간정보 획득----------------------------
		if (startDate != null && endDate != null) {
			try {
				SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date start = transFormat.parse(startDate);
				Date end = transFormat.parse(endDate);
				// 한달 전
				startDate_lastmonth = transFormat.format(getMonthAgo(start));
				endDate_lastmonth = transFormat.format(getMonthAgo(end));
				System.out.println("한달 전 : " + startDate_lastmonth + " ~ " + endDate_lastmonth);

				// 일년 전
				startDate_lastyear = transFormat.format(getYearAgo(start));
				endDate_lastyear = transFormat.format(getYearAgo(end));
				System.out.println("일년 전 : " + startDate_lastyear + " ~ " + endDate_lastyear);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}		
		//데이터 읽어오기---------------------------------------------
		//현재
		resultList = dao.getIntegratedData(seq, startDate, endDate);
		dailyDataList = dao.getDailyData(seq, startDate, endDate);
		//한달 전
		if(startDate_lastmonth != null && endDate_lastmonth != null) {
			resultList_lastmonth = dao.getIntegratedData(seq, startDate_lastmonth, endDate_lastmonth);
			dailyDataList_lastmonth = dao.getDailyData(seq, startDate_lastmonth, endDate_lastmonth);
		}
		//일년 전
		if(startDate_lastyear != null && endDate_lastyear != null) {
			resultList_lastyear = dao.getIntegratedData(seq, startDate_lastyear, endDate_lastyear);
			dailyDataList_lastyear = dao.getDailyData(seq, startDate_lastyear, endDate_lastyear);
		}		
		
		//데이터 시각화(소숫점 제한)-----------------------------------
		//현재
		if(dailyDataList == null || dailyDataList.isEmpty())dailyDataList = null;
		if(resultList.isEmpty())resultList = null;
		else {
			//calculate bounceRate, eventRate
			DecimalFormat format = new DecimalFormat(".###"); // 소숫점 3자리 까지 제한
			
			for(InformVO vo : resultList) {
				if(vo.getSessions() > 0)vo.setBounceRate(Double.parseDouble(format.format(vo.getBounces() / (double)vo.getSessions() * 100)));
				if(vo.getPageviews() > 0)vo.setEventRate(Double.parseDouble(format.format(vo.getTotalEvents() / (double)vo.getPageviews() * 100)));
			}
		}		
		//한달 전
		if(dailyDataList_lastmonth == null || dailyDataList_lastmonth.isEmpty())dailyDataList_lastmonth = null;
		if(resultList_lastmonth == null || resultList_lastmonth.isEmpty())resultList_lastmonth = null;
		else {
			//calculate bounceRate, eventRate
			DecimalFormat format = new DecimalFormat(".###"); // 소숫점 3자리 까지 제한
			for(InformVO vo : resultList_lastmonth) {
				if(vo.getSessions() > 0)vo.setBounceRate(Double.parseDouble(format.format(vo.getBounces() / (double)vo.getSessions() * 100)));
				if(vo.getPageviews() > 0)vo.setEventRate(Double.parseDouble(format.format(vo.getTotalEvents() / (double)vo.getPageviews() * 100)));
			}
			
		}
		//일년 전
		if(dailyDataList_lastyear == null || dailyDataList_lastyear.isEmpty())dailyDataList_lastyear = null;
		if(resultList_lastyear == null || resultList_lastyear.isEmpty())resultList_lastyear = null;
		else {
			//calculate bounceRate, eventRate
			DecimalFormat format = new DecimalFormat(".###"); // 소숫점 3자리 까지 제한
			for(InformVO vo : resultList_lastyear) {
				if(vo.getSessions() > 0)vo.setBounceRate(Double.parseDouble(format.format(vo.getBounces() / (double)vo.getSessions() * 100)));
				if(vo.getPageviews() > 0)vo.setEventRate(Double.parseDouble(format.format(vo.getTotalEvents() / (double)vo.getPageviews() * 100)));
			}
			
		}
		
		if(dailyDataList_lastmonth != null && !dailyDataList_lastmonth.isEmpty()) {
			for(DailyVO vo : dailyDataList_lastmonth) {
				try {
					SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
					Date day = transFormat.parse(vo.getmDate());
					Calendar cal = Calendar.getInstance();
					cal.setTime(day);
					cal.add(Calendar.MONTH, 1);
					String tmp = transFormat.format(cal.getTime());
					vo.setmDate(tmp);
				}
				catch(Exception e) {
					e.printStackTrace();
				}			
			}
		}
		if(dailyDataList_lastyear != null && !dailyDataList_lastyear.isEmpty()) {
			for(DailyVO vo : dailyDataList_lastyear) {
				try {
					SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
					Date day = transFormat.parse(vo.getmDate());
					Calendar cal = Calendar.getInstance();
					cal.setTime(day);
					cal.add(Calendar.YEAR, 1);
					String tmp = transFormat.format(cal.getTime());
					vo.setmDate(tmp);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		//데이터 전송------------------------------------------------------------
		model.addAttribute("seq", seq);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("result", resultList);
		model.addAttribute("result_lastmonth", resultList_lastmonth);
		model.addAttribute("result_lastyear", resultList_lastyear);
		model.addAttribute("dailyDataList", dailyDataList);
		model.addAttribute("dailyDataList_lastmonth", dailyDataList_lastmonth);
		model.addAttribute("dailyDataList_lastyear", dailyDataList_lastyear);
		model.addAttribute("lastmonth", new String(startDate_lastmonth + "~" + endDate_lastmonth));
		model.addAttribute("lastyear", new String(startDate_lastyear + "~" + endDate_lastyear));
		GoodsVO pageName = dao.getPageName((String)param.get("idx"));
		model.addAttribute("pageName", pageName.getName());
	}
	
	private Date getMonthAgo(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, -1);

		return cal.getTime();
	}

	private Date getYearAgo(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.YEAR, -1);

		return cal.getTime();
	}

	private List<GoodsVO> replacePathToCode(List<GoodsVO> oldList) {

		List<GoodsVO> newList = new ArrayList<GoodsVO>();
		
		for(GoodsVO vo : oldList) {
			String tmpCode = parseCodeInURL(vo.getCode());
			vo.setCode(tmpCode);
			newList.add(vo);
		}
		
		return newList;
	}
	
	private String parseCodeInURL(String url) {
		  String code = null;
		  
		  String []arr = url.split("\\?");
		  if(arr.length > 1) {
			  String tmp = arr[1];
			  arr = tmp.split("&");
			  
			  int idx=0;
			  if(arr.length > 1) {
				  for(idx=0; idx < arr.length; idx++) {
					  if(arr[idx].contains("masterSeq"))break;
				  }
			  }
			  tmp = arr[idx];
			  arr = tmp.split("=");
			  code = arr[1];
			  while(code.length()%4 != 0)code = code.concat("=");
		  }
		  
		  return code;
	}
}


