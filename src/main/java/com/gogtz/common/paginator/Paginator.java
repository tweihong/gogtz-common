/*
 * Copyright 2015-2016 the original author or authors..
 *
 * Licensed under the 青岛高软盛信息技术有限公司 License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gogtz.com/licenses/LICENSE-1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.gogtz.common.paginator;

import com.gogtz.common.string.GetterUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

/**
 * 分页器，根据page,limit,total用于页面上分页显示多项内容，计算页码和当前页的偏移量，方便页面分页使用.
 *
 * @author GOGTZ-Z
 * @version 0.0.0
 * @since 1.0
 */
public class Paginator<T> implements java.io.Serializable {

    // Paginator paginator = new Paginator(pageNum, pageSize, total, navigatePages, recordList);

    /**
     * serialVersionUID
     */
    private final long serialVersionUID = 1L;

    /**
     * 页数
     */
    private int pageNum;
    /**
     * 默认每页显示10条
     */
    private int pageSize = 10;

    /**
     * 条数
     */
    private int size;
    /**
     * 开始行
     */
    private int startRow;
    /**
     * 结束行
     */
    private int endRow;
    /**
     * 总记录数
     */
    private long total;
    /**
     * 总页数
     */
    private int pages;
    /**
     * 内容列表
     */
    private List<T> list;
    /**
     * 前一页 码数
     */
    private int firstPage;
    /**
     * 前一页
     */
    private int prePage;
    /**
     * 后一页
     */
    private int nextPage;
    /**
     * 后一页 码数
     */
    private int lastPage;
    /**
     * 是否是第一页
     */
    private boolean isFirstPage;
    /**
     * 是否是最后页
     */
    private boolean isLastPage;
    /**
     * 是否有前一页
     */
    private boolean hasPreviousPage;
    /**
     * 是否有后一页
     */
    private boolean hasNextPage;

    /**
     * 默认显示5个标签页
     */
    private int navigatePages = 8;

    /**
     * 页数条
     */
    private Integer[] navigatepageNums;

    /**
     * <p>
     * 使用构造函数创建分页.
     * </p>
     *
     * @param pageNum  起始页码
     * @param pageSize 一页显示多少页
     * @param total    总件数
     */
    public Paginator(Integer pageNum, Integer pageSize, Integer total) {
        this(pageNum, pageSize, total, null, null);
    }

    /**
     * <p>
     * 使用构造函数创建分页.
     * </p>
     *
     * @param pageNum       当前第几页
     * @param pageSize      一页显示多少页
     * @param total         总件数
     * @param navigatePages 翻页标签出现多少个
     */
    public Paginator(Integer pageNum, Integer pageSize, Integer total, Integer navigatePages, List<T> list) {
        super();
        this.pageSize = pageSize;
        this.total = total;
        this.navigatePages = navigatePages == null ? this.navigatePages : navigatePages;
        this.list = list;
        this.size = this.list == null ? 0 : this.list.size();
        this.pageNum = computePageNo(pageNum);
    }

    /**
     * <p>
     * 获取当前页码数.
     * </p>
     * <p>
     * <pre>
     * 1：Paginator paginator       = new Paginator(1, 99, 10);
     *    paginator.getPageNum()      = 1
     * 2：Paginator paginator       = new Paginator(1, 99);
     *    paginator.getPageNum()      = 1
     * </pre>
     *
     * @return 当前选择页的开始页码
     */
    public int getPageNum() {
        return pageNum;
    }

    /**
     * <p>
     * 获取每页显示的页码数.
     * </p>
     * <p>
     * <pre>
     * 1：Paginator paginator       = new Paginator(1, 99, 10);
     *    paginator.getPageSize()      = 10
     * 2：Paginator paginator       = new Paginator(1, 99);
     *    paginator.getPageSize()      = 5（默认5页）
     * </pre>
     *
     * @return 构造函数传入的值，默认返回5
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * <p>
     * 获取总件数.
     * </p>
     * <p>
     * <pre>
     * 1：Paginator paginator            = new Paginator(1, 99, 10);
     *    paginator.getTotal()      = 99
     * 2：Paginator paginator            = new Paginator(1, 99);
     *    paginator.getTotal()      = 99
     * </pre>
     *
     * @return 构造函数传入的值，返回总件数
     */
    public long getTotal() {
        return total;
    }

    /**
     * <p>
     * 是否是首页（第一页），第一页页码为1
     * </p>
     *
     * @return 是否是首页(true|false)
     */
    public boolean isFirstPage() {
        this.isFirstPage = this.pageNum <= 1;
        return this.isFirstPage;
    }

    /**
     * <p>
     * 是否是最后一页
     * </p>
     *
     * @return 是否是最后一页(true|false)
     */
    public boolean isLastPage() {
        this.isLastPage = this.pageNum >= getPages();
        return this.isLastPage;
    }

    /**
     * <p>
     * 前一页的页码
     * </p>
     *
     * @return 返回前一页的页码
     */
    public int getPrePage() {
        if (isHasPreviousPage()) {
            this.prePage = this.pageNum - 1;
        }
        return this.prePage;
    }

    /**
     * <p>
     * 下一页的页码
     * </p>
     *
     * @return 返回下一页的页码
     */
    public int getNextPage() {
        if (isHasNextPage()) {
            this.nextPage = this.pageNum + 1;
        }
        return this.nextPage;
    }

    /**
     * <p>
     * 是否有前一页。
     * </p>
     *
     * @return 是否有前一页(true|false)
     */
    public boolean isHasPreviousPage() {
        this.hasPreviousPage = (this.pageNum > 1);
        return this.hasPreviousPage;
    }

    /**
     * <p>
     * 是否有下一页。
     * </p>
     *
     * @return 是否有下一页(true|false)
     */
    public boolean isHasNextPage() {
        this.hasNextPage = (this.pageNum + 1 <= getPages());
        return this.hasNextPage;
    }

    /**
     * <p>
     * 获取开始行。
     * </p>
     *
     * @return 开始行
     */
    public int getStartRow() {
        if (getPageSize() <= 0 || this.total <= 0) {
            return 0;
        }
        this.startRow = this.pageNum > 0 ? (this.pageNum - 1) * getPageSize() + 1 : 0;
        return this.startRow;
    }

    /**
     * <p>
     * 获取结束行。
     * </p>
     *
     * @return 结束行
     */
    public int getEndRow() {
        endRow = 0;
        if (pageNum > 0) {
            endRow = Integer.valueOf(String.valueOf(Math.min((long) pageSize * (long) pageNum, getTotal())));
        }
        return endRow;
    }

    /**
     * 得到 总页数
     *
     * @return
     */
    public int getPages() {
        if (total <= 0) {
            pages = 0;
        }
        if (pageSize <= 0) {
            pages = 0;
        }

        long count = total / (long) pageSize;
        if (total % pageSize > 0) {
            count++;
        }
        pages = (int) count;
        return pages;
    }

    protected int computePageNo(int page) {
        int computePageNumber = this.computePageNumber(page, pageSize, total);
        this.getEndRow();
        this.isFirstPage();
        this.isLastPage();
        return computePageNumber;
    }

    /**
     * 页码滑动窗口，并将当前页尽可能地放在滑动窗口的中间部位。
     *
     * @return
     */
    public Integer[] getNavigatepageNums() {
        navigatepageNums = getNavigatePages(navigatePages);
        return navigatepageNums;
    }

    /**
     * 页码滑动窗口，并将当前页尽可能地放在滑动窗口的中间部位。 注意:不可以使用 getSlider(1)方法名称，因为在JSP中会与
     * getSlider()方法冲突，报exception
     *
     * @return
     */
    public Integer[] getNavigatePages(int navigatePages) {
        return generateLinkPageNumbers(getPageNum(), (int) getPages(), navigatePages);
    }

    private int computeisLastPageNumber(long totalItems, int pageSize) {
        if (pageSize <= 0)
            return 1;
        int result = (int) (totalItems % pageSize == 0 ? totalItems / pageSize : totalItems / pageSize + 1);
        if (result <= 1)
            result = 1;
        return result;
    }

    /**
     * @param page       当前页码
     * @param pageSize   每页显示条
     * @param totalItems 总条数
     * @return
     */
    private int computePageNumber(int page, int pageSize, long totalItems) {
        if (page <= 1) {
            return 1;
        }
        if (Integer.MAX_VALUE == page || page > computeisLastPageNumber(totalItems, pageSize)) { // last
            // page
            return computeisLastPageNumber(totalItems, pageSize);
        }
        return page;
    }

    private Integer[] generateLinkPageNumbers(int currentPageNumber, int isLastPageNumber, int count) {
        int avg = count / 2;

        int startPageNumber = currentPageNumber - avg;
        if (startPageNumber <= 0) {
            startPageNumber = 1;
        }

        int endPageNumber = startPageNumber + count - 1;
        if (endPageNumber > isLastPageNumber) {
            endPageNumber = isLastPageNumber;
        }

        if (endPageNumber - startPageNumber < count) {
            startPageNumber = endPageNumber - count + 1;
            if (startPageNumber <= 0) {
                startPageNumber = 1;
            }
        }

        List<Integer> result = new java.util.ArrayList<Integer>();
        for (int i = startPageNumber; i <= endPageNumber; i++) {
            result.add(new Integer(i));
        }

        Integer[] computePageNumber = result.toArray(new Integer[result.size()]);


        if (computePageNumber != null && computePageNumber.length > 0) {
            this.firstPage = computePageNumber[0];
            this.lastPage = computePageNumber[computePageNumber.length - 1];
        }

        return computePageNumber;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("PageInfo{");
        sb.append("pageNum=").append(this.getPageNum());
        sb.append(", pageSize=").append(this.getPageSize());
        sb.append(", size=").append(this.size);
        sb.append(", startRow=").append(this.getStartRow());
        sb.append(", endRow=").append(this.getEndRow());
        sb.append(", total=").append(this.getTotal());
        sb.append(", pages=").append(this.getPages());
        sb.append(", list=").append(this.list);
        sb.append(", firstPage=").append(this.firstPage);
        sb.append(", prePage=").append(this.getPrePage());
        sb.append(", nextPage=").append(this.getNextPage());
        sb.append(", lastPage=").append(this.lastPage);
        sb.append(", isFirstPage=").append(this.isFirstPage());
        sb.append(", isLastPage=").append(this.isLastPage());
        sb.append(", hasPreviousPage=").append(this.hasPreviousPage);
        sb.append(", hasNextPage=").append(this.hasNextPage);
        sb.append(", navigatePages=").append(this.navigatePages);
        sb.append(", navigatepageNums=");
        if (this.getNavigatepageNums() == null) {
            sb.append("null");
        } else {
            sb.append('[');

            for (int i = 0; i < this.getNavigatepageNums().length; ++i) {
                sb.append(i == 0 ? "" : ", ").append(this.getNavigatepageNums()[i]);
            }

            sb.append(']');
        }

        sb.append('}');
        return sb.toString();
    }

    /**
     * 获取当前的页码数和每页显示的行数
     *
     * @param request
     * @return
     */
    public static HashMap<String, Integer> getPageNumPageSize(HttpServletRequest request) {
        return getPageNumPageSize(request, 1, 10);
    }

    /**
     * 获取当前的页码数和每页显示的行数
     *
     * @param request
     * @param defaultPageNum
     * @param defaultPageSize
     * @return
     */
    public static HashMap<String, Integer> getPageNumPageSize(HttpServletRequest request, int defaultPageNum, int defaultPageSize) {
        int pageNum = request.getParameter("offset") == null ? defaultPageNum : Integer.parseInt(request.getParameter("offset"));
        int pageSize = request.getParameter("limit") == null ? defaultPageSize : Integer.parseInt(request.getParameter("limit"));

        if (pageNum == 0) {
            pageNum = defaultPageNum;
        } else {
            pageNum = pageNum / pageSize + 1;
        }

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("pageNum", pageNum);
        map.put("pageSize", pageSize);
        return map;
    }

    /**
     * 获取当前的页码数和每页显示的行数
     *
     * @param defaultPageNum
     * @param defaultPageSize
     * @return
     */
    public static HashMap<String, Integer> getPageNumPageSize(String defaultPageNum, String defaultPageSize) {
        int pageNum = 0;
        int pageSize = 10;
        if (StringUtils.isNotEmpty(defaultPageNum)) {
            pageNum = GetterUtil.getInteger(defaultPageNum);
        }
        if (StringUtils.isNotEmpty(defaultPageSize)) {
            pageSize = GetterUtil.getInteger(defaultPageSize);
        }
        if (pageNum == 0) {
            pageNum = 1;
        } else {
            pageNum = pageNum / pageSize + 1;
        }

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("pageNum", pageNum);
        map.put("pageSize", pageSize);
        return map;
    }
}
