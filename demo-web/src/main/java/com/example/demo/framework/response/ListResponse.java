package com.example.demo.framework.response;

import com.example.demo.framework.constant.HttpStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

/**
 * @author JiakunXu
 */
@Getter
@Setter
public class ListResponse<T> extends AbstractResponse {

    @Serial
    private static final long serialVersionUID = -5567902033588518919L;

    private Long              total;

    private List<T>           rows;

    private T                 extra;

    public ListResponse(List<T> rows) {
        this.setCode(HttpStatus.OK);
        this.setRows(rows);
    }

    public ListResponse(long total, List<T> rows) {
        this.setCode(HttpStatus.OK);
        this.setTotal(total);
        this.setRows(rows);
    }

    public ListResponse(long total, List<T> rows, T extra) {
        this.setCode(HttpStatus.OK);
        this.setTotal(total);
        this.setRows(rows);
        this.setExtra(extra);
    }

}
