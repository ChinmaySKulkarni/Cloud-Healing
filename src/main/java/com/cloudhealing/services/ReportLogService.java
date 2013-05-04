package com.cloudhealing.services;

import com.cloudhealing.backend.Healer;
import com.cloudhealing.backend.OnlinePredictor;
import com.cloudhealing.backend.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/logger")
public class ReportLogService {


    @Autowired
    OnlinePredictor predictor;

    private Row row;

    @RequestMapping(value = "/sendLog", method = RequestMethod.POST)
    public String acceptLog(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        String log = request.getParameter("log");

        response.setStatus(HttpServletResponse.SC_OK);
        row = Row.generate(log);
        if (predictor.predict(row.getNode(), row.getTime(), row.getEntry_data())) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            System.out.println("Failure Predicted.. Taking healing action!!");
            Runnable runnable = new MyThread(row.getNode());
            (new Thread(runnable)).start();
        }
        model.addAttribute("entryProcessed", log);
        model.addAttribute("success", true);
        return "";

    }
}

class MyThread implements Runnable {

    private String node;

    public MyThread(String node) {
        this.node = node;
    }

    @Override
    public void run() {

        Healer healer = new Healer();
        healer.heal(node);
        System.out.println("Migration Successful");

    }
}