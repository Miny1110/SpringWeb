package com.jdbc.springweb;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.jdbc.dao.BoardDAO2;
import com.jdbc.dto.BoardDTO;
import com.jdbc.util.MyPage;

@Controller
public class BoardController {
	
	/*@Autowired로 가져와도 되고 @Qualifier("boardDAO")로 가져와도 된다.*/
	@Autowired
	@Qualifier("boardDAO2")
	BoardDAO2 dao;
	
	@Autowired
	MyPage myPage;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		return "index";
	}
	
	/*
	@RequestMapping(value = "/created.action", method = RequestMethod.GET)
	public String created() throws Exception{
		
		return "bbs/created";
	}
	*/
	
	@RequestMapping(value = "/created.action",method = RequestMethod.GET)
	public ModelAndView created() {
		
		ModelAndView mav = new ModelAndView();
		
		mav.setViewName("bbs/created");
		
		return mav;
	}
	
	@RequestMapping(value = "/created_ok.action", method = RequestMethod.POST)
	public String created_ok(BoardDTO dto,HttpServletRequest request) throws Exception{
		
		int maxNum = dao.getMaxNum();
		
		dto.setNum(maxNum+1);
		dto.setIpAddr(request.getRemoteAddr());
		
		dao.insertData(dto);
		
		return "redirect:/list.action";
	}
	
	@RequestMapping(value = "/list.action",method = {RequestMethod.GET, RequestMethod.POST})
	public String list(HttpServletRequest request) throws Exception {
		
		String cp = request.getContextPath();
		
		String pageNum = request.getParameter("pageNum");

		int currentPage = 1;

		if(pageNum!=null) {
			currentPage = Integer.parseInt(pageNum);
		}

		String searchKey = request.getParameter("searchKey");
		String searchValue = request.getParameter("searchValue");

		if(searchKey==null) {
			searchKey = "subject";
			searchValue = "";
		}else {
			if(request.getMethod().equalsIgnoreCase("GET")) {
				searchValue = URLDecoder.decode(searchValue, "UTF-8");
			}
		}

		//전체 데이터 갯수
		int dataCount = dao.getDataCount(searchKey, searchValue);

		//페이지에 표시되는 수
		int numPerPage = 5;

		//전체 페이지 갯수
		int totalPage = myPage.getPageCount(numPerPage, dataCount);

		//삭제했을때 제일 마지막 페이지에 보이는 데이터 처리
		if(currentPage>totalPage) {
			currentPage = totalPage;
		}

		//rowNum에 시작과 끝 값
		int start = (currentPage-1) * numPerPage +1;
		int end = currentPage * numPerPage;

		List<BoardDTO> lists = 
				dao.getLists(start, end, searchKey, searchValue);

		String param = "";
		if(searchValue!=null && !searchValue.equals("")) {
			param = "searchKey=" + searchKey;
			param+= "&searchValue=" + URLEncoder.encode(searchValue, "UTF-8");
		}

		String listUrl = cp + "/list.action";

		if(!param.equals("")) {
			listUrl += "?" + param;
		}

		String pageIndexList = 
				myPage.pageIndexList(currentPage, totalPage, listUrl);

		String articleUrl = cp + "/article.action?pageNum=" + currentPage;

		if(!param.equals("")) {
			articleUrl += "&" + param;
		}

		//포워딩할 데이터
		request.setAttribute("lists", lists);
		request.setAttribute("pageIndexList", pageIndexList);
		request.setAttribute("articleUrl", articleUrl);
		request.setAttribute("dataCount", dataCount);
		
		return "/bbs/list";
	}
	
	/*
	@RequestMapping(value = "/article.action",
	method = {RequestMethod.GET, RequestMethod.POST})
	public String ariticle(HttpServletRequest request, 
	HttpServletResponse response) throws Exception {
	*/	
	
	@RequestMapping(value = "/article.action",method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView article(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
	
		String cp = request.getContextPath();
		
		int num = Integer.parseInt(request.getParameter("num"));
		String pageNum = request.getParameter("pageNum");

		String searchKey = request.getParameter("searchKey");
		String searchValue = request.getParameter("searchValue");

		if(searchValue!=null && !searchValue.equals("")) {
			searchValue = URLDecoder.decode(searchValue,"UTF-8");
		}

		dao.updateHitCount(num);

		BoardDTO dto = dao.getReadData(num);

		if(dto==null) {
			//String url = cp + "/list.action";
			//response.sendRedirect(url);
			
			ModelAndView mav = new ModelAndView();
			mav.setViewName("redirect:/list.action");
			
			return mav;
		}

		int lineSu = dto.getContent().split("\n").length;

		dto.setContent(dto.getContent().replaceAll("\r", "<br/>"));

		String param = "pageNum=" + pageNum;

		if(searchValue!=null && !searchValue.equals("")) {

			param += "&searchKey=" + searchKey;
			param += "&searchValue=" + URLEncoder.encode(searchValue, "UTF-8");

		}
		/*
		response.setAttribute("dto", dto);
		response.setAttribute("params", param);
		response.setAttribute("lineSu", lineSu);
		response.setAttribute("pageNum", pageNum);

		return "bbs/article";
		*/
		
		ModelAndView mav = new ModelAndView();
		
		mav.setViewName("bbs/ariticle");
		
		mav.addObject("dto",dto);
		mav.addObject("params", param);
		mav.addObject("lineSu", lineSu);
		mav.addObject("pageNum", pageNum);
		
		mav.setViewName("bbs/article");
		
		return mav;
		
	}
	
	
	@RequestMapping(value = "/updated.action",method = {RequestMethod.GET, RequestMethod.POST})
	public String updated(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		String cp = request.getContextPath();
		int num = Integer.parseInt(request.getParameter("num"));
		String pageNum = request.getParameter("pageNum");
		
		String searchKey = request.getParameter("searchKey");
		String searchValue = request.getParameter("searchValue");
		
		if(searchValue!=null) {
			searchValue = URLDecoder.decode(searchValue,"utf-8");
					
		}
		
		BoardDTO dto = dao.getReadData(num);
		
		if(dto==null) {
			String url = cp + "/bbs/list.do";
			response.sendRedirect(url);
		}
		
		String param = "pageNum=" + pageNum;
		
		if(searchValue!=null && !searchValue.equals("")) {
			param += "&searchKey=" + searchKey;
			param += "&searchValue=" + URLEncoder.encode(searchValue,"utf-8");
		}
		
		request.setAttribute("dto", dto);
		request.setAttribute("pageNum", pageNum);
		request.setAttribute("params", param);
		request.setAttribute("searchKey", searchKey);
		request.setAttribute("searchValue", searchValue);
		/*param에도 searchKey와 searchValue가 있지만,
		params에서는 searchKey와 searchValue를 따로 떼서 사용할 수 없기 때문에 따로 보내주어야 한다.*/
		
		return "bbs/updated";
		
		
	}
	
	
	@RequestMapping(value = "/updated_ok.action",
			method = {RequestMethod.GET, RequestMethod.POST})
	public String updated_ok(BoardDTO dto,HttpServletRequest request,HttpServletResponse response) throws Exception {
	
		String pageNum = request.getParameter("pageNum");
		
		String searchKey = request.getParameter("searchKey");
		String searchValue = request.getParameter("searchValue");
		
		if(searchValue!=null) {
			searchValue = URLDecoder.decode(searchValue,"utf-8");
		}		
		
		dao.updatedData(dto);
		
		String param = "pageNum=" + pageNum;
		
		if(searchValue!=null && !searchValue.equals("")) {
			param += "&searchKey=" + searchKey;
			param += "&searchValue=" + URLEncoder.encode(searchValue,"utf-8");
		}
	
		return "redirect:/list.action?" + param;	
	
	}
	
	
	@RequestMapping(value = "/deleted_ok.action",
			method = {RequestMethod.GET, RequestMethod.POST})
	public String deleted_ok(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		int num = Integer.parseInt(request.getParameter("num"));
		String pageNum = request.getParameter("pageNum");
		
		String searchKey = request.getParameter("searchKey");
		String searchValue = request.getParameter("searchValue");
		
		if(searchValue!=null && searchValue.equals("")) {
			searchValue = URLDecoder.decode(searchValue,"utf-8");
					
		}	
		
		dao.deleteData(num);
		
		String param = "pageNum=" + pageNum;
		
		if(searchValue!=null && !searchValue.equals("")) {
			param += "&searchKey=" + searchKey;
			param += "&searchValue=" + URLEncoder.encode(searchValue,"utf-8");
		}			
		
		return "redirect:/list.action?" + param;
	}
	
	
		
	
	
	
	
	
	
	
	
	
	
	
}               

