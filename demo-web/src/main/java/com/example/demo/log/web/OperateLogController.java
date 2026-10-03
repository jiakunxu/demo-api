package com.example.demo.log.web;

import com.example.demo.framework.response.ListResponse;
import com.example.demo.framework.response.ObjectResponse;
import com.example.demo.framework.web.BaseController;
import com.example.demo.operate.api.OperateLogService;
import com.example.demo.operate.api.bo.OperateLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/monitor/log/oper")
public class OperateLogController extends BaseController {

    @Autowired
    private OperateLogService operateLogService;

    @PreAuthorize("hasAuthority('monitor:log:crud')")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ListResponse<OperateLog> list(HttpServletRequest request, HttpServletResponse response) {
        OperateLog log = this.getParameter(request, new OperateLog());

        long count = operateLogService.countLog(log);

        if (count == 0) {
            return new ListResponse<>(0, List.of());
        }

        return new ListResponse<>(count, operateLogService.listLogs(log));
    }

    @PreAuthorize("hasAuthority('monitor:log:crud')")
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ObjectResponse<OperateLog> get(HttpServletRequest request,
                                          HttpServletResponse response) {
        String id = this.getParameter(request, "id");
        return new ObjectResponse<>(operateLogService.getLog(id));
    }

}
