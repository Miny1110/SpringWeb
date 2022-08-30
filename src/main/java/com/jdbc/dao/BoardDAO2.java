package com.jdbc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.jdbc.dto.BoardDTO;

public class BoardDAO2 {

	private JdbcTemplate jdbcTemplate;
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) throws Exception {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	//num의 최대값
	public int getMaxNum() {
		
		int maxNum = 0;
		
		StringBuilder sql = new StringBuilder(200);
		sql.append("select nvl(max(num),0) from board");
			
		maxNum = jdbcTemplate.queryForInt(sql.toString());
		
		return maxNum;
		
	}
	
	//입력
	public void insertData(BoardDTO dto) {
		
		StringBuilder sql = new StringBuilder();
			
		sql.append("insert into board(num,name,pwd,email,subject,");
		sql.append("content,ipAddr,hitCount,created) ");
		sql.append("values (?,?,?,?,?,?,?,0,sysdate)");
		
		/* 이렇게 써도 된다
		sql.append("insert into board(num,name,pwd,email,subject,")
		.append("content,ipAddr,hitCount,created) ")
		.append("values (?,?,?,?,?,?,?,0,sysdate)");
		*/
		
		jdbcTemplate.update(sql.toString(),
				dto.getNum(),dto.getName(),dto.getPwd(),dto.getEmail(),dto.getSubject(),
				dto.getContent(),dto.getIpAddr());
	}
	
	//전체데이터 가져오기
	public List<BoardDTO> getLists(int start,int end,String searchKey,String searchValue){
		
		StringBuilder sql = new StringBuilder(500);
			
		searchValue = "%" + searchValue + "%";
			
		sql.append("select * from (")
		.append("select rownum rnum,data.* from(")
		.append("select num,name,subject,hitCount,")
		.append("to_char(created,'YYYY-MM-DD') created ")
		.append("from board where " + searchKey + " like ? order by num desc) data ) ")
		.append("where rnum>=? and rnum<=?");
			
		/* 여러개의 데이터를 넘길 때에는 Object 배열에 담아서 넘기면 된다. */
		List<BoardDTO> lists = jdbcTemplate.query(sql.toString(), 
				new Object[] {searchValue,start,end},new RowMapper<BoardDTO>() {

					@Override
					public BoardDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

						BoardDTO dto = new BoardDTO();
						
						dto.setNum(rs.getInt("num"));
						dto.setName(rs.getString("name"));
						dto.setSubject(rs.getString("subject"));
						dto.setHitCount(rs.getInt("hitCount"));
						dto.setCreated(rs.getString("created"));
						
						return dto;
					}
		});
		return lists;
	}
	

	//전체데이터의 개수
	public int getDataCount(String searchKey,String searchValue) {
		
		int dataCount = 0;
		StringBuilder sql = new StringBuilder();
			
		searchValue = "%" + searchValue + "%";
		
		sql.append("select nvl(count(*),0) from board ")
		.append("where " + searchKey + " like ?");

		dataCount = jdbcTemplate.queryForInt(sql.toString(),searchValue);
		
		return dataCount;
		
	}
	

	//num으로 한개의 데이터 가져오기
	public BoardDTO getReadData(int num) {
			
		StringBuilder sql = new StringBuilder();
		
		sql.append("select num,name,pwd,email,subject,content,ipAddr,hitCount,created ")
		.append("from board where num=?");
		
		BoardDTO dtoOne = jdbcTemplate.queryForObject(sql.toString(),new RowMapper<BoardDTO>() {

				@Override
				public BoardDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

					BoardDTO dto = new BoardDTO();
					
					dto.setNum(rs.getInt("num"));
					dto.setName(rs.getString("name"));
					dto.setPwd(rs.getString("pwd"));
					dto.setEmail(rs.getString("email"));
					dto.setSubject(rs.getString("subject"));
					dto.setContent(rs.getString("content"));
					dto.setIpAddr(rs.getString("ipAddr"));
					dto.setHitCount(rs.getInt("hitCount"));
					dto.setCreated(rs.getString("created"));
					
					return dto;
				}
		},num);
			
		return dtoOne;
	}
	
	
	//조회수 증가
	public void updateHitCount(int num) {
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("update board set hitCount = hitCount + 1 where num=?");

		jdbcTemplate.update(sql.toString(),num);
		
	}
	
	
	//수정
	public void updatedData(BoardDTO dto) {
			
		StringBuilder sql = new StringBuilder();
		
		sql.append("update board set name=?,pwd=?,email=?,subject=?,content=? ")
		.append("where num=?");
		
		jdbcTemplate.update(sql.toString(),
				dto.getName(),dto.getPwd(),dto.getEmail(),dto.getSubject(),dto.getContent(),dto.getNum());
		
	}
	
	
	//삭제
	public void deleteData(int num) {
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("delete board where num=?");
		
		jdbcTemplate.update(sql.toString(),num);
		
	}
	
	
}
