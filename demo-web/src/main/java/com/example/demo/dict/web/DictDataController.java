package com.example.demo.dict.web;

import com.example.demo.dict.api.DictDataService;
import com.example.demo.dict.api.DictTypeService;
import com.example.demo.dict.api.bo.DictData;
import com.example.demo.dict.api.bo.DictType;
import com.example.demo.framework.annotation.Log;
import com.example.demo.framework.response.ListResponse;
import com.example.demo.framework.response.ObjectResponse;
import com.example.demo.framework.web.BaseController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/system/dict")
public class DictDataController extends BaseController {

    @Autowired
    private DictDataService dictDataService;

    @Autowired
    private DictTypeService dictTypeService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ListResponse<DictData> list(HttpServletRequest request, HttpServletResponse response) {
        String typeValue = this.getParameter(request, "type");
        DictData dict = this.getParameter(request, new DictData());

        if (dict.getPageNo() == null || dict.getPageSize() == null) {
            if (StringUtils.isNotBlank(typeValue)) {
                return new ListResponse<>(dictDataService.listDatas(null, typeValue.split(",")));
            }

            return new ListResponse<>(dictDataService.listDatas(null, (String) null));
        }

        dict.setName(this.getParameter(request, "name"));
        dict.setStatus(this.getParameter(request, "status"));

        long count = dictDataService.countData(null, typeValue, dict);

        if (count == 0) {
            return new ListResponse<>(0, List.of());
        }
        return new ListResponse<>(count, dictDataService.listDatas(null, typeValue, dict));
    }

    @PreAuthorize("hasAuthority('dict:crud')")
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ObjectResponse<DictData> get(HttpServletRequest request, HttpServletResponse response) {
        String id = this.getParameter(request, "id");
        String typeValue = this.getParameter(request, "type");
        String value = this.getParameter(request, "value");
        return new ObjectResponse<>(StringUtils.isNotBlank(id) ? dictDataService.getData(id)
            : dictDataService.getData(typeValue, value));
    }

    @Log(module = "", desc = "")
    @PreAuthorize("hasAuthority('dict:crud')")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ObjectResponse<DictData> save(HttpServletRequest request, HttpServletResponse response) {
        DictData dict = this.getParameter(request, DictData.class);

        DictType dictType = dictTypeService.getType(dict.getTypeId(), dict.getTypeValue());
        if (dictType != null) {
            dict.setTypeValue(dictType.getValue());
        }

        return new ObjectResponse<>(dictDataService.insertData(
            dictType == null ? null : dictType.getId(), dict, this.getUser().getName()));
    }

    @Log(module = "", desc = "")
    @PreAuthorize("hasAuthority('dict:crud')")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ObjectResponse<DictData> update(HttpServletRequest request,
                                           HttpServletResponse response) {
        DictData dict = this.getParameter(request, DictData.class);
        return new ObjectResponse<>(
            dictDataService.updateData(dict.getId(), dict, this.getUser().getName()));
    }

    @Log(module = "", desc = "")
    @PreAuthorize("hasAuthority('dict:crud')")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ObjectResponse<DictData> delete(HttpServletRequest request,
                                           HttpServletResponse response) {
        DictData dict = this.getParameter(request, DictData.class);
        return new ObjectResponse<>(
            dictDataService.deleteData(null, dict.getId(), this.getUser().getName()));
    }

}
