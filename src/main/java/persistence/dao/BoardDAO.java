package persistence.dao;

import persistence.dto.BoardDTO;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import persistence.PooledDataSource;

import javax.sql.DataSource;

public class BoardDAO {
//DTO를 기반으로 수행할 기능
private final DataSource ds = PooledDataSource.getDataSource();

    public List<BoardDTO> findAll(){
        Connection conn = null;
        String sql = "SELECT * FROM BOARD";
        Statement stmt= null;
        ResultSet rs = null;

        List<BoardDTO> boardDTOs = new ArrayList<>();
        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            String url = "jdbc:mysql://localhost/mydb?characterEncoding=utf8&serverTimezone=UTC&useSSL=false";
//            conn = DriverManager.getConnection(url, "root", "0000");
            conn = ds.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                BoardDTO boardDTO = new BoardDTO();
                Long id = rs.getLong("id");
                String title = rs.getString("title");
                String writer = rs.getString("writer");
                String contents = rs.getString("contents");
                LocalDateTime regdate = rs.getTimestamp("regdate").toLocalDateTime();
                int hit = rs.getInt(6);
                boardDTO.setId(id);
                boardDTO.setTitle(title);
                boardDTO.setWriter(writer);
                boardDTO.setContents(contents);
                boardDTO.setRegdate(regdate);
                boardDTO.setHit(hit);
                boardDTOs.add(boardDTO);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            try{
                if(conn != null && !rs.isClosed()){
                    rs.close();
                }
                if(conn != null && !stmt.isClosed()){
                    rs.close();
                }
                if(conn != null && !conn.isClosed()){
                    conn.close();
                }
            }
            catch(SQLException e){
                e.printStackTrace();
            }
        }
        return boardDTOs;
    }
}
