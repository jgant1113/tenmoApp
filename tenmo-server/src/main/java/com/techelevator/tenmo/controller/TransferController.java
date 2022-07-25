package com.techelevator.tenmo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.techelevator.tenmo.dao.TransferDAO;
import com.techelevator.tenmo.model.Transfer;

@RestController
@PreAuthorize("isAuthenticated()")
public class TransferController {

    @Autowired
    private TransferDAO transfersDAO;

    @RequestMapping(value = "account/transfers/{id}", method = RequestMethod.GET)
    public List<Transfer> getAllMyTransfers(@PathVariable int id) {
        List<Transfer> output = transfersDAO.getAllTransfers(id);
        return output;
    }

    @RequestMapping(path = "transfers/{id}", method = RequestMethod.GET)
    public Transfer getSelectedTransfer(@PathVariable int id) {
        Transfer transfer = transfersDAO.getTransferById(id);
        return transfer;
    }

    @RequestMapping(path = "transfer", method = RequestMethod.POST)
    public String sendTransferRequest(@RequestBody Transfer transfer) {
        String results = transfersDAO.sendTransfer(transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
        return results;
    }

    @RequestMapping(path = "request", method = RequestMethod.POST)
    public String requestTransferRequest(@RequestBody Transfer transfer) {
        String results = transfersDAO.requestTransfer(transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
        return results;
    }

    @RequestMapping(value = "request/{id}", method = RequestMethod.GET)
    public List<Transfer> getAllTransferRequests(@PathVariable int id) {
        List<Transfer> output = transfersDAO.getPendingRequests(id);
        return output;
    }


    @RequestMapping(path = "transfer/status/{statusId}", method = RequestMethod.PUT)
    public String updateRequest(@RequestBody Transfer transfer, @PathVariable int statusId) {
        String output = transfersDAO.updateTransferRequest(transfer, statusId);
        return output;
    }
}
