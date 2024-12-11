package service;

import dao.WithdrawDAO;
import dao.RefundDAO;
import dto.WithdrawDTO;
import dto.RefundDTO;
import dto.WithdrawDetailDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WithdrawService {
    private final WithdrawDAO withdrawDAO = new WithdrawDAO();
    private final RefundDAO refundDAO = new RefundDAO();

    public String getApprovedWithdrawData() {
        List<WithdrawDetailDTO> withdraws = withdrawDAO.getApprovedWithdrawsByDormitory();
        Map<String, List<WithdrawDetailDTO>> groupedWithdraws = new HashMap<>();

        // 생활관별로 그룹화
        for (WithdrawDetailDTO withdraw : withdraws) {
            groupedWithdraws.computeIfAbsent(withdraw.getDormitoryName(), k -> new ArrayList<>()).add(withdraw);
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<WithdrawDetailDTO>> entry : groupedWithdraws.entrySet()) {
            sb.append("DORM_START|").append(entry.getKey()).append("\n");
            for (WithdrawDetailDTO withdraw : entry.getValue()) {
                sb.append("STUDENT|")
                        .append(withdraw.getStudentName()).append("|")
                        .append(withdraw.getStudentId()).append("|")
                        .append(withdraw.getWithdrawalDate()).append("|")
                        .append(withdraw.getBankName()).append("|")
                        .append(withdraw.getAccountNumber()).append("|")
                        .append(withdraw.getRefundAmount()).append("\n");
            }
            sb.append("DORM_END\n");
        }

        return sb.toString();
    }

    public boolean processAllRefunds() {
        List<WithdrawDTO> approvedWithdraws = withdrawDAO.getApprovedWithdraws();
        return refundDAO.processRefunds(approvedWithdraws);
    }

//    public String getApprovedWithdrawData() {
//        List<WithdrawDTO> withdraws = withdrawDAO.getApprovedWithdraws();
//        StringBuilder sb = new StringBuilder();
//
//        for (WithdrawDTO withdraw : withdraws) {
//            sb.append(withdraw.getStudentId()).append(",")
//                    .append(withdraw.getWithdrawalDate()).append(",")
//                    .append(withdraw.getBankName()).append(",")
//                    .append(withdraw.getAccountNumber()).append(",")
//                    .append(withdraw.getRefundAmount()).append(";");
//        }
//
//        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
//    }
//


}