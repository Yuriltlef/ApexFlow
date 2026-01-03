// 权限常量定义
export const PERMISSIONS = {
  // 管理权限
  ADMIN: 'isAdmin',
  
  // 模块权限
  MANAGE_ORDER: 'canManageOrder',
  MANAGE_LOGISTICS: 'canManageLogistics',
  MANAGE_AFTER_SALES: 'canManageAfterSales',
  MANAGE_REVIEW: 'canManageReview',
  MANAGE_INVENTORY: 'canManageInventory',
  MANAGE_INCOME: 'canManageIncome'
};

// 权限中文名称映射
export const PERMISSION_NAMES = {
  [PERMISSIONS.ADMIN]: '超级管理员',
  [PERMISSIONS.MANAGE_ORDER]: '订单管理',
  [PERMISSIONS.MANAGE_LOGISTICS]: '物流管理',
  [PERMISSIONS.MANAGE_AFTER_SALES]: '售后管理',
  [PERMISSIONS.MANAGE_REVIEW]: '评价管理',
  [PERMISSIONS.MANAGE_INVENTORY]: '库存管理',
  [PERMISSIONS.MANAGE_INCOME]: '财务管理'
};

// 权限描述
export const PERMISSION_DESCRIPTIONS = {
  [PERMISSIONS.ADMIN]: '拥有系统所有权限',
  [PERMISSIONS.MANAGE_ORDER]: '可以管理订单的创建、查看、修改和删除',
  [PERMISSIONS.MANAGE_LOGISTICS]: '可以管理物流信息，包括发货、跟踪等',
  [PERMISSIONS.MANAGE_AFTER_SALES]: '可以处理售后申请和退款',
  [PERMISSIONS.MANAGE_REVIEW]: '可以管理商品评价',
  [PERMISSIONS.MANAGE_INVENTORY]: '可以管理商品库存',
  [PERMISSIONS.MANAGE_INCOME]: '可以管理财务收支'
};

// 获取用户权限列表（返回数组形式）
export function getUserPermissionsList(permissions) {
  if (!permissions) return [];
  
  return Object.keys(permissions)
    .filter(key => permissions[key] === true)
    .map(key => ({
      key,
      name: PERMISSION_NAMES[key] || key,
      description: PERMISSION_DESCRIPTIONS[key] || ''
    }));
}