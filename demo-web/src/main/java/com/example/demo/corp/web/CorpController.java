package com.example.demo.corp.web;

import com.example.demo.corp.api.CorpService;
import com.example.demo.corp.api.bo.Corp;
import com.example.demo.framework.web.BaseController;
import com.example.demo.framework.response.ListResponse;
import com.example.demo.framework.response.ObjectResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author JiakunXu
 */
@RestController
@RequestMapping(value = "/corp")
public class CorpController extends BaseController {

    @Autowired
    private CorpService corpService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ListResponse<Corp> list(HttpServletRequest request, HttpServletResponse response) {
        Corp corp = this.getParameter(request, new Corp());

        long count = corpService.countCorp(corp);

        if (count == 0) {
            return new ListResponse<>(0, null);
        }

        return new ListResponse<>(count, corpService.listCorps(corp));
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ObjectResponse<Corp> get(HttpServletRequest request, HttpServletResponse response) {
        String id = this.getParameter(request, "id");
        return new ObjectResponse<>(corpService.getCorp(id));
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ObjectResponse<Corp> update(HttpServletRequest request, HttpServletResponse response) {
        Corp corp = this.getParameter(request, Corp.class);
        return new ObjectResponse<>(
            corpService.updateCorp(corp.getId(), corp, this.getUser().getName()));
    }

}
