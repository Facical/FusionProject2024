package org.example;

import persistence.dao.BoardDAO;
import persistence.dto.BoardDTO;
import service.BoardService;
import view.BoardView;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String args[]){
            BoardDAO boardDAO = new BoardDAO();
            BoardView boardView= new BoardView();
            BoardService boardService = new BoardService(boardDAO);
            List<BoardDTO> all = boardService.findAll();
            boardView.printAll(all);

    }
}