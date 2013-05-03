package com.cloudhealing.services;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/logger")
public class ReportLogService {

    @RequestMapping(value = "/sendLog", method = RequestMethod.POST)
    public String acceptLog(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        String log = request.getParameter("log");

        response.setStatus(HttpServletResponse.SC_CREATED);
        model.addAttribute("message", log);
        model.addAttribute("success", true);
        return "";

    }

}