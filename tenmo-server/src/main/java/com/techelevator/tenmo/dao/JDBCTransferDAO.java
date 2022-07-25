package com.techelevator.tenmo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import com.techelevator.tenmo.model.TransferNotFoundException;
import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JDBCTransferDAO implements TransferDAO {


    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AccountDAO accountDAO;

    //SQL WORKS HERE, but not returning any data until we have other transfer sql working
    @Override
    public List<Transfer> getAllTransfers(int userId) {
        List<Transfer> list = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfer";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while (results.next() ) {
            Transfer transfer = mapRowToTransfer(results);
            list.add(transfer);
        }
        return list;
    }

    //SQL WORKS HERE, but not returning any data until we have other transfer sql working
    @Override
    public Transfer getTransferById(int transactionId) {
        Transfer transfer = new Transfer();
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfer WHERE transfer_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transactionId);
        if (results.next()) {
            transfer = mapRowToTransfer(results);
        } else {
            throw new TransferNotFoundException();
        }
        return transfer;
    }

    //SQL WORKKKSSSSSSS
    @Override
    public String sendTransfer(int userFrom, int userTo, BigDecimal amount) {
        SqlRowSet results = null;
        Transfer transferHistory = null;
        List<Transfer> output = new ArrayList<>();

        if (userFrom == userTo) {
            return " Why would you send yourself money? Just keep it! ";
        }
        if ((amount.compareTo(accountDAO.getBalance(userFrom)) == -1) && (amount.compareTo(new BigDecimal(0)) == 1)) {
            String sql =    "BEGIN;\n" +
                            "update account set balance = balance - ? where user_id = ?;\n" +
                            "update account set balance = balance + ? where user_id = ?;\n" +
                            "COMMIT;";
            jdbcTemplate.update(sql, amount, userFrom, amount, userTo);
            BigDecimal userFromNewBalance = accountDAO.getBalance(userFrom);
            BigDecimal userToNewBalance = accountDAO.getBalance(userTo);
            System.out.println("-----------------------------------------------------------");
            return "Balance for " + userFrom + " is " + userFromNewBalance + ", and balance for " + userTo + " is " + userToNewBalance;

        } else {
            return "Transfer failed due to a lack of funds, amount was $0 or less, or there was no valid user. ";
        }
    }

    //SQL NEEDS DEBUGGING
    @Override
    public String requestTransfer(int userFrom, int userTo, BigDecimal amount) {
        if (userFrom == userTo) {
            return " You can't take money from yourself! ";
        }
        if (amount.compareTo(new BigDecimal(0)) == 1) {
            String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                    "VALUES (1, 1, ?, ?, ?)";
            jdbcTemplate.update(sql, userFrom, userTo, amount);
            return "Request sent";
        } else {
            return " The request has an error, and could not completed at this time. ";
        }
    }

    //Needs to be debugged
    @Override
    public List<Transfer> getPendingRequests(int userId) {
        List<Transfer> output = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_type_id, transfer.transfer_status_id, account_from, account_to, amount " +
                "FROM transfer " +
                "JOIN account ON account.account_id = transfer.account_from " +
                "JOIN transfer_status ON transfer.transfer_status_id = transfer_status.transfer_status_id " +
                "WHERE user_id = ? AND transfer_status_desc = 'Pending'";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, userId);
        while (results.next()) {
            Transfer transfer = mapRowToTransfer(results);
            output.add(transfer);
        }
        return output;
    }

    //Needs debugging on SQL
    @Override
    public String updateTransferRequest(Transfer transfer, int statusId) {
        if (statusId == 3) {
            String sql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?;";
            jdbcTemplate.update(sql, statusId, transfer.getTransferId());
            return " Update was successful! ";
        }
        if (!(accountDAO.getBalance(transfer.getAccountFrom()).compareTo(transfer.getAmount()) == -1)) {
            String sql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?;";
            jdbcTemplate.update(sql, statusId, transfer.getTransferId());
            accountDAO.addToBalance(transfer.getAmount(), transfer.getAccountTo());
            accountDAO.subtractFromBalance(transfer.getAmount(), transfer.getAccountFrom());
            return "Update was successful! ";
        } else {
            return " Insufficient funds, transfer unable to complete. ";
        }
    }

    private Transfer mapRowToTransfer(SqlRowSet results) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(results.getInt("transfer_id"));
        transfer.setTransferTypeId(results.getInt("transfer_type_id"));
        transfer.setTransferStatusId(results.getInt("transfer_status_id"));
        transfer.setAccountFrom(results.getInt("account_From"));
        transfer.setAccountTo(results.getInt("account_to"));
        transfer.setAmount(results.getBigDecimal("amount"));
        try {
            transfer.setUserFrom(results.getString("userFrom"));
            transfer.setUserTo(results.getString("userTo"));
        } catch (Exception e) {}
        try {
            transfer.setTransferType(results.getString("transfer_type_desc"));
            transfer.setTransferStatus(results.getString("transfer_status_desc"));
        } catch (Exception e) {}
        return transfer;
    }



}
