package com.fams.service;

import com.fams.database.MarksDAO;
import java.sql.SQLException;
import java.util.List;

/** Application service for marks eligibility operations. */
public class MarksService {
    private final MarksDAO dao;
    public MarksService() { this(new MarksDAO()); }
    public MarksService(MarksDAO dao) { this.dao=dao; }
    public List<MarksDAO.Eligibility> checkEligibility(String studentId) throws SQLException {
        String id=(studentId==null||studentId.isBlank())?null:studentId.trim();
        return dao.getEligibility(id);
    }
}
