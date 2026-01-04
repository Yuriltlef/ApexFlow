// src/utils/userData.d.ts

declare interface UserInfo {
  realName?: string;
  username?: string;
  isGuest?: boolean;
  [key: string]: any;
}

declare interface Permissions {
  isAdmin?: boolean;
  canManageOrder?: boolean;
  canManageLogistics?: boolean;
  canManageAfterSales?: boolean;
  [key: string]: boolean;
}

declare class UserDataManager {
  constructor();
  private _userInfo: UserInfo | null;
  private _permissions: Permissions | null;
  private _token: string | null;
  
  private _initFromStorage(): void;
  private _clearAll(): void;
  
  setUserData(userData: UserInfo, permissionsData: Permissions): boolean;
  setToken(token: string): void;
  getPermissions(): Permissions | null;
  getToken(): string | null;
  isLoggedIn(): boolean;
  isGuest(): boolean;
  hasPermission(permissionKey: string): boolean;
  isAdmin(): boolean;
  logout(): void;
  getDisplayName(): string;
  getUserRoleText(): string;
  getUserInfo(): UserInfo | null;
}

declare const userDataManager: UserDataManager;

export default userDataManager;