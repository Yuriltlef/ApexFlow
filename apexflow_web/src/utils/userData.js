// src/utils/userData.js

class UserDataManager {
  constructor() {
    // 私有属性，存储用户数据
    this._userInfo = null;
    this._permissions = null;
    this._token = null;
    
    // 尝试从本地存储初始化
    this._initFromStorage();
  }
  
  // 从本地存储初始化数据
  _initFromStorage() {
    try {
      const token = localStorage.getItem('token');
      const userInfoStr = localStorage.getItem('userInfo');
      const permissionsStr = localStorage.getItem('permissions');
      
      if (token) this._token = token;
      if (userInfoStr) this._userInfo = JSON.parse(userInfoStr);
      if (permissionsStr) this._permissions = JSON.parse(permissionsStr);
    } catch (error) {
      console.warn('初始化用户数据失败:', error);
      this._clearAll();
    }
  }
  
  // 清空所有数据
  _clearAll() {
    this._userInfo = null;
    this._permissions = null;
    this._token = null;
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    localStorage.removeItem('permissions');
  }
  
  // 设置用户数据
  setUserData(userData, permissionsData) {
    try {
      // 设置内存数据
      this._userInfo = userData;
      this._permissions = permissionsData;
      
      // 持久化存储
      localStorage.setItem('userInfo', JSON.stringify(userData));
      localStorage.setItem('permissions', JSON.stringify(permissionsData));
      return true;
    } catch (e) {
      console.error('保存用户数据失败', e);
      return false;
    }
  }

  // [新增] 设置 Token (用于游客模式模拟)
  setToken(token) {
    this._token = token;
    localStorage.setItem('token', token);
  }
  
  // 获取权限信息
  getPermissions() {
    return this._permissions;
  }
  
  // 获取Token
  getToken() {
    return this._token;
  }
  
  // 检查是否已登录
  isLoggedIn() {
    return !!this._token && !!this._userInfo;
  }

  // [新增] 判断是否为游客
  isGuest() {
    return this._userInfo && this._userInfo.isGuest === true;
  }
  
  // 检查是否有特定权限
  hasPermission(permissionKey) {
    if (!this._permissions) return false;
    return this._permissions[permissionKey] === true;
  }
  
  // 检查是否是管理员
  isAdmin() {
    if (!this._permissions) return false;
    return this._permissions.isAdmin === true;
  }
  
  // 登出 - 清除所有数据
  logout() {
    this._clearAll();
  }
  
  // 获取用户显示名称
  getDisplayName() {
    if (!this._userInfo) return '未登录用户';
    return this._userInfo.realName || this._userInfo.username || '用户';
  }
  
  // 获取用户角色文本
  getUserRoleText() {
    if (!this._permissions) return '未知角色';
    
    // [修改] 优先判断游客
    if (this.isGuest()) return '访客模式';

    if (this.isAdmin()) return '超级管理员';
    
    // 根据权限判断角色
    const roles = [];
    if (this.hasPermission('canManageOrder')) roles.push('订单管理员');
    if (this.hasPermission('canManageLogistics')) roles.push('物流管理员');
    if (this.hasPermission('canManageAfterSales')) roles.push('售后管理员');
    
    return roles.length > 0 ? roles.join(' · ') : '普通用户';
  }

  getUserInfo() {
    return this._userInfo;
  }
}

const userDataManager = new UserDataManager();
export default userDataManager;