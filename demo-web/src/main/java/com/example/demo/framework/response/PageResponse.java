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
public class PageResponse<T> extends AbstractResponse {

    @Serial
    private static final long serialVersionUID = 5272336433321751345L;

    private Long              pageNo;

    private Long              pageSize;

    private Long              pageCount;

    private Long              totalCount;

    private List<T>           list;

    public PageResponse(Long pageNo, Long pageSize, Long totalCount, List<T> list) {
        this.setCode(HttpStatus.OK);
        this.setPageNo(pageNo == null ? 0 : pageNo);
        this.setPageSize(pageSize == null ? 0 : pageSize);
        this.setTotalCount(totalCount == null ? 0 : totalCount);
        this.setList(list);

        if (this.pageSize == 0 || this.totalCount == 0) {
            this.setPageCount(1L);
        } else {
            this.setPageCount(
                this.totalCount / this.pageSize + (this.totalCount % this.pageSize == 0 ? 0 : 1));
        }
    }

}
