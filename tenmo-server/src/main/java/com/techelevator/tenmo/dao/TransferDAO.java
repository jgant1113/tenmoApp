package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.List;

import com.techelevator.tenmo.model.Transfer;

public interface TransferDAO {

     List<Transfer> getAllTransfers(int userId);
     Transfer getTransferById(int transactionId);
     String sendTransfer(int userFrom, int userTo, BigDecimal amount);
     String requestTransfer(int userFrom, int userTo, BigDecimal amount);
     List<Transfer> getPendingRequests(int userId);
     String updateTransferRequest(Transfer transfer, int statusId);

}
