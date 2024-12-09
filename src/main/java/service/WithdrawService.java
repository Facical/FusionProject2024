package service;

import dao.WithdrawDAO;
import dao.RefundDAO;
import dto.WithdrawDTO;
import dto.RefundDTO;
import java.util.List;

public class WithdrawService {
    private final WithdrawDAO withdrawDAO = new WithdrawDAO();
    private final RefundDAO refundDAO = new RefundDAO();

    public String getApprovedWithdrawData() {
        List<WithdrawDTO> withdraws = withdrawDAO.getApprovedWithdraws();
        StringBuilder sb = new StringBuilder();

        for (WithdrawDTO withdraw : withdraws) {
            sb.append(withdraw.getStudentId()).append(",")
                    .append(withdraw.getWithdrawalDate()).append(",")
                    .append(withdraw.getBankName()).append(",")
                    .append(withdraw.getAccountNumber()).append(",")
                    .append(withdraw.getRefundAmount()).append(";");
        }

        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
    }

    public boolean processAllRefunds() {
        List<WithdrawDTO> approvedWithdraws = withdrawDAO.getApprovedWithdraws();
        return refundDAO.processRefunds(approvedWithdraws);
    }

}