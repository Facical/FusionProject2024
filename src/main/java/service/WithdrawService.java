package service;

import dao.WithdrawDAO;
import dao.RefundDAO;
import dto.WithdrawDTO;
import dto.RefundDTO;
import java.util.List;

public class WithdrawService {
    private final WithdrawDAO withdrawDAO = new WithdrawDAO();
    private final RefundDAO refundDAO = new RefundDAO();

    public String getWithdrawAndRefundData() {
        List<WithdrawDTO> withdraws = withdrawDAO.getAllWithdraws();
        StringBuilder sb = new StringBuilder();

        for (WithdrawDTO withdraw : withdraws) {
            List<RefundDTO> refunds = refundDAO.getRefundsByWithdrawId(withdraw.getWithdrawalId());
            int totalRefund = 0;
            for (RefundDTO refund : refunds) {
                totalRefund += refund.getAmount();
            }

            sb.append(withdraw.getStudentId()).append(",")
                    .append(withdraw.getWithdrawalDate()).append(",")
                    .append(withdraw.getBankName()).append(",")
                    .append(withdraw.getAccountNumber()).append(",")
                    .append(totalRefund).append(";");
        }

        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
    }
}