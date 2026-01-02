package com.apex.core.dto;

/**
 * 管理员用户列表响应DTO
 */
public class AdminUserListResponse {
    private java.util.List<AdminUserDTO> users;
    private Integer currentPage;
    private Integer pageSize;
    private Long totalCount;
    private Integer totalPages;

    // Getters and Setters
    public java.util.List<AdminUserDTO> getUsers() { return users; }
    public void setUsers(java.util.List<AdminUserDTO> users) { this.users = users; }

    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }

    public Long getTotalCount() { return totalCount; }
    public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
}
